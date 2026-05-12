# Search Benchmark (Lucene vs JVector)

This Maven project builds two independent indexes over the same deterministic synthetic dataset:

1. **Apache Lucene** (inverted index + native HNSW vector fields)
2. **JVector-based index** (separate on-disk vector/metadata index and cosine ANN scoring)

## Features

- Shared deterministic dataset generator (fixed seed)
- Document schema:
  - 10 int fields
  - 10 float fields
  - 20 analyzed text fields
  - 8 keyword fields
  - 1 `comment` field
  - 2 vectors from `comment`
- Configurable vector dimension (default `384`)
- Query workloads:
  - pure keyword
  - pure vector
  - hybrid (keyword + vector ranking)
  - range + vector ranking
- Metrics:
  - latency (avg, p95, p99)
  - throughput (QPS)
  - ranking overlap (Top 1%, 1–5%, 5–10%)
  - intersection, Jaccard, Recall@K
  - Spearman correlation
- CSV output for per-query metrics and ranking comparisons

## Build

```bash
mvn clean compile
```

## Run

Default settings target the full experiment scale from the requirements (20M docs, TopK=1000):

```bash
mvn exec:java
```

For a quick local run, override parameters:

```bash
mvn exec:java -Dexec.args="docs=10000 queries=100 warmup=20 topK=100 vectorDim=384 seed=42"
```

## Output

Outputs are written to `benchmark-output/`:

- `per-query-metrics.csv`
- `ranking-comparison-metrics.csv`
- separate Lucene and JVector index directories

## CRUD Ticket Benchmark

The repository also includes a deterministic CRUD benchmark for a ticket system.
It generates insert/read/update/delete actions across many users, stores ticket
metadata with timestamp and user ID, replays the same workload against MySQL,
HBase, and ClickHouse-style components, and writes action latency analytics.

Run the default workload (100k actions, 10k users):

```bash
mvn exec:java -Dexec.mainClass=com.helpercodes.crudbenchmark.CrudBenchmarkRunner
```

Run a smaller local workload:

```bash
mvn exec:java -Dexec.mainClass=com.helpercodes.crudbenchmark.CrudBenchmarkRunner -Dexec.args="users=100 actions=1000 seed=42 output=crud-benchmark-output"
```

CRUD analytics are written to `crud-benchmark-output/`:

- `crud-action-stats.csv`
- `crud-action-stats.html`

The included MySQL, HBase, and ClickHouse components are in-process benchmark
adapters so the workload can run without external database services. Replace the
`TicketStore` implementations with real client-backed adapters when database
clusters are available.

## Reproducibility

- Data generation is deterministic from `(seed, docId)`.
- Query generation is deterministic from `seed`.
- Same generated documents and vectors are used for both index builders.
