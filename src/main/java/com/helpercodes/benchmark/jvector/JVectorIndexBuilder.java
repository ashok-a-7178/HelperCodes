package com.helpercodes.benchmark.jvector;

import com.helpercodes.benchmark.api.IndexBuilder;
import com.helpercodes.benchmark.config.BenchmarkConfig;
import com.helpercodes.benchmark.data.DocumentRecord;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JVectorIndexBuilder implements IndexBuilder {
    private final Path indexFile;
    private final List<JVectorDocument> docs = new ArrayList<>();

    public JVectorIndexBuilder(BenchmarkConfig config) throws IOException {
        Files.createDirectories(config.jvectorDirectory());
        this.indexFile = config.jvectorDirectory().resolve("jvector-index.bin");
    }

    @Override
    public void add(DocumentRecord record) {
        docs.add(new JVectorDocument(record.docId(), record.keywordFields()[0], record.intFields()[0], record.vectorA()));
    }

    @Override
    public void close() throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(indexFile)))) {
            out.writeObject(docs);
        }
    }
}
