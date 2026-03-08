# Microservices Kafka Demo

A Spring Boot microservices project demonstrating **event-driven architecture** using **Apache Kafka**.  
Three independent services communicate exclusively through Kafka topics — no direct HTTP calls between services.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT (Postman / cURL)                     │
│                   POST http://localhost:8081/orders                 │
└───────────────────────────────┬─────────────────────────────────────┘
                                │  HTTP POST
                                ▼
┌───────────────────────────────────────────────────────┐
│               ORDER SERVICE  (port 8081)              │
│                                                       │
│  OrderController  →  OrderService  →  OrderProducer   │
│                                                       │
│  • Accepts the order via REST                         │
│  • Stores it in memory                                │
│  • Publishes an Order event to Kafka                  │
└───────────────────────────────┬───────────────────────┘
                                │  Kafka Topic: order-created
                                ▼
┌───────────────────────────────────────────────────────┐
│              PAYMENT SERVICE  (port 8082)             │
│                                                       │
│  PaymentConsumer  →  PaymentProducer                  │
│                                                       │
│  • Listens to "order-created" topic                   │
│  • Processes the payment (sets status = PAID)         │
│  • Publishes a PaymentSuccess event to Kafka          │
└───────────────────────────────┬───────────────────────┘
                                │  Kafka Topic: payment-success
                                ▼
┌───────────────────────────────────────────────────────┐
│           NOTIFICATION SERVICE  (port 8083)           │
│                                                       │
│  NotificationConsumer                                 │
│                                                       │
│  • Listens to "payment-success" topic                 │
│  • Sends a notification confirming payment            │
└───────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Technology        | Version   | Purpose                              |
|-------------------|-----------|--------------------------------------|
| Java              | 17        | Language                             |
| Spring Boot       | 3.5.11    | Application framework                |
| Spring Kafka      | 3.3.x     | Kafka integration                    |
| Apache Kafka      | 3.9.x     | Message broker                       |
| Confluent Kafka   | 7.4.0     | Docker image for Kafka + Zookeeper   |
| Lombok            | latest    | Boilerplate reduction                |
| Maven             | 3.x       | Build tool                           |
| Docker            | latest    | Running Kafka infrastructure         |

---

## Project Structure

```
microservices-kafka-demo/
│
├── docker/
│   └── docker-compose.yml          # Zookeeper + Kafka containers
│
├── order-service/                  # Exposes REST API, publishes to Kafka
│   └── src/main/java/.../
│       ├── controller/OrderController.java
│       ├── service/OrderService.java
│       ├── producer/OrderProducer.java
│       └── model/Order.java
│
├── payment-service/                # Consumes orders, publishes payment result
│   └── src/main/java/.../
│       ├── consumer/PaymentConsumer.java
│       ├── producer/PaymentProducer.java
│       └── model/
│           ├── OrderCreatedEvent.java
│           └── PaymentSuccessEvent.java
│
└── notification-service/           # Consumes payment result, sends notification
    └── src/main/java/.../
        ├── consumer/NotificationConsumer.java
        └── model/PaymentSuccessEvent.java
```

---

## Kafka Topics

| Topic              | Producer          | Consumer             |
|--------------------|-------------------|----------------------|
| `order-created`    | order-service     | payment-service      |
| `payment-success`  | payment-service   | notification-service |

---

## Prerequisites

- **Java 17+**
- **Maven 3.x**
- **Docker Desktop** (for Kafka)

---

## Running the Project

### Step 1 — Start Kafka

```bash
cd docker
docker-compose up -d
```

Verify containers are running:
```bash
docker ps
```
You should see `zookeeper` and `kafka` containers up.

---

### Step 2 — Start the Services

Open **three separate terminals** and run each service:

```bash
# Terminal 1 — Order Service
cd order-service
mvn spring-boot:run
```

```bash
# Terminal 2 — Payment Service
cd payment-service
mvn spring-boot:run
```

```bash
# Terminal 3 — Notification Service
cd notification-service
mvn spring-boot:run
```

Wait until all three print `Started ... Application in X seconds`.

---

### Step 3 — Trigger the Flow

**Using cURL:**
```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId":"2001","product":"Phone","quantity":1}'
```

**Using Postman / Insomnia:**
```
Method : POST
URL    : http://localhost:8081/orders
Body   : (raw JSON)
{
  "orderId": "2001",
  "product": "Phone",
  "quantity": 1
}
```

**Expected HTTP response:**
```
200 OK
Order [2001] accepted and published to Kafka
```

---

### Step 4 — Watch the Cascade in Terminals

**order-service terminal:**
```
================================================
  ORDER SERVICE — New Order Received
  Order ID  : 2001
  Product   : Phone
  Quantity  : 1
================================================
  [ORDER SERVICE] SUCCESS — Order published to Kafka
  Topic     : order-created
  Order ID  : 2001
  Partition : 0  |  Offset: 0
  Waiting for Payment Service to pick it up...
```

**payment-service terminal:**
```
================================================
  PAYMENT SERVICE — Order Event Received
  Order ID  : 2001
  Product   : Phone
  Quantity  : 1
  Processing payment...
  Payment Status : PAID
  Publishing result to 'payment-success' topic...
================================================
  [PAYMENT SERVICE] SUCCESS — Payment result published to Kafka
  Topic     : payment-success
  Order ID  : 2001  |  Status: PAID
  Partition : 0  |  Offset: 0
  Waiting for Notification Service to pick it up...
```

**notification-service terminal:**
```
================================================
  NOTIFICATION SERVICE — Payment Event Received
  Order ID  : 2001
  Status    : PAID
  Sending notification to customer...
  Notification sent! — Order 2001 has been PAID and confirmed.
================================================
```

---

## REST Endpoints

### Order Service — `http://localhost:8081`

| Method | Endpoint  | Description                              | Request Body         |
|--------|-----------|------------------------------------------|----------------------|
| POST   | `/orders` | Place a new order, publishes to Kafka    | `Order` JSON         |
| GET    | `/orders` | Returns all orders placed in this session| —                    |

**Order JSON schema:**
```json
{
  "orderId": "2001",
  "product": "Phone",
  "quantity": 1
}
```

> Payment Service (8082) and Notification Service (8083) are **consumer-only** — they have no public REST endpoints.

---

## Configuration

### order-service `application.yml`
```yaml
server:
  port: 8081
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

### payment-service `application.yml`
```yaml
server:
  port: 8082
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: payment-group
      auto-offset-reset: earliest
```

### notification-service `application.yml`
```yaml
server:
  port: 8083
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

---

## Stopping Everything

```bash
# Stop Kafka
cd docker
docker-compose down
```

---

## Key Concepts Demonstrated

- **Event-driven architecture** — services react to events, not direct calls
- **Kafka Producer** — publishing typed Java objects as JSON to a topic
- **Kafka Consumer** — listening to topics with `@KafkaListener`
- **Service decoupling** — no service imports or calls another service directly
- **JsonSerializer / JsonDeserializer** — automatic POJO ↔ JSON conversion
- **Multi-topic chaining** — `order-created` → `payment-success` event chain
