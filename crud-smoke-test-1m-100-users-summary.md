# CRUD Smoke Test Summary: 1M Actions Across 100 Users

## Result

Smoke test completed successfully.

## Run Details

- Date/time: 2026-05-12T12:15:09Z
- Users: 100
- Actions: 1,000,000
- Seed: 42
- Components tested: MySQL, HBase, ClickHouse
- Output directory: `/tmp/crud-benchmark-1m-100-users`
- Build status: Success
- Maven total time: 6.784 s

## Command

```bash
mvn test && mvn compile exec:java \
  -Dexec.mainClass=com.helpercodes.crudbenchmark.CrudBenchmarkRunner \
  -Dexec.args="users=100 actions=1000000 seed=42 output=/tmp/crud-benchmark-1m-100-users"
```

## Generated Artifacts

- `/tmp/crud-benchmark-1m-100-users/crud-action-stats.csv`
- `/tmp/crud-benchmark-1m-100-users/crud-action-stats.html`

## Action Counts

| Action | Count |
| --- | ---: |
| INSERT | 399,761 |
| READ | 249,585 |
| UPDATE | 249,932 |
| DELETE | 100,722 |

## Latency Summary

| Component | Action | Count | Avg ms | Min ms | Max ms |
| --- | --- | ---: | ---: | ---: | ---: |
| MySQL | INSERT | 399,761 | 0.000225 | 0.000040 | 9.022420 |
| MySQL | READ | 249,585 | 0.001426 | 0.000100 | 131.680957 |
| MySQL | UPDATE | 249,932 | 0.001808 | 0.000511 | 71.584705 |
| MySQL | DELETE | 100,722 | 0.000286 | 0.000040 | 0.038652 |
| HBase | INSERT | 399,761 | 0.000448 | 0.000170 | 0.782901 |
| HBase | READ | 249,585 | 0.001301 | 0.000170 | 9.029144 |
| HBase | UPDATE | 249,932 | 0.002415 | 0.000641 | 48.813011 |
| HBase | DELETE | 100,722 | 0.000967 | 0.000160 | 2.579064 |
| ClickHouse | INSERT | 399,761 | 0.000179 | 0.000040 | 9.628535 |
| ClickHouse | READ | 249,585 | 0.000680 | 0.000080 | 0.142435 |
| ClickHouse | UPDATE | 249,932 | 0.000980 | 0.000491 | 0.311952 |
| ClickHouse | DELETE | 100,722 | 0.000240 | 0.000030 | 0.023684 |

## Notes

- The smoke test used the repository's in-process benchmark adapters, so no external database services were required.
- `mvn test` passed before the smoke test run.
