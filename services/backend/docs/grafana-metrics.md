# Grafana Dashboard Queries

## Reservation Latency Histogram
```
histogram_quantile(0.95, sum(rate(reservation_creation_latency_seconds_bucket[5m])) by (le))
```

## Failed Payments Counter
```
rate(failed_payments_total[5m])
```

## JVM & System Health
```
jvm_memory_used_bytes
process_cpu_usage
```

