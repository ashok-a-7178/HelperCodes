package com.helpercodes.benchmark.jvector;

import com.helpercodes.benchmark.api.SearchResultItem;
import com.helpercodes.benchmark.api.SearchRunner;
import com.helpercodes.benchmark.config.BenchmarkConfig;
import com.helpercodes.benchmark.data.QuerySpec;
import com.helpercodes.benchmark.data.QueryType;
import io.github.jbellis.jvector.graph.GraphIndexBuilder;
import io.github.jbellis.jvector.graph.GraphSearcher;
import io.github.jbellis.jvector.graph.ImmutableGraphIndex;
import io.github.jbellis.jvector.graph.MapRandomAccessVectorValues;
import io.github.jbellis.jvector.graph.SearchResult;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import io.github.jbellis.jvector.vector.VectorizationProvider;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import io.github.jbellis.jvector.vector.types.VectorTypeSupport;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class JVectorSearchRunner implements SearchRunner {
    private final List<JVectorDocument> docs;
    private final MapRandomAccessVectorValues vectors;
    private final ImmutableGraphIndex graph;
    private final int searchEf;

    @SuppressWarnings("unchecked")
    public JVectorSearchRunner(BenchmarkConfig config) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(config.jvectorDirectory().resolve("jvector-index.bin"))))) {
            this.docs = (List<JVectorDocument>) in.readObject();
        }
        this.searchEf = config.jvectorSearchEf();
        VectorTypeSupport support = VectorizationProvider.getInstance().getVectorTypeSupport();
        Map<Integer, VectorFloat<?>> vectorMap = new HashMap<>(docs.size());
        for (int i = 0; i < docs.size(); i++) {
            vectorMap.put(i, support.createFloatVector(docs.get(i).vector()));
        }
        this.vectors = new MapRandomAccessVectorValues(vectorMap, config.vectorDimension());
        try (GraphIndexBuilder builder = new GraphIndexBuilder(
                vectors,
                VectorSimilarityFunction.COSINE,
                config.jvectorGraphM(),
                config.jvectorSearchEf(),
                1.2f,
                1.2f,
                false)) {
            this.graph = builder.build(vectors);
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

        VectorFloat<?> queryVector = VectorizationProvider.getInstance().getVectorTypeSupport().createFloatVector(query.vector());
        Bits bits = node -> node >= 0 && node < docs.size() && filter.test(docs.get(node));
        SearchResult annResult = GraphSearcher.search(
                queryVector,
                query.topK(),
                Math.max(searchEf, query.topK()),
                vectors,
                VectorSimilarityFunction.COSINE,
                graph,
                bits);
        List<SearchResultItem> result = new ArrayList<>(annResult.getNodes().length);
        for (SearchResult.NodeScore hit : annResult.getNodes()) {
            if (hit.node >= 0 && hit.node < docs.size()) {
                result.add(new SearchResultItem(docs.get(hit.node).docId(), hit.score));
            }
        }
        return result;
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
