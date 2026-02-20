package com.helpercodes.benchmark.api;

import com.helpercodes.benchmark.data.DocumentRecord;

public interface IndexBuilder extends AutoCloseable {
    void add(DocumentRecord record) throws Exception;
    @Override
    void close() throws Exception;
}
