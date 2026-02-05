# Spring Boot Prometheus Monitoring Demo

A complete Spring Boot application with Prometheus metrics, Grafana dashboards, Redis caching, and Loki logging for comprehensive monitoring.

## Features

- **Spring Boot REST API** with User CRUD operations
- **Redis caching** with 24-hour TTL for improved performance
- **Prometheus metrics** integration with custom counters, timers, and gauges
- **Grafana dashboards** for visualization
- **Loki log aggregation** with Promtail for centralized logging
- **H2 in-memory database** with sample data
- **Custom business metrics** for monitoring user operations and cache performance

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Spring Boot   │───▶│   Prometheus    │───▶│    Grafana      │
│   Application   │    │   (Metrics)     │    │  (Dashboard)    │
│   Port: 8080    │    │   Port: 9090    │    │   Port: 3000    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                                             ▲
         ▼                                             │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Redis       │    │   Promtail      │───▶│      Loki       │
│   (Caching)     │    │ (Log Collector) │    │   (Logs)        │
│   Port: 6379    │    │   Port: 9080    │    │   Port: 3100    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Quick Start

### Prerequisites
- Java 17+ (you have Java 21 ✅)
- Homebrew (for installing Prometheus, Grafana, Redis, Loki, and Promtail)

### Step 1: Start Spring Boot Application
```bash
# Build and run the application
./mvnw clean package -DskipTests
java -jar target/metrics-demo-0.0.1-SNAPSHOT.jar
```

### Step 2: Install and Start All Services
```bash
# Install all monitoring tools
brew install prometheus grafana redis loki promtail

# Start Redis
brew services start redis

# Start Prometheus (in new terminal)
prometheus --config.file=prometheus/prometheus.yml

# Start Loki (in another terminal)
loki -config.file=loki/loki-config.yml

# Start Promtail (in another terminal)
promtail -config.file=loki/promtail-config.yml

# Start Grafana (in another terminal)
grafana-server --config=/usr/local/etc/grafana/grafana.ini --homepath /usr/local/share/grafana
```

### Step 3: Access the Services
- **Spring Boot App**: http://localhost:8080
- **API Endpoints**: http://localhost:8080/api/users
- **Metrics**: http://localhost:8080/actuator/prometheus
- **Health Check**: http://localhost:8080/actuator/health
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: `password`
- **Redis**: localhost:6379 (use `redis-cli` to connect)
- **Prometheus**: http://localhost:9090
- **Loki**: http://localhost:3100 (API only, no web UI)
- **Grafana**: http://localhost:3000 (admin/admin)

## API Endpoints

### User Management
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Health & Monitoring
- `GET /api/users/health` - Health check
- `GET /api/users/slow` - Slow endpoint (for testing)
- `GET /actuator/health` - Spring Boot health
- `GET /actuator/metrics` - All metrics
- `GET /actuator/prometheus` - Prometheus metrics

## Sample API Calls

### Create a User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "department": "Engineering"
  }'
```

### Get All Users
```bash
curl http://localhost:8080/api/users
```

### Test Slow Endpoint
```bash
curl http://localhost:8080/api/users/slow
```

## Custom Metrics

The application exposes several custom metrics:

### Counters
- `users_created_total` - Total number of users created
- `users_retrieved_total` - Total number of user retrievals
- `user_cache_hits_total` - Total number of Redis cache hits
- `user_cache_misses_total` - Total number of Redis cache misses
- `create_user_count` - Number of create user API calls
- `get_users_count` - Number of get all users API calls

### Timers
- `get_users_duration` - Time taken to get all users
- `get_user_by_id_duration` - Time taken to get user by ID
- `create_user_duration` - Time taken to create a user
- `update_user_duration` - Time taken to update a user
- `delete_user_duration` - Time taken to delete a user
- `slow_endpoint_duration` - Time taken for slow endpoint

### Gauges
- `users_total` - Current total number of users in the system

## Setting Up Grafana Dashboard

1. **Access Grafana**: http://localhost:3000 (admin/admin)
2. **Add Prometheus Data Source**: 
   - URL: `http://localhost:9090`
   - Click "Save & Test"
3. **Add Loki Data Source**:
   - URL: `http://localhost:3100`
   - Click "Save & Test"
4. **Create Dashboard**:
   - Click "+" → "Dashboard" → "Add visualization"
   - Select "Prometheus" data source for metrics
   - Select "Loki" data source for logs

## Redis Caching Workflow

The application implements a Redis caching layer with the following behavior:

### Cache Strategy
- **Cache Key Pattern**: `user:{id}` (e.g., `user:1`, `user:2`)
- **TTL**: 24 hours (86400 seconds)
- **Cache-Aside Pattern**: Check cache first, then database if miss

### Cache Flow
1. **GET /api/users/{id}**:
   - Check Redis for `user:{id}`
   - **Cache HIT**: Return user from Redis (fast)
   - **Cache MISS**: Fetch from H2 database, cache result, return user

2. **POST /api/users** (Create):
   - Save user to H2 database
   - Cache the new user in Redis

3. **PUT /api/users/{id}** (Update):
   - Update user in H2 database
   - Update cache with new user data

4. **DELETE /api/users/{id}**:
   - Delete user from H2 database
   - Remove user from Redis cache

### Cache Monitoring
- `user_cache_hits_total` - Successful cache retrievals
- `user_cache_misses_total` - Cache misses (database queries)
- Cache hit ratio = hits / (hits + misses)

### Redis Commands for Testing
```bash
# Connect to Redis
redis-cli

# View all cached users
KEYS user:*

# Get specific user from cache
GET user:1

# Check TTL (time to live)
TTL user:1

# Clear all cache
FLUSHALL
```

## Log Aggregation with Loki

### Log Sources
- **Application Logs**: `logs/spring-boot.log`
- **Log Format**: `timestamp [thread] level logger - message`
- **Collection**: Promtail reads log file and sends to Loki

### Log Queries in Grafana
```logql
# All application logs
{job="spring-boot-prometheus"}

# Cache-related logs only
{job="spring-boot-prometheus"} |= "Cache"

# Database fetch logs
{job="spring-boot-prometheus"} |= "Fetching user"

# Error logs
{job="spring-boot-prometheus"} |= "ERROR"
```

### Sample Log Entries
```
2026-02-05 11:45:05.835 [http-nio-8080-exec-5] INFO c.e.m.service.UserCacheService - Cache MISS - User 1 not found in Redis
2026-02-05 11:45:05.837 [http-nio-8080-exec-5] INFO c.e.metricsdemo.service.UserService - Fetching user 1 from H2 database
2026-02-05 11:45:10.123 [http-nio-8080-exec-6] INFO c.e.m.service.UserCacheService - Cache HIT - Fetching user 1 from Redis
```

## Sample Grafana Queries

### Prometheus Metrics
```promql
# Current user count
users_total

# Users created over time
rate(users_created_total[5m])

# Cache hit ratio
user_cache_hits_total / (user_cache_hits_total + user_cache_misses_total)

# Cache performance
rate(user_cache_hits_total[5m])
rate(user_cache_misses_total[5m])

# API request rate
rate(http_server_requests_seconds_count[5m])

# Response time percentiles
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# JVM memory usage
jvm_memory_used_bytes{area="heap"}
```

### Loki Log Queries
```logql
# All application logs
{job="spring-boot-prometheus"}

# Cache performance logs
{job="spring-boot-prometheus"} |= "Cache"

# Database queries
{job="spring-boot-prometheus"} |= "Fetching user"

# Error logs
{job="spring-boot-prometheus"} |= "ERROR"
```

## Generate Load for Testing
```bash
# Clear Redis cache to test cache misses
redis-cli FLUSHALL

# Create multiple users (will be cached)
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/users \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"User $i\",\"email\":\"user$i@example.com\",\"department\":\"Test\"}"
done

# Test cache behavior
curl http://localhost:8080/api/users/1  # Cache miss (first call)
curl http://localhost:8080/api/users/1  # Cache hit (second call)

# Generate traffic to see metrics
for i in {1..100}; do
  curl http://localhost:8080/api/users
  curl http://localhost:8080/api/users/1
done
```

## Troubleshooting

### Common Issues

1. **Port conflicts**: Make sure ports 8080, 9090, 3000, 6379, and 3100 are available
2. **Metrics not showing in Grafana**: 
   - Check if Prometheus is scraping: http://localhost:9090/targets
   - Verify Spring Boot metrics: http://localhost:8080/actuator/prometheus
3. **Logs not appearing in Loki**:
   - Check if Promtail is running: `ps aux | grep promtail`
   - Verify log file exists: `ls -la logs/spring-boot.log`
   - Test Loki directly: `curl http://localhost:3100/ready`
4. **Redis connection issues**:
   - Check if Redis is running: `brew services list | grep redis`
   - Test connection: `redis-cli ping`
5. **Database connection**: Use H2 console at http://localhost:8080/h2-console

### Useful Commands
```bash
# Check if all services are running
curl http://localhost:8080/actuator/health
curl http://localhost:9090/api/v1/targets
curl http://localhost:3000/api/health
curl http://localhost:3100/ready
redis-cli ping

# View specific metrics
curl http://localhost:8080/actuator/prometheus | grep users_
curl http://localhost:8080/actuator/prometheus | grep cache_

# Redis operations
redis-cli KEYS "user:*"
redis-cli GET user:1
redis-cli FLUSHALL

# View recent logs
tail -f logs/spring-boot.log

# Stop services
pkill prometheus
pkill loki
pkill promtail
brew services stop grafana
brew services stop redis
```

## Project Structure

```
spring-boot-prometheus/
├── src/main/java/com/example/metricsdemo/
│   ├── MetricsDemoApplication.java      # Main application
│   ├── controller/UserController.java   # REST API with metrics
│   ├── model/User.java                  # User entity
│   ├── repository/UserRepository.java   # JPA repository
│   ├── service/
│   │   ├── UserService.java             # Business logic with metrics
│   │   └── UserCacheService.java        # Redis caching service
│   └── config/
│       ├── DataInitializer.java         # Sample data loader
│       └── RedisConfig.java             # Redis configuration
├── src/main/resources/
│   └── application.yml                  # Spring Boot configuration
├── prometheus/
│   └── prometheus.yml                   # Prometheus configuration
├── loki/
│   ├── loki-config.yml                  # Loki configuration
│   └── promtail-config.yml              # Promtail configuration
├── grafana/                            # Grafana configuration
├── logs/                               # Application logs
│   └── spring-boot.log                 # Log file (auto-generated)
├── pom.xml                             # Maven dependencies
└── README.md                           # This file
```

## Monitoring Best Practices

1. **Use appropriate metric types**:
   - Counters for things that only increase (cache hits, user creations)
   - Gauges for values that can go up and down (current user count)
   - Timers for measuring duration (API response times)

2. **Implement effective caching**:
   - Use Redis for frequently accessed data
   - Set appropriate TTL values (24 hours for user data)
   - Monitor cache hit ratios to optimize performance

3. **Centralized logging**:
   - Use structured logging with consistent formats
   - Aggregate logs with Loki for searchability
   - Include correlation IDs for request tracing

4. **Add meaningful labels** to metrics for better filtering

5. **Set up alerts** in Grafana for critical metrics:
   - High cache miss ratio
   - Slow API response times
   - Error rate thresholds

6. **Monitor key business metrics** alongside technical metrics

This project demonstrates a complete observability stack for Spring Boot applications using industry-standard tools including metrics, logging, and caching.