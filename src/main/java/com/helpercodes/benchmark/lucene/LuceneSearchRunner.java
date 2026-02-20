package com.helpercodes.benchmark.lucene;

import com.helpercodes.benchmark.api.SearchResultItem;
import com.helpercodes.benchmark.api.SearchRunner;
import com.helpercodes.benchmark.config.BenchmarkConfig;
import com.helpercodes.benchmark.data.QuerySpec;
import com.helpercodes.benchmark.data.QueryType;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.MMapDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.lucene.document.IntPoint.newRangeQuery;

public class LuceneSearchRunner implements SearchRunner {
    private final MMapDirectory directory;
    private final DirectoryReader reader;
    private final IndexSearcher searcher;

    public LuceneSearchRunner(BenchmarkConfig config) throws IOException {
        this.directory = new MMapDirectory(config.luceneDirectory());
        this.reader = DirectoryReader.open(directory);
        this.searcher = new IndexSearcher(reader);
    }

    @Override
    public List<SearchResultItem> search(QuerySpec querySpec) throws IOException {
        Query query = switch (querySpec.queryType()) {
            case PURE_KEYWORD -> new TermQuery(new Term("keyword0", querySpec.keyword()));
            case PURE_VECTOR -> new KnnFloatVectorQuery("vectorA", querySpec.vector(), querySpec.topK());
            case HYBRID -> new KnnFloatVectorQuery(
                    "vectorA", querySpec.vector(), querySpec.topK(), new TermQuery(new Term("keyword0", querySpec.keyword())));
            case RANGE_VECTOR -> new KnnFloatVectorQuery(
                    "vectorA", querySpec.vector(), querySpec.topK(), newRangeQuery("int0", querySpec.rangeMin(), querySpec.rangeMax()));
        };

        if (querySpec.queryType() == QueryType.PURE_KEYWORD) {
            BooleanQuery.Builder boosted = new BooleanQuery.Builder();
            boosted.add(query, BooleanClause.Occur.MUST);
            boosted.add(new KnnFloatVectorQuery("vectorA", querySpec.vector(), querySpec.topK()), BooleanClause.Occur.SHOULD);
            query = boosted.build();
        }

        TopDocs topDocs = searcher.search(query, querySpec.topK());
        List<SearchResultItem> results = new ArrayList<>(topDocs.scoreDocs.length);
        for (ScoreDoc sd : topDocs.scoreDocs) {
            Document d = searcher.doc(sd.doc);
            results.add(new SearchResultItem(d.getField("docId").numericValue().intValue(), sd.score));
        }
        return results;
    }

    @Override
    public String name() {
        return "lucene";
    }

    @Override
    public void close() throws IOException {
        reader.close();
        directory.close();
    }
}
