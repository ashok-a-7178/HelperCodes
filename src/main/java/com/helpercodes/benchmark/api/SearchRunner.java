package com.helpercodes.benchmark.api;

import com.helpercodes.benchmark.data.QuerySpec;

import java.util.List;

public interface SearchRunner extends AutoCloseable {
    List<SearchResultItem> search(QuerySpec query) throws Exception;
    String name();
    @Override
    void close() throws Exception;
}
