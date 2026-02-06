# Elasticsearch Fuzzy Search Setup

## What is Fuzzy Search?

Fuzzy search finds results even when you make typos or spelling mistakes. It's much smarter than regular database LIKE queries.

## Examples

**Regular Search (PostgreSQL):**
- Search: "Jhon" → No results ❌
- Search: "engneering" → No results ❌

**Fuzzy Search (Elasticsearch):**
- Search: "Jhon" → Finds "John Doe" ✅
- Search: "engneering" → Finds "Engineering" ✅
- Search: "clark" → Finds "Clark Kent" ✅

## Installation

Elasticsearch is installed via Homebrew:
```bash
brew tap elastic/tap
brew install elastic/tap/elasticsearch-full
```

## Starting Elasticsearch

```bash
# Start Elasticsearch
export ES_JAVA_HOME=$(/usr/libexec/java_home)
/usr/local/opt/elasticsearch-full/bin/elasticsearch -d

# Check if running
curl http://localhost:9200
```

## API Endpoints

### Search with Fuzzy Matching (Default)
```bash
GET /api/users/search?query=jhon&page=0&size=5
```
- **Now uses Elasticsearch fuzzy search!**
- Handles typos automatically
- Searches name, email, and department
- Your UI automatically gets fuzzy search

### Legacy Fuzzy Search Endpoint
```bash
GET /api/users/fuzzy-search?query=jhon&page=0&size=5
```
- Same as above, kept for backward compatibility
- Returns raw Elasticsearch documents

## How It Works

1. **Indexing**: When you create/update/delete a user, it's automatically synced to Elasticsearch
2. **Searching**: Fuzzy search uses Elasticsearch's multi-match query with fuzziness
3. **Scoring**: Results are ranked by relevance (name matches score higher than email)

## Configuration

**application.yml:**
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
```

## Data Sync

- ✅ Create user → Indexed in Elasticsearch
- ✅ Update user → Re-indexed in Elasticsearch
- ✅ Delete user → Removed from Elasticsearch
- ✅ Existing users → Auto-indexed on startup

## Testing

1. Start Elasticsearch
2. Start your Spring Boot app
3. **Your existing UI search now has fuzzy matching!**

Try these searches in your web UI:
- Type "jhon" → Finds "John Doe" ✅
- Type "engneering" → Finds users in "Engineering" ✅
- Type "clark" → Finds "Clark Kent" ✅

Or test via API:
```bash
# Typo in name
curl "http://localhost:8080/api/users/search?query=jhon"

# Typo in department
curl "http://localhost:8080/api/users/search?query=engneering"

# Partial match
curl "http://localhost:8080/api/users/search?query=clark"
```

## Swagger UI

Access fuzzy search endpoint at:
```
http://localhost:8080/swagger-ui.html
```

Look for `/api/users/fuzzy-search` endpoint.

## Architecture

```
User Action (Create/Update/Delete)
    ↓
PostgreSQL (Source of Truth)
    ↓
Redis Cache (24h TTL)
    ↓
Elasticsearch Index (Search)
```

## Troubleshooting

**Elasticsearch not starting:**
```bash
# Check if Java is available
java -version

# Set JAVA_HOME
export ES_JAVA_HOME=$(/usr/libexec/java_home)

# Start manually
/usr/local/opt/elasticsearch-full/bin/elasticsearch
```

**Index not created:**
```bash
# Check Elasticsearch is running
curl http://localhost:9200

# Check indices
curl http://localhost:9200/_cat/indices
```

**Users not indexed:**
- Check application logs for "Indexing users in Elasticsearch"
- Restart the app to trigger reindexing

## Performance

- **PostgreSQL LIKE**: Slow on large datasets, no typo tolerance
- **Elasticsearch**: Fast full-text search, handles typos, scales well

For 18 users, both are fast. For 10,000+ users, Elasticsearch is significantly better.
