#!/bin/bash

echo "🚀 Starting all services for User Management System..."
echo ""

# Start Elasticsearch
echo "1️⃣ Starting Elasticsearch..."
./start-elasticsearch.sh
if [ $? -ne 0 ]; then
    echo "❌ Failed to start Elasticsearch"
    exit 1
fi
echo ""

# Start Prometheus
echo "2️⃣ Starting Prometheus..."
./start-prometheus.sh
if [ $? -ne 0 ]; then
    echo "❌ Failed to start Prometheus"
    exit 1
fi
echo ""

# Check Redis
echo "3️⃣ Checking Redis..."
if redis-cli ping > /dev/null 2>&1; then
    echo "✅ Redis is running"
else
    echo "⚠️  Redis is not running. Start it with: brew services start redis"
fi
echo ""

# Check PostgreSQL
echo "4️⃣ Checking PostgreSQL..."
if pg_isready -U userapp > /dev/null 2>&1; then
    echo "✅ PostgreSQL is running"
else
    echo "⚠️  PostgreSQL is not running. Start it with: brew services start postgresql@15"
fi
echo ""

# Check Grafana
echo "5️⃣ Checking Grafana..."
if curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ Grafana is running"
else
    echo "⚠️  Grafana is not running. Start it with: brew services start grafana"
fi
echo ""

# Check Loki
echo "6️⃣ Checking Loki..."
if curl -s http://localhost:3100/ready > /dev/null 2>&1; then
    echo "✅ Loki is running"
else
    echo "⚠️  Loki is not running. Start it with: brew services start loki"
fi
echo ""

echo "✅ All core services are ready!"
echo ""
echo "📋 Service URLs:"
echo "   - Application:    http://localhost:8080"
echo "   - Swagger UI:     http://localhost:8080/swagger-ui.html"
echo "   - Prometheus:     http://localhost:9090"
echo "   - Grafana:        http://localhost:3000"
echo "   - Elasticsearch:  http://localhost:9200"
echo ""
echo "🎯 Now start the Spring Boot app with: ./run-app.sh"
