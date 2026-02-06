#!/bin/bash

# Start Elasticsearch with proper Java configuration

echo "Starting Elasticsearch..."

# Set Java home
export ES_JAVA_HOME=$(/usr/libexec/java_home)

# Check if Elasticsearch is already running
if curl -s http://localhost:9200 > /dev/null 2>&1; then
    echo "✅ Elasticsearch is already running on port 9200"
    curl -s http://localhost:9200 | grep -o '"number" : "[^"]*"'
    exit 0
fi

# Start Elasticsearch in background
/usr/local/opt/elasticsearch-full/bin/elasticsearch -d

echo "Waiting for Elasticsearch to start..."
for i in {1..30}; do
    if curl -s http://localhost:9200 > /dev/null 2>&1; then
        echo "✅ Elasticsearch started successfully!"
        echo ""
        curl -s http://localhost:9200 | python3 -m json.tool 2>/dev/null || curl -s http://localhost:9200
        echo ""
        echo "Access Elasticsearch at: http://localhost:9200"
        exit 0
    fi
    sleep 1
done

echo "❌ Elasticsearch failed to start within 30 seconds"
echo "Check logs at: /usr/local/var/log/elasticsearch/"
exit 1
