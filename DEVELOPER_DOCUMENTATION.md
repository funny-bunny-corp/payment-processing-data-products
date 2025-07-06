# Payment Processing Data Products - Developer Documentation

## 1. OVERVIEW

### Purpose and Primary Functionality

The Payment Processing Data Products module is a microservice designed to handle user transaction data analytics and aggregation for the Paymentic payment platform. It provides real-time access to user transaction metrics including monthly averages and last transaction details.

**Core Capabilities:**
- **Transaction Analytics**: Calculates monthly transaction averages for users
- **Transaction History**: Retrieves last transaction details for users
- **Event Processing**: Consumes payment order events from Kafka for real-time data updates
- **Data Aggregation**: Stores and aggregates transaction data for analytical purposes

### When to Use This Component vs. Alternatives

**Use this component when:**
- You need real-time transaction analytics and aggregation
- You require user-specific transaction data queries
- You're building payment analytics dashboards
- You need to integrate with event-driven payment processing systems

**Consider alternatives when:**
- You need real-time transaction processing (use core payment service)
- You require complex financial reporting (use dedicated BI tools)
- You need transaction modification capabilities (use transactional services)

### Architectural Context

This module fits into the Paymentic ecosystem as a **data analytics service** that:
- Consumes events from the payment processing core system via Kafka
- Stores aggregated transaction data in MongoDB
- Exposes analytics data via gRPC API
- Follows **Hexagonal Architecture** principles for maintainability and testability

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Payment Core  │────│  Kafka Events   │────│  This Module    │
│    Service      │    │                 │    │ (Analytics API) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        │
                                              ┌─────────────────┐
                                              │    MongoDB      │
                                              │ (Aggregated     │
                                              │   Data)         │
                                              └─────────────────┘
```

## 2. TECHNICAL SPECIFICATION

### Framework and Technologies

- **Framework**: Quarkus 3.13.2 (Supersonic Subatomic Java Framework)
- **Language**: Java 21
- **Architecture**: Hexagonal Architecture (Ports & Adapters)
- **Messaging**: Apache Kafka with CloudEvents
- **Database**: MongoDB
- **API Protocol**: gRPC
- **Event Format**: CloudEvents 2.5.0

### API Reference

#### gRPC Service: `UserTransactionsService`

**Service Definition Location**: `src/main/proto/payment-processing.proto`

##### Methods

**1. GetUserMonthAverage**
```protobuf
rpc GetUserMonthAverage (UserMonthAverageRequest) returns (UserMonthAverageResponse) {}
```

**Parameters:**
- `UserMonthAverageRequest`
  - `document` (string): User document/ID
  - `month` (string): Month in format "yyyy-MM-dd HH:mm:ss.SSSSSS"

**Returns:**
- `UserMonthAverageResponse`
  - `month` (string): The queried month
  - `document` (string): User document/ID
  - `total` (string): Monthly average transaction amount

**2. GetLastUserTransaction**
```protobuf
rpc GetLastUserTransaction (LastUserTransactionRequest) returns (LastUserTransactionResponse) {}
```

**Parameters:**
- `LastUserTransactionRequest`
  - `document` (string): User document/ID

**Returns:**
- `LastUserTransactionResponse`
  - `document` (string): User document/ID
  - `sellerId` (string): Seller identifier
  - `currency` (string): Transaction currency
  - `value` (string): Transaction amount

### Core Domain Models

#### UserTransaction
```java
public class UserTransaction {
    private String id;
    private String document;
    private BigDecimal value;
    private CheckoutId checkout;
    private Payment payment;
    private String currency;
    private String at;
    private Seller seller;
}
```

#### AverageTransactionsValue
```java
public class AverageTransactionsValue {
    private String document;
    private BigDecimal average;
    
    public static AverageTransactionsValue defaultAverage(String document) {
        return new AverageTransactionsValue(document, new BigDecimal("200.00"));
    }
}
```

#### LastUserTransaction
```java
public class LastUserTransaction {
    private String document;
    private BigDecimal value;
    private String currency;
    private String sellerId;
}
```

### Event Processing

#### Kafka Consumer Configuration

**Topic**: `order-started`
**Event Type**: `paymentic.io.payment-processing.v1.payment-order.approved`
**Processing Mode**: Blocking

**Event Structure**:
```java
@ApplicationScoped
public class OrderStartedProcessor {
    @Blocking
    @Incoming("order-started")
    public CompletionStage<Void> process(Message<TransactionDetails> message);
}
```

### State Management

- **Persistence**: MongoDB with reactive client
- **Transaction State**: Event-sourced from Kafka messages
- **Aggregations**: Pre-calculated and stored for performance
- **Default Values**: System provides default averages (200.00) when no data exists

### Architecture Layers

#### Domain Layer (`com.paymentic.domain`)
- **Entities**: UserTransaction, AverageTransactionsValue, LastUserTransaction
- **Services**: UserTransactionService (Application Service)
- **Repositories**: UserTransactionRepository (Interface)

#### Infrastructure Layer (`com.paymentic.infra`)
- **MongoDB**: Database persistence implementation
- **Kafka**: Message broker configuration

#### Adapters Layer (`com.paymentic.adapter`)
- **gRPC**: Input adapter for API requests
- **Kafka**: Input adapter for event processing
- **MongoDB**: Output adapter for data persistence

## 3. IMPLEMENTATION EXAMPLES

### Basic Usage Example

#### 1. Setting up the Development Environment

```bash
# Clone the repository
git clone <repository-url>
cd payment-processing-data-products

# Run in development mode
./mvnw compile quarkus:dev

# Access Dev UI at http://localhost:8080/q/dev/
```

#### 2. gRPC Client Implementation

```java
@RegisterForReflection
public class PaymentAnalyticsClient {
    
    @Inject
    @GrpcClient("payment-analytics")
    UserTransactionsService client;
    
    public UserMonthAverageResponse getUserMonthlyAverage(String document, String month) {
        UserMonthAverageRequest request = UserMonthAverageRequest.newBuilder()
            .setDocument(document)
            .setMonth(month)
            .build();
            
        return client.getUserMonthAverage(request).await().indefinitely();
    }
    
    public LastUserTransactionResponse getLastTransaction(String document) {
        LastUserTransactionRequest request = LastUserTransactionRequest.newBuilder()
            .setDocument(document)
            .build();
            
        return client.getLastUserTransaction(request).await().indefinitely();
    }
}
```

### Advanced Configuration Example

#### 1. Custom Repository Implementation

```java
@ApplicationScoped
public class MongoUserTransactionRepository implements UserTransactionRepository {
    
    @Inject
    ReactiveMongoClient mongoClient;
    
    @Override
    public void add(UserTransaction transaction) {
        ReactiveMongoCollection<Document> collection = getCollection();
        Document doc = new Document()
            .append("document", transaction.getDocument())
            .append("value", transaction.getValue())
            .append("currency", transaction.getCurrency())
            .append("at", transaction.getAt())
            .append("sellerId", transaction.getSeller().getId());
            
        collection.insertOne(doc).await().indefinitely();
    }
    
    @Override
    public AverageTransactionsValue totalPerMonth(String document, LocalDate at) {
        // Implementation with MongoDB aggregation pipeline
        List<Document> pipeline = Arrays.asList(
            new Document("$match", new Document("document", document)
                .append("at", new Document("$regex", "^" + at.getYear() + "-" + at.getMonthValue()))),
            new Document("$group", new Document("_id", "$document")
                .append("average", new Document("$avg", "$value")))
        );
        
        Document result = getCollection().aggregate(pipeline).first().await().indefinitely();
        
        if (result != null) {
            return new AverageTransactionsValue(
                result.getString("_id"),
                new BigDecimal(result.getDouble("average").toString())
            );
        }
        
        return null;
    }
}
```

#### 2. Custom Event Processing

```java
@ApplicationScoped
public class CustomOrderProcessor {
    
    @Inject
    Event<UserTransaction> userTransactionEvent;
    
    @Blocking
    @Incoming("custom-payment-events")
    public CompletionStage<Void> processCustomEvent(Message<CustomPaymentEvent> message) {
        CustomPaymentEvent event = message.getPayload();
        
        // Custom business logic
        if (isValidPaymentEvent(event)) {
            UserTransaction transaction = mapToUserTransaction(event);
            userTransactionEvent.fire(transaction);
        }
        
        return message.ack();
    }
    
    private boolean isValidPaymentEvent(CustomPaymentEvent event) {
        return event.getAmount().compareTo(BigDecimal.ZERO) > 0 
            && event.getDocument() != null;
    }
}
```

### Customization Scenarios

#### 1. Adding Custom Metrics

```java
@ApplicationScoped
public class EnhancedUserTransactionService extends UserTransactionService {
    
    public BigDecimal calculateYearlyAverage(String document, int year) {
        // Custom implementation for yearly averages
        return userTransactionRepository.yearlyAverage(document, year);
    }
    
    public List<UserTransaction> getTransactionsByCategory(String document, String category) {
        // Custom implementation for categorical queries
        return userTransactionRepository.findByCategory(document, category);
    }
}
```

#### 2. Custom Response Formatters

```java
@ApplicationScoped
public class CustomGrpcUserTransactionsService extends GrpcUserTransactionsService {
    
    @Override
    public Uni<UserMonthAverageResponse> getUserMonthAverage(UserMonthAverageRequest request) {
        // Add custom validation and formatting
        if (!isValidDocument(request.getDocument())) {
            return Uni.createFrom().failure(new IllegalArgumentException("Invalid document"));
        }
        
        return super.getUserMonthAverage(request)
            .map(response -> enhanceResponse(response, request));
    }
    
    private UserMonthAverageResponse enhanceResponse(UserMonthAverageResponse response, 
                                                   UserMonthAverageRequest request) {
        // Add custom fields or formatting
        return response.toBuilder()
            .setTotal(formatCurrency(response.getTotal()))
            .build();
    }
}
```

### Common Patterns and Best Practices

#### 1. Error Handling Pattern

```java
@ApplicationScoped
public class RobustUserTransactionService {
    
    private static final Logger LOGGER = Logger.getLogger(RobustUserTransactionService.class);
    
    public AverageTransactionsValue totalPerMonth(String document, LocalDate at) {
        try {
            AverageTransactionsValue result = userTransactionRepository.totalPerMonth(document, at);
            
            if (result == null) {
                LOGGER.info("No transactions found for document: " + document + ", returning default");
                return AverageTransactionsValue.defaultAverage(document);
            }
            
            return result;
        } catch (Exception e) {
            LOGGER.error("Error calculating monthly average for document: " + document, e);
            // Fallback to default
            return AverageTransactionsValue.defaultAverage(document);
        }
    }
}
```

#### 2. Reactive Programming Pattern

```java
@ApplicationScoped
public class ReactiveTransactionProcessor {
    
    @Incoming("transaction-stream")
    @Outgoing("processed-transactions")
    public Multi<UserTransaction> processTransactionStream(Multi<TransactionDetails> transactions) {
        return transactions
            .onItem().transform(this::mapToUserTransaction)
            .select().where(this::isValidTransaction)
            .onFailure().recoverWithItem(this::createDefaultTransaction)
            .log();
    }
    
    private UserTransaction mapToUserTransaction(TransactionDetails details) {
        return new UserTransaction(
            details.getTransaction().getId().toString(),
            details.getBuyer().getDocument(),
            new BigDecimal(details.getAmount()),
            details.getCheckoutId(),
            details.getPayment(),
            details.getCurrency(),
            details.getAt(),
            details.getSeller()
        );
    }
}
```

## 4. TROUBLESHOOTING

### Common Errors and Solutions

#### 1. gRPC Connection Issues

**Error**: `io.grpc.StatusRuntimeException: UNAVAILABLE`

**Solution**:
```bash
# Check if the service is running
curl -f http://localhost:8080/q/health

# Verify gRPC port configuration
# Check application.properties for grpc.server.port
```

**Configuration**:
```properties
# application.properties
quarkus.grpc.server.port=9000
quarkus.grpc.server.host=0.0.0.0
```

#### 2. Kafka Connection Problems

**Error**: `org.apache.kafka.common.errors.TimeoutException`

**Solution**:
```properties
# application.properties
kafka.bootstrap.servers=localhost:9092
mp.messaging.incoming.order-started.connector=smallrye-kafka
mp.messaging.incoming.order-started.topic=order-started
mp.messaging.incoming.order-started.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.order-started.value.deserializer=io.quarkus.kafka.client.serialization.JsonbDeserializer
```

#### 3. MongoDB Connection Issues

**Error**: `com.mongodb.MongoTimeoutException`

**Solution**:
```properties
# application.properties
quarkus.mongodb.connection-string=mongodb://localhost:27017
quarkus.mongodb.database=paymentic
quarkus.mongodb.credentials.username=admin
quarkus.mongodb.credentials.password=password
```

#### 4. Default Average Not Applied

**Error**: Null response for users without transactions

**Solution**:
```java
// Ensure proper null handling in GrpcUserTransactionsService
@Override
public Uni<UserMonthAverageResponse> getUserMonthAverage(UserMonthAverageRequest request) {
    var at = toLocalDateTime(request.getMonth());
    AverageTransactionsValue average = this.userTransactionService.totalPerMonth(
        request.getDocument(), at.toLocalDate());
    
    if (Objects.isNull(average)) {
        var defaultAverage = AverageTransactionsValue.defaultAverage(request.getDocument());
        return Uni.createFrom().item(() -> UserMonthAverageResponse.newBuilder()
            .setMonth(request.getMonth())
            .setDocument(defaultAverage.getDocument())
            .setTotal(defaultAverage.getAverage().toString())
            .build());
    }
    
    return Uni.createFrom().item(() -> UserMonthAverageResponse.newBuilder()
        .setDocument(average.getDocument())
        .setTotal(average.getAverage().toString())
        .setMonth(at.getMonth().toString())
        .build());
}
```

### Debugging Strategies

#### 1. Enable Debug Logging

```properties
# application.properties
quarkus.log.category."com.paymentic".level=DEBUG
quarkus.log.category."io.quarkus.grpc".level=DEBUG
quarkus.log.category."io.smallrye.reactive.messaging".level=DEBUG
```

#### 2. Use Quarkus Dev UI

```bash
# Start in dev mode
./mvnw compile quarkus:dev

# Access Dev UI
open http://localhost:8080/q/dev/
```

**Available Debug Tools**:
- **gRPC Services**: Test gRPC endpoints directly
- **Kafka**: Monitor message consumption
- **Health Checks**: Verify service status
- **Configuration**: Review all properties

#### 3. Integration Testing

```java
@QuarkusTest
public class UserTransactionServiceTest {
    
    @Inject
    UserTransactionService service;
    
    @Test
    public void testMonthlyAverageCalculation() {
        String document = "123456789";
        LocalDate testDate = LocalDate.of(2024, 1, 15);
        
        AverageTransactionsValue result = service.totalPerMonth(document, testDate);
        
        assertThat(result).isNotNull();
        assertThat(result.getAverage()).isGreaterThan(BigDecimal.ZERO);
    }
}
```

### Performance Considerations

#### 1. Database Indexing

```javascript
// MongoDB indexes for optimal query performance
db.user_transactions.createIndex({ "document": 1, "at": 1 })
db.user_transactions.createIndex({ "document": 1, "sellerId": 1 })
db.user_transactions.createIndex({ "at": 1 })
```

#### 2. Kafka Consumer Optimization

```properties
# application.properties
# Increase throughput
mp.messaging.incoming.order-started.consumer.max.poll.records=500
mp.messaging.incoming.order-started.consumer.fetch.min.bytes=1024

# Reduce latency
mp.messaging.incoming.order-started.consumer.fetch.max.wait.ms=100
```

#### 3. gRPC Performance Tuning

```properties
# application.properties
# Connection pooling
quarkus.grpc.server.max-inbound-message-size=4194304
quarkus.grpc.server.max-inbound-metadata-size=8192

# Threading
quarkus.grpc.server.executor.thread-pool-size=10
```

#### 4. Memory Management

```bash
# JVM tuning for production
java -Xmx512m -Xms256m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=100 \
     -jar target/quarkus-app/quarkus-run.jar
```

## 5. RELATED COMPONENTS

### Dependencies

#### Core Dependencies
- **Quarkus Platform**: 3.13.2 (Web framework and runtime)
- **Quarkus gRPC**: gRPC server and client support
- **Quarkus Kafka**: Reactive messaging with Kafka
- **Quarkus MongoDB**: Reactive MongoDB client
- **CloudEvents**: 2.5.0 (Event format standardization)

#### Development Dependencies
- **Quarkus JUnit5**: Testing framework
- **Quarkus Dev Services**: Development database containers

### Components Commonly Used Alongside

#### 1. Payment Processing Core Service
**Purpose**: Handles actual payment transactions
**Integration**: Publishes events to Kafka that this service consumes

```yaml
# Example Docker Compose integration
version: '3.8'
services:
  payment-core:
    image: paymentic/payment-core:latest
    environment:
      - KAFKA_BROKERS=kafka:9092
    depends_on:
      - kafka
      
  payment-analytics:
    image: paymentic/payment-analytics:latest
    environment:
      - KAFKA_BROKERS=kafka:9092
      - MONGODB_URI=mongodb://mongo:27017/paymentic
    depends_on:
      - kafka
      - mongo
```

#### 2. API Gateway
**Purpose**: Routes external requests to appropriate services
**Integration**: Forwards gRPC requests to this service

```yaml
# Kong Gateway configuration
services:
  - name: payment-analytics
    url: grpc://payment-analytics:9000
    
routes:
  - name: analytics-route
    service: payment-analytics
    paths:
      - /analytics
```

#### 3. Monitoring Stack
**Purpose**: Observability and monitoring
**Integration**: Collects metrics and logs from this service

```properties
# application.properties
# Prometheus metrics
quarkus.micrometer.export.prometheus.enabled=true
quarkus.micrometer.export.prometheus.path=/metrics

# Jaeger tracing
quarkus.jaeger.enabled=true
quarkus.jaeger.service-name=payment-analytics
```

#### 4. Data Warehouse/Analytics Platform
**Purpose**: Long-term data storage and complex analytics
**Integration**: This service can push aggregated data to data lakes

```java
@ApplicationScoped
public class DataWarehouseExporter {
    
    @Scheduled(every = "1h")
    public void exportHourlyAggregations() {
        // Export data to data warehouse
        List<AggregatedData> data = generateHourlyAggregations();
        dataWarehouseClient.bulkInsert(data);
    }
}
```

### Alternative Approaches

#### 1. Batch Processing Alternative
**When to use**: For non-real-time analytics requirements
**Technology**: Apache Spark, Apache Flink
**Trade-offs**: Higher latency, better for complex transformations

#### 2. CQRS with Event Sourcing
**When to use**: For complex domain logic and full audit trails
**Technology**: Axon Framework, EventStore
**Trade-offs**: More complexity, better consistency guarantees

#### 3. Stream Processing Alternative
**When to use**: For complex event transformations
**Technology**: Kafka Streams, Apache Flink
**Trade-offs**: Better for stateful processing, more operational complexity

#### 4. GraphQL API Alternative
**When to use**: For flexible client-driven queries
**Technology**: GraphQL Java, SmallRye GraphQL
**Trade-offs**: More flexible queries, less performance optimization

### Deployment Considerations

#### 1. Container Deployment

```dockerfile
FROM registry.access.redhat.com/ubi8/openjdk-21:1.18

COPY target/quarkus-app/lib/ /deployments/lib/
COPY target/quarkus-app/*.jar /deployments/
COPY target/quarkus-app/app/ /deployments/app/
COPY target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080 9000
USER 185
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
```

#### 2. Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-analytics
spec:
  replicas: 2
  selector:
    matchLabels:
      app: payment-analytics
  template:
    metadata:
      labels:
        app: payment-analytics
    spec:
      containers:
      - name: payment-analytics
        image: paymentic/payment-analytics:latest
        ports:
        - containerPort: 8080
          name: http
        - containerPort: 9000
          name: grpc
        env:
        - name: KAFKA_BROKERS
          value: "kafka:9092"
        - name: MONGODB_URI
          value: "mongodb://mongo:27017/paymentic"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```

---

## Summary

This Payment Processing Data Products module provides a robust, scalable solution for transaction analytics in the Paymentic ecosystem. Built on Quarkus with a hexagonal architecture, it offers high performance, maintainability, and strong integration capabilities with modern cloud-native infrastructure.

The service excels at real-time transaction aggregation while maintaining clean separation of concerns and comprehensive observability features. Its reactive programming model and event-driven architecture make it ideal for high-throughput payment processing environments.