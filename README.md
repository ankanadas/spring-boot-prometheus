# Spring Boot Prometheus Monitoring Demo

A complete Spring Boot application with Prometheus metrics and Grafana dashboards for monitoring.

## Features

- **Spring Boot REST API** with User CRUD operations
- **Prometheus metrics** integration with custom counters, timers, and gauges
- **Grafana dashboards** for visualization
- **H2 in-memory database** with sample data
- **Custom business metrics** for monitoring user operations

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Spring Boot   │───▶│   Prometheus    │───▶│    Grafana      │
│   Application   │    │   (Metrics)     │    │  (Dashboard)    │
│   Port: 8080    │    │   Port: 9090    │    │   Port: 3000    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Quick Start

### Prerequisites
- Java 17+ (you have Java 21 ✅)
- Homebrew (for installing Prometheus & Grafana)

### Step 1: Start Spring Boot Application
```bash
# Build and run the application
./mvnw clean package -DskipTests
java -jar target/metrics-demo-0.0.1-SNAPSHOT.jar
```

### Step 2: Install and Start Monitoring Tools
```bash
# Install Prometheus and Grafana
brew install prometheus grafana

# Start Prometheus (in new terminal)
prometheus --config.file=prometheus/prometheus.yml

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
- **Prometheus**: http://localhost:9090
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
3. **Create Dashboard**:
   - Click "+" → "Dashboard" → "Add visualization"
   - Select "Prometheus" data source
   - Use metrics like: `users_total`, `users_created_total`, `http_server_requests_seconds_count`

## Sample Grafana Queries

```promql
# Current user count
users_total

# Users created over time
rate(users_created_total[5m])

# API request rate
rate(http_server_requests_seconds_count[5m])

# Response time percentiles
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# JVM memory usage
jvm_memory_used_bytes{area="heap"}
```

## Generate Load for Testing
```bash
# Create multiple users
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/users \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"User $i\",\"email\":\"user$i@example.com\",\"department\":\"Test\"}"
done

# Generate traffic
for i in {1..100}; do
  curl http://localhost:8080/api/users
  curl http://localhost:8080/api/users/1
done
```

## Troubleshooting

### Common Issues

1. **Port conflicts**: Make sure ports 8080, 9090, and 3000 are available
2. **Metrics not showing in Grafana**: 
   - Check if Prometheus is scraping: http://localhost:9090/targets
   - Verify Spring Boot metrics: http://localhost:8080/actuator/prometheus
3. **Database connection**: Use H2 console at http://localhost:8080/h2-console

### Useful Commands
```bash
# Check if services are running
curl http://localhost:8080/actuator/health
curl http://localhost:9090/api/v1/targets
curl http://localhost:3000/api/health

# View specific metrics
curl http://localhost:8080/actuator/prometheus | grep users_

# Stop services
pkill prometheus
brew services stop grafana
```

## Project Structure

```
spring-boot-prometheus/
├── src/main/java/com/example/metricsdemo/
│   ├── MetricsDemoApplication.java      # Main application
│   ├── controller/UserController.java   # REST API with metrics
│   ├── model/User.java                  # User entity
│   ├── repository/UserRepository.java   # JPA repository
│   ├── service/UserService.java         # Business logic with metrics
│   └── config/DataInitializer.java      # Sample data loader
├── src/main/resources/
│   └── application.yml                  # Spring Boot configuration
├── prometheus/
│   └── prometheus.yml                   # Prometheus configuration
├── grafana/                            # Grafana configuration (for Docker)
├── pom.xml                             # Maven dependencies
└── README.md                           # This file
```

## Monitoring Best Practices

1. **Use appropriate metric types**:
   - Counters for things that only increase
   - Gauges for values that can go up and down
   - Timers for measuring duration

2. **Add meaningful labels** to metrics for better filtering

3. **Set up alerts** in Grafana for critical metrics

4. **Monitor key business metrics** alongside technical metrics

This project demonstrates a complete monitoring setup for Spring Boot applications using industry-standard tools.