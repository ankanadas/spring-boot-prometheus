# Metrics Persistence Guide

## The Problem

When you restart your Spring Boot application:
- **Prometheus counters reset to 0** (in-memory metrics)
- **Grafana dashboards show gaps** or reset data
- **Historical data appears lost**

## The Solution

### 1. Prometheus Data Retention (✅ Configured)

Prometheus stores metrics data on disk and retains it across restarts:

```bash
# Start Prometheus with 30-day retention
./start-prometheus.sh
```

**Configuration:**
- **Data Directory**: `prometheus/data/` (persisted on disk)
- **Retention**: 7 days
- **Recording Rules**: Pre-aggregated metrics for better performance

### 2. Use Rate Queries in Grafana

Instead of using raw counter values, use `rate()` or `increase()` functions:

**❌ Bad (Shows Resets):**
```promql
user_cache_hits_total
```

**✅ Good (Handles Resets):**
```promql
rate(user_cache_hits_total[5m])
```

**✅ Better (Shows Total Increase):**
```promql
increase(user_cache_hits_total[1h])
```

### 3. Recording Rules (✅ Configured)

Pre-computed metrics that survive app restarts:

```yaml
# prometheus/recording-rules.yml
- record: cache:hit_ratio
  expr: |
    rate(user_cache_hits_total[5m]) / 
    (rate(user_cache_hits_total[5m]) + rate(user_cache_misses_total[5m]))
```

## How It Works

### Scenario: App Restart

**Before Restart:**
```
user_cache_hits_total = 1000
user_cache_misses_total = 200
```

**After Restart:**
```
user_cache_hits_total = 0  ← Counter resets
user_cache_misses_total = 0
```

**Prometheus Behavior:**
- Stores historical data: `[1000, 1000, 1000, 0, 5, 10, 15...]`
- `rate()` function handles the reset automatically
- Calculates rate based on time series, not absolute values

**Grafana Display:**
- Shows continuous trend line
- No gaps or jumps
- Historical data preserved

## Best Practices

### 1. Always Use Rate Functions

```promql
# Cache hit rate (per second)
rate(user_cache_hits_total[5m])

# Total cache hits in last hour
increase(user_cache_hits_total[1h])

# Cache hit ratio
rate(user_cache_hits_total[5m]) / 
(rate(user_cache_hits_total[5m]) + rate(user_cache_misses_total[5m]))
```

### 2. Use Recording Rules for Complex Queries

Recording rules pre-compute metrics every 10 seconds:
- Faster dashboard loading
- Consistent calculations
- Survives app restarts

### 3. Configure Proper Retention

```bash
# 7 days (default in start-prometheus.sh)
--storage.tsdb.retention.time=7d

# Or by size
--storage.tsdb.retention.size=10GB
```

## Starting Services

### Option 1: Use Startup Script (Recommended)

```bash
# Start Prometheus with proper configuration
./start-prometheus.sh

# Start Spring Boot app
./mvnw spring-boot:run
```

### Option 2: Manual Start

```bash
# Start Prometheus
prometheus \
  --config.file=prometheus/prometheus.yml \
  --storage.tsdb.path=prometheus/data \
  --storage.tsdb.retention.time=7d \
  --web.listen-address=:9090

# Start Spring Boot
./mvnw spring-boot:run
```

## Verifying Data Persistence

### 1. Check Prometheus Data Directory

```bash
ls -lh prometheus/data/
# Should show TSDB blocks with timestamps
```

### 2. Query Historical Data

Visit http://localhost:9090 and run:

```promql
# Check if historical data exists
user_cache_hits_total[1h]

# Should show data points even after app restart
```

### 3. Test App Restart

```bash
# 1. Make some API calls
curl http://localhost:8080/api/users/1
curl http://localhost:8080/api/users/2

# 2. Check metrics
curl http://localhost:8080/actuator/prometheus | grep cache_hits

# 3. Restart app
# Stop and start Spring Boot

# 4. Check Grafana dashboard
# Historical data should still be visible
```

## Troubleshooting

### Dashboard Shows Gaps

**Problem**: Grafana shows gaps when app restarts

**Solution**: Use `rate()` or `increase()` functions:
```promql
rate(user_cache_hits_total[5m])
```

### Data Disappears After 7 Days

**Problem**: Data older than 7 days is automatically deleted

**Solution**: This is by design for cache metrics. To keep data longer:
```bash
# Edit start-prometheus.sh and change retention
--storage.tsdb.retention.time=30d  # or any duration you want
```

### Metrics Reset to Zero

**Problem**: This is normal behavior for Prometheus counters

**Solution**: 
- Prometheus handles this automatically
- Use rate/increase functions
- Recording rules smooth out the data

## Summary

✅ **Prometheus**: Stores data on disk (7-day retention)
✅ **Recording Rules**: Pre-computed metrics
✅ **Rate Functions**: Handle counter resets
✅ **Grafana**: Shows continuous trends

Your metrics are now persistent across restarts! 🎉
