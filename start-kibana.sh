#!/bin/bash

echo "Starting Kibana..."

# Check if Kibana is already running
if curl -s http://localhost:5601/api/status > /dev/null 2>&1; then
    echo "✅ Kibana is already running on port 5601"
    echo "Access Kibana at: http://localhost:5601"
    exit 0
fi

# Start Kibana
brew services start elastic/tap/kibana-full

echo "Waiting for Kibana to start (this takes 30-60 seconds)..."
for i in {1..60}; do
    if curl -s http://localhost:5601/api/status > /dev/null 2>&1; then
        echo ""
        echo "✅ Kibana started successfully!"
        echo ""
        echo "Access Kibana at: http://localhost:5601"
        echo ""
        echo "Quick Start:"
        echo "1. Open http://localhost:5601"
        echo "2. Click 'Explore on my own'"
        echo "3. Go to Dev Tools (wrench icon) to run queries"
        echo "4. Go to Discover to browse your data"
        exit 0
    fi
    echo -n "."
    sleep 1
done

echo ""
echo "⚠️  Kibana is taking longer than expected to start"
echo "Check logs at: /usr/local/var/log/kibana/"
echo "Or try: brew services restart elastic/tap/kibana-full"
exit 1
