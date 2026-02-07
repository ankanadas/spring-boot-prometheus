# Kibana Guide - Elasticsearch Debugging UI

## What is Kibana?

Kibana is the **visual interface for Elasticsearch**. It's like pgAdmin for PostgreSQL or phpMyAdmin for MySQL - a UI to:
- Browse and search your data
- Run queries and see results
- Debug search queries
- Visualize data with charts
- Monitor Elasticsearch health

## Installation & Startup

### Install
```bash
brew install elastic/tap/kibana-full
```

### Start
```bash
# Using the startup script
./start-kibana.sh

# Or manually
brew services start elastic/tap/kibana-full
```

### Access
```
http://localhost:5601
```

**Note:** Kibana takes 30-60 seconds to start. Be patient!

## First Time Setup

1. Open http://localhost:5601
2. Click **"Explore on my own"** (skip the tutorial)
3. You're in!

## Key Features for Debugging

### 1. Dev Tools (Query Console)

**Location:** Click the wrench icon (🔧) in the left sidebar

This is like SQL console but for Elasticsearch queries.

#### Check if index exists
```json
GET /users
```

#### Count documents
```json
GET /users/_count
```

#### View all documents
```json
GET /users/_search
{
  "query": {
    "match_all": {}
  }
}
```

#### Search with fuzzy matching
```json
GET /users/_search
{
  "query": {
    "multi_match": {
      "query": "jhon",
      "fields": ["name^2", "email", "departmentName"],
      "fuzziness": "AUTO"
    }
  }
}
```

#### View specific document
```json
GET /users/_doc/1
```

#### Search by name
```json
GET /users/_search
{
  "query": {
    "match": {
      "name": "john"
    }
  }
}
```

#### Fuzzy search with explanation
```json
GET /users/_search
{
  "query": {
    "fuzzy": {
      "name": {
        "value": "jhon",
        "fuzziness": "AUTO"
      }
    }
  },
  "explain": true
}
```

### 2. Discover (Browse Data)

**Location:** Click the compass icon (🧭) in the left sidebar

#### First Time Setup:
1. Click "Create data view"
2. Name: `users`
3. Index pattern: `users`
4. Click "Save data view to Kibana"

#### Browse Your Data:
- See all 18 users in a table
- Filter by fields (name, email, department)
- Search with KQL (Kibana Query Language)
- View individual documents

#### Example Searches:
```
name: "John"
email: *@example.com
departmentName: "Engineering"
```

### 3. Index Management

**Location:** Menu → Stack Management → Index Management

- View all indices
- Check index health
- See document count
- View index settings
- Delete indices (careful!)

### 4. Monitoring

**Location:** Menu → Stack Management → Stack Monitoring

- Elasticsearch cluster health
- Node statistics
- Index statistics
- Query performance

## Common Debugging Tasks

### Task 1: Check if users are indexed

**Dev Tools:**
```json
GET /users/_count
```

**Expected Result:**
```json
{
  "count": 18,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  }
}
```

### Task 2: View a specific user

**Dev Tools:**
```json
GET /users/_doc/1
```

**Expected Result:**
```json
{
  "_index": "users",
  "_id": "1",
  "_version": 1,
  "_source": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "departmentName": "Engineering"
  }
}
```

### Task 3: Test fuzzy search

**Dev Tools:**
```json
GET /users/_search
{
  "query": {
    "multi_match": {
      "query": "jhon",
      "fields": ["name^2", "email", "departmentName"],
      "fuzziness": "AUTO"
    }
  }
}
```

**Expected Result:**
Should find "John Doe" even though you typed "jhon"

### Task 4: See why a result matched

**Dev Tools:**
```json
GET /users/_search
{
  "query": {
    "match": {
      "name": "john"
    }
  },
  "explain": true
}
```

This shows the **relevance score calculation** - why this document matched and how the score was computed.

### Task 5: View index mapping

**Dev Tools:**
```json
GET /users/_mapping
```

Shows how fields are indexed (text, keyword, etc.)

### Task 6: Delete and recreate index

**Dev Tools:**
```json
# Delete index
DELETE /users

# Recreate by reindexing from your app
# Call: POST http://localhost:8080/api/users/reindex
```

### Task 7: Search across all fields

**Dev Tools:**
```json
GET /users/_search
{
  "query": {
    "query_string": {
      "query": "engineering OR john"
    }
  }
}
```

### Task 8: Aggregate by department

**Dev Tools:**
```json
GET /users/_search
{
  "size": 0,
  "aggs": {
    "departments": {
      "terms": {
        "field": "departmentName.keyword"
      }
    }
  }
}
```

Shows count of users per department.

## Useful Queries for Your App

### Find all Engineering users
```json
GET /users/_search
{
  "query": {
    "match": {
      "departmentName": "Engineering"
    }
  }
}
```

### Find users with @example.com email
```json
GET /users/_search
{
  "query": {
    "wildcard": {
      "email": "*@example.com"
    }
  }
}
```

### Find users with typo in name
```json
GET /users/_search
{
  "query": {
    "fuzzy": {
      "name": {
        "value": "tonny",
        "fuzziness": 1
      }
    }
  }
}
```

### Search with highlighting
```json
GET /users/_search
{
  "query": {
    "match": {
      "name": "john"
    }
  },
  "highlight": {
    "fields": {
      "name": {}
    }
  }
}
```

Shows matched terms in bold.

### Get top 3 users by relevance
```json
GET /users/_search
{
  "size": 3,
  "query": {
    "multi_match": {
      "query": "engineering",
      "fields": ["name^2", "email", "departmentName"]
    }
  },
  "sort": [
    "_score"
  ]
}
```

## Kibana Query Language (KQL)

In the Discover tab, you can use KQL for quick searches:

```
# Find by name
name: "John"

# Find by email domain
email: *@example.com

# Find by department
departmentName: "Engineering"

# Combine conditions (AND)
name: "John" and departmentName: "Engineering"

# Combine conditions (OR)
departmentName: "Engineering" or departmentName: "Marketing"

# Exclude
not departmentName: "Engineering"

# Wildcard
name: John*
```

## Troubleshooting

### Kibana won't start
```bash
# Check if Elasticsearch is running
curl http://localhost:9200

# Restart Kibana
brew services restart elastic/tap/kibana-full

# Check logs
tail -f /usr/local/var/log/kibana/kibana.log
```

### Can't see users index
```bash
# Check if index exists
curl http://localhost:9200/_cat/indices

# Reindex from your app
curl -X POST http://localhost:8080/api/users/reindex
```

### Queries return no results
1. Check document count: `GET /users/_count`
2. View all documents: `GET /users/_search`
3. Check your query syntax
4. Try `match_all` query first

### Connection refused
- Kibana takes 30-60 seconds to start
- Wait and try again
- Check if port 5601 is available: `lsof -i :5601`

## Best Practices

### 1. Use Dev Tools for Testing
Before implementing a query in your app, test it in Dev Tools first.

### 2. Use Explain for Debugging
Add `"explain": true` to understand why results match or don't match.

### 3. Monitor Index Size
Check index size regularly:
```json
GET /_cat/indices/users?v
```

### 4. Use Filters for Exact Matches
```json
{
  "query": {
    "bool": {
      "filter": [
        {"term": {"departmentName.keyword": "Engineering"}}
      ]
    }
  }
}
```

### 5. Limit Result Size
Always use `"size"` parameter to avoid loading too many results:
```json
GET /users/_search
{
  "size": 10,
  "query": {...}
}
```

## Keyboard Shortcuts (Dev Tools)

- `Ctrl/Cmd + Enter` - Run query
- `Ctrl/Cmd + I` - Auto-indent
- `Ctrl/Cmd + /` - Comment/uncomment
- `Ctrl/Cmd + Option + L` - Format JSON

## Quick Reference

### Essential Endpoints
```
http://localhost:5601              - Kibana home
http://localhost:5601/app/dev_tools - Dev Tools (query console)
http://localhost:5601/app/discover  - Discover (browse data)
http://localhost:9200              - Elasticsearch (direct access)
```

### Essential Commands
```bash
# Start Kibana
./start-kibana.sh

# Stop Kibana
brew services stop elastic/tap/kibana-full

# Restart Kibana
brew services restart elastic/tap/kibana-full

# Check status
brew services list | grep kibana
```

## Summary

Kibana is your **debugging companion** for Elasticsearch:
- ✅ Visual interface for queries
- ✅ Browse and search data easily
- ✅ Test queries before coding
- ✅ Debug why searches work or don't work
- ✅ Monitor Elasticsearch health

**Access it at:** http://localhost:5601

**Most useful feature:** Dev Tools (🔧) - Run queries and see results instantly!
