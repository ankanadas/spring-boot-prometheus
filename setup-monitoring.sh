#!/bin/bash

echo "🔧 Setting up Prometheus and Grafana for Spring Boot Prometheus Demo..."

# Check if Homebrew is installed
if ! command -v brew &> /dev/null; then
    echo "❌ Homebrew not found. Please install Homebrew first:"
    echo "   /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
    exit 1
fi

echo "📊 Installing Prometheus..."
if brew list prometheus &>/dev/null; then
    echo "✅ Prometheus already installed"
else
    brew install prometheus
fi

echo "📈 Installing Grafana..."
if brew list grafana &>/dev/null; then
    echo "✅ Grafana already installed"
else
    brew install grafana
fi

echo ""
echo "🚀 Starting services..."

# Start Prometheus in background
echo "Starting Prometheus..."
prometheus --config.file=prometheus/prometheus.yml &
PROMETHEUS_PID=$!
echo "Prometheus PID: $PROMETHEUS_PID"

# Wait a bit for Prometheus to start
sleep 3

# Start Grafana in background
echo "Starting Grafana..."
brew services start grafana

echo ""
echo "✅ Setup complete!"
echo ""
echo "🌐 Access URLs:"
echo "   Spring Boot App: http://localhost:8080"
echo "   Prometheus:      http://localhost:9090"
echo "   Grafana:         http://localhost:3000 (admin/admin)"
echo ""
echo "📋 Next steps:"
echo "1. Open Grafana at http://localhost:3000"
echo "2. Login with admin/admin"
echo "3. Add Prometheus data source: http://localhost:9090"
echo "4. Import or create dashboards"
echo ""
echo "🛑 To stop services:"
echo "   kill $PROMETHEUS_PID"
echo "   brew services stop grafana"