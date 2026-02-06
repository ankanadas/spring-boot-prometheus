# Loki Log Storage Guide

## How Loki Stores Logs

Loki is a **log aggregation system** that stores logs on the local filesystem (not in a traditional database).

### Storage Architecture

```
┌─────────────────────────────────────────────────────┐
│  Spring Boot App                                    │
│  └─ Writes logs to: logs/spring-boot.log          │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│  Promtail (Log Collector)                          │
│  └─ Reads: logs/spring-boot.log                   │
│  └─ Sends to: Loki (port 3100)                    │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│  Loki (Log Storage)                                │
│  ├─ Indexes: loki/tsdb-shipper-active/            │
│  ├─ Chunks: loki/chunks/                          │
│  ├─ WAL: loki/wal/                                │
│  └─ Compactor: loki/compactor/                    │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│  Grafana (Log Viewer)                              │
│  └─ Queries Loki for logs                         │
└─────────────────────────────────────────────────────┘
```

## Storage Components

### 1. **Chunks** (`loki/chunks/`)
- **What**: Compressed log data
- **Format**: Gzip compressed chunks
- **Size**: ~440KB currently
- **Purpose**: Actual log content storage

### 2. **WAL** (`loki/wal/`)
- **What**: Write-Ahead Log
- **Format**: Binary log files
- **Size**: ~64KB currently
- **Purpose**: Temporary buffer before writing to chunks

### 3. **TSDB Index** (`loki/tsdb-shipper-active/`)
- **What**: Time-series database index
- **Format**: TSDB files
- **Purpose**: Fast log searching by labels and time

### 4. **Compactor** (`loki/compactor/`)
- **What**: Cleanup and optimization
- **Purpose**: Deletes old logs, merges chunks

## Database Type

**Loki does NOT use a traditional database!**

Instead, it uses:
- **Filesystem storage** (local disk)
- **TSDB (Time Series Database)** for indexing
- **Object storage** (filesystem in our case)

### Why Not a Database?

✅ **Advantages:**
- Faster writes (append-only)
- Lower resource usage
- Simpler deployment
- Cost-effective storage
- Easy to scale

❌ **Not Suitable For:**
- Complex queries
- Full-text search
- Relational data

## Retention Configuration

### Current Settings (✅ Configured)

```yaml
limits_config:
  retention_period: 168h  # 7 days

compactor:
  retention_enabled: true
  retention_delete_delay: 2h
  compaction_interval: 10m
```

### What This Means:

| Setting | Value | Description |
|---------|-------|-------------|
| **Retention Period** | 7 days (168h) | Logs older than 7 days are deleted |
| **Delete Delay** | 2 hours | Wait 2h before actually deleting |
| **Compaction Interval** | 10 minutes | Check for old logs every 10 min |

## Log Lifecycle

```
Day 0:    Log created → Written to WAL
Day 0-1:  WAL → Compressed to chunks
Day 1-7:  Stored in chunks (queryable)
Day 7+:   Marked for deletion by compactor
Day 7+2h: Actually deleted from disk
```

## Storage Size

Check current storage usage:

```bash
# Total Loki storage
du -sh loki/

# Individual components
du -sh loki/chunks/    # Compressed logs
du -sh loki/wal/       # Write-ahead log
du -sh loki/tsdb-shipper-active/  # Index

# Example output:
# 440K    loki/chunks
# 64K     loki/wal
# 128K    loki/tsdb-shipper-active
```

## How Logs Flow

### 1. Application Writes Logs
```java
logger.info("User {} fetched from cache", userId);
```
↓
```
logs/spring-boot.log:
2026-02-05 18:30:15.123 [http-nio-8080-exec-1] INFO  UserService - User 1 fetched from cache
```

### 2. Promtail Reads and Sends
```yaml
# promtail-config.yml
- job_name: spring-boot-prometheus
  static_configs:
    - targets:
        - localhost
      labels:
        job: spring-boot-prometheus
        __path__: /path/to/logs/spring-boot.log
```

### 3. Loki Stores
- **Indexes** by labels: `{job="spring-boot-prometheus"}`
- **Compresses** log content
- **Stores** in chunks directory

### 4. Grafana Queries
```logql
{job="spring-boot-prometheus"} |= "cache"
```

## Retention Management

### Check Retention Status

```bash
# Check Loki config
cat loki/loki-config.yml | grep -A 5 "retention"

# Check compactor logs
tail -f loki/compactor/*.log
```

### Adjust Retention Period

Edit `loki/loki-config.yml`:

```yaml
limits_config:
  retention_period: 168h   # Change to 336h for 14 days, etc.
```

Then restart Loki:
```bash
brew services restart loki
```

### Manual Cleanup

```bash
# Stop Loki
brew services stop loki

# Remove old data
rm -rf loki/chunks/*
rm -rf loki/wal/*
rm -rf loki/tsdb-shipper-active/*

# Start Loki
brew services start loki
```

## Querying Logs

### In Grafana

1. Go to **Explore** → Select **Loki** datasource
2. Use LogQL queries:

```logql
# All logs
{job="spring-boot-prometheus"}

# Logs containing "cache"
{job="spring-boot-prometheus"} |= "cache"

# Logs from last hour
{job="spring-boot-prometheus"} |= "cache" [1h]

# Count cache hits
count_over_time({job="spring-boot-prometheus"} |= "cache hit" [5m])
```

### Via API

```bash
# Query logs
curl -G -s "http://localhost:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={job="spring-boot-prometheus"}' \
  --data-urlencode 'limit=10'

# Check Loki health
curl http://localhost:3100/ready

# Check metrics
curl http://localhost:3100/metrics
```

## Storage Best Practices

### 1. Monitor Disk Usage

```bash
# Check storage growth
watch -n 60 'du -sh loki/'

# Set up alerts if storage > 1GB
```

### 2. Adjust Retention Based on Needs

| Use Case | Retention | Reason |
|----------|-----------|--------|
| **Development** | 3-7 days | Quick debugging |
| **Staging** | 7-14 days | Testing cycles |
| **Production** | 30-90 days | Compliance, auditing |

### 3. Use Log Levels Wisely

```java
// Don't log everything at INFO level
logger.debug("Detailed debug info");  // Only in dev
logger.info("Important events");      // Production
logger.error("Errors");                // Always log
```

### 4. Compress Old Logs

Loki automatically compresses, but you can also:
```bash
# Compress application logs
gzip logs/spring-boot.log.2026-02-04
```

## Troubleshooting

### Logs Not Appearing in Grafana

**Check Promtail:**
```bash
brew services list | grep promtail
tail -f /usr/local/var/log/promtail.log
```

**Check Loki:**
```bash
brew services list | grep loki
curl http://localhost:3100/ready
```

**Check log file path:**
```bash
cat loki/promtail-config.yml | grep __path__
ls -lh logs/spring-boot.log
```

### Storage Growing Too Fast

**Reduce retention:**
```yaml
retention_period: 72h  # 3 days instead of 7
```

**Reduce log verbosity:**
```yaml
# application.yml
logging:
  level:
    com.example: WARN  # Instead of INFO
```

### Compactor Not Running

**Check config:**
```yaml
compactor:
  retention_enabled: true  # Must be true
```

**Restart Loki:**
```bash
brew services restart loki
```

## Summary

| Component | Storage Type | Location | Retention |
|-----------|-------------|----------|-----------|
| **Loki Chunks** | Filesystem | `loki/chunks/` | 7 days |
| **Loki WAL** | Filesystem | `loki/wal/` | Temporary |
| **Loki Index** | TSDB | `loki/tsdb-shipper-active/` | 7 days |
| **App Logs** | Text file | `logs/spring-boot.log` | Manual cleanup |

✅ **No traditional database required**
✅ **7-day retention configured**
✅ **Automatic cleanup enabled**
✅ **Filesystem-based storage**

Your logs are efficiently stored and automatically cleaned up! 📝
