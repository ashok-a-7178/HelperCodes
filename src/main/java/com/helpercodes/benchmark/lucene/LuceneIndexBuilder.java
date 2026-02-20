package com.helpercodes.benchmark.lucene;

import com.helpercodes.benchmark.api.IndexBuilder;
import com.helpercodes.benchmark.config.BenchmarkConfig;
import com.helpercodes.benchmark.data.DocumentRecord;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.VectorSimilarityFunction;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;

import java.io.IOException;
import java.nio.file.Files;

public class LuceneIndexBuilder implements IndexBuilder {
    private final Directory directory;
    private final IndexWriter writer;

    public LuceneIndexBuilder(BenchmarkConfig config) throws IOException {
        Files.createDirectories(config.luceneDirectory());
        this.directory = new MMapDirectory(config.luceneDirectory());
        IndexWriterConfig writerConfig = new IndexWriterConfig(new StandardAnalyzer());
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writerConfig.setRAMBufferSizeMB(256);
        this.writer = new IndexWriter(directory, writerConfig);
    }

    @Override
    public void add(DocumentRecord record) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("docId", Integer.toString(record.docId()), Field.Store.YES));

        for (int i = 0; i < record.intFields().length; i++) {
            doc.add(new IntPoint("int" + i, record.intFields()[i]));
            doc.add(new StoredField("int" + i + "_stored", record.intFields()[i]));
        }
        for (int i = 0; i < record.floatFields().length; i++) {
            doc.add(new FloatPoint("float" + i, record.floatFields()[i]));
            doc.add(new StoredField("float" + i + "_stored", record.floatFields()[i]));
        }
        for (int i = 0; i < record.textFields().length; i++) {
            doc.add(new TextField("text" + i, record.textFields()[i], Field.Store.NO));
        }
        for (int i = 0; i < record.keywordFields().length; i++) {
            doc.add(new StringField("keyword" + i, record.keywordFields()[i], Field.Store.NO));
        }

        doc.add(new TextField("comment", record.comment(), Field.Store.NO));
        doc.add(new KnnFloatVectorField("vectorA", record.vectorA(), VectorSimilarityFunction.COSINE));
        doc.add(new KnnFloatVectorField("vectorB", record.vectorB(), VectorSimilarityFunction.COSINE));

        writer.addDocument(doc);
    }

    @Override
    public void close() throws IOException {
        writer.commit();
        writer.close();
        directory.close();
    }
}
