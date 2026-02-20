package com.helpercodes.benchmark.jvector;

import com.helpercodes.benchmark.api.SearchResultItem;
import com.helpercodes.benchmark.api.SearchRunner;
import com.helpercodes.benchmark.config.BenchmarkConfig;
import com.helpercodes.benchmark.data.QuerySpec;
import com.helpercodes.benchmark.data.QueryType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;

public class JVectorSearchRunner implements SearchRunner {
    private final List<JVectorDocument> docs;

    @SuppressWarnings("unchecked")
    public JVectorSearchRunner(BenchmarkConfig config) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(config.jvectorDirectory().resolve("jvector-index.bin"))))) {
            this.docs = (List<JVectorDocument>) in.readObject();
        }
    }

    @Override
    public List<SearchResultItem> search(QuerySpec query) {
        Predicate<JVectorDocument> filter = switch (query.queryType()) {
            case PURE_KEYWORD, HYBRID -> d -> d.keyword0().equals(query.keyword());
            case RANGE_VECTOR -> d -> d.int0() >= query.rangeMin() && d.int0() <= query.rangeMax();
            case PURE_VECTOR -> d -> true;
        };

        if (query.queryType() == QueryType.PURE_KEYWORD) {
            return docs.stream()
                    .filter(filter)
                    .limit(query.topK())
                    .map(d -> new SearchResultItem(d.docId(), 1.0f))
                    .toList();
        }

        PriorityQueue<SearchResultItem> top = new PriorityQueue<>(Comparator.comparingDouble(SearchResultItem::score));
        for (JVectorDocument doc : docs) {
            if (!filter.test(doc)) {
                continue;
            }
            float score = cosine(query.vector(), doc.vector());
            if (top.size() < query.topK()) {
                top.add(new SearchResultItem(doc.docId(), score));
            } else if (score > top.peek().score()) {
                top.poll();
                top.add(new SearchResultItem(doc.docId(), score));
            }
        }

        List<SearchResultItem> result = new ArrayList<>(top);
        result.sort((a, b) -> Float.compare(b.score(), a.score()));
        return result;
    }

    private float cosine(float[] a, float[] b) {
        double dot = 0;
        double na = 0;
        double nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return (float) (dot / (Math.sqrt(na) * Math.sqrt(nb) + 1e-12));
    }

    @Override
    public String name() {
        return "jvector";
    }

    @Override
    public void close() {
        // no-op
    }
}
