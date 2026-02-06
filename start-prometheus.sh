#!/bin/bash

# Start Prometheus with proper retention and data directory
echo "Starting Prometheus with 7-day data retention..."

# Create data directory if it doesn't exist
mkdir -p prometheus/data

# Stop any existing Prometheus instance
pkill -f "prometheus.*9090" 2>/dev/null

# Start Prometheus
prometheus \
  --config.file=prometheus/prometheus.yml \
  --storage.tsdb.path=prometheus/data \
  --storage.tsdb.retention.time=7d \
  --web.listen-address=:9090 \
  --web.enable-lifecycle \
  > prometheus/prometheus.log 2>&1 &

PROM_PID=$!
echo "Prometheus started with PID: $PROM_PID"
echo "Data directory: prometheus/data"
echo "Retention: 7 days"
echo "Logs: prometheus/prometheus.log"
echo ""
echo "Access Prometheus at: http://localhost:9090"
