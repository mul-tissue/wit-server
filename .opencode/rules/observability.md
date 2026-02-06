# Observability Rules

## Three Pillars

1. **Logging** - What happened
2. **Metrics** - How much/how fast
3. **Tracing** - Where it happened (request flow)

---

## Logging Standards

### Log Levels

| Level | Usage | Example |
|-------|-------|---------|
| ERROR | Immediate action required | DB connection failed, payment failed |
| WARN | Potential issue | Retry attempt, deprecated API used |
| INFO | Business events | User created, order completed |
| DEBUG | Development debugging | Method entry, variable values |

### Structured Logging Format

Use key=value format for easy parsing:

```java
log.info("action=create_user userId={} email={}", userId, email);
log.info("action=process_order orderId={} total={} durationMs={}", orderId, total, duration);
log.error("action=payment_failed orderId={} reason={}", orderId, reason, exception);
```

### Mandatory Logging

| Event | Level | Required Fields |
|-------|-------|-----------------|
| API request/response | INFO | method, uri, status, durationMs |
| Business event | INFO | action, entityId, userId |
| Error | ERROR | action, reason, context, stacktrace |
| External API call | INFO | service, endpoint, status, durationMs |

### Forbidden in Logs

- ❌ Passwords (even hashed)
- ❌ JWT tokens
- ❌ API keys/secrets
- ❌ Credit card numbers
- ❌ Personal identification numbers

---

## Metrics (Micrometer)

### Standard Metrics

#### HTTP Requests
```java
// Automatically collected by Spring Boot Actuator
http_server_requests_seconds_count
http_server_requests_seconds_sum
http_server_requests_seconds_max
```

#### Custom Business Metrics
```java
@Service
public class OrderService {
    private final Counter orderCounter;
    private final Timer orderProcessingTimer;
    
    public OrderService(MeterRegistry registry) {
        this.orderCounter = registry.counter("orders.created");
        this.orderProcessingTimer = registry.timer("orders.processing");
    }
    
    public Order create(OrderRequest request) {
        return orderProcessingTimer.record(() -> {
            Order order = processOrder(request);
            orderCounter.increment();
            return order;
        });
    }
}
```

#### Database Connection Pool
```yaml
# Automatically exposed by HikariCP
hikaricp_connections_active
hikaricp_connections_idle
hikaricp_connections_pending
```

### Key Metrics to Monitor

| Metric | Warning Threshold | Critical Threshold |
|--------|------------------|-------------------|
| API latency (p99) | > 500ms | > 2s |
| Error rate | > 1% | > 5% |
| DB connection pool | > 70% | > 90% |
| JVM heap usage | > 70% | > 85% |

---

## Tracing

### Request Correlation

Every request should have a trace ID:

```java
@Component
public class TraceFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, FilterChain chain) {
        
        String traceId = Optional.ofNullable(request.getHeader("X-Trace-Id"))
            .orElse(UUID.randomUUID().toString().substring(0, 8));
        
        MDC.put("traceId", traceId);
        response.setHeader("X-Trace-Id", traceId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

### Log Pattern with Trace ID
```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{36} - %msg%n</pattern>
```

---

## Health Checks

### Actuator Configuration
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
  health:
    db:
      enabled: true
    redis:
      enabled: true
```

### Custom Health Indicator
```java
@Component
public class PaymentGatewayHealthIndicator implements HealthIndicator {
    
    private final PaymentGateway paymentGateway;
    
    @Override
    public Health health() {
        try {
            paymentGateway.healthCheck();
            return Health.up().build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
```

---

## Production Checklist

### Before Go-Live
- [ ] Structured logging configured
- [ ] Trace ID propagation enabled
- [ ] Health checks for all dependencies
- [ ] Actuator endpoints secured
- [ ] Key metrics identified and dashboards created
- [ ] Alerting rules configured

### Future Enhancements (TODO)
- [ ] Prometheus + Grafana dashboards
- [ ] Distributed tracing (Zipkin/Jaeger)
- [ ] Log aggregation (ELK/Loki)
- [ ] APM integration (DataDog/NewRelic)

---

## Alerting Guidelines

### Alert Severity

| Severity | Response Time | Example |
|----------|---------------|---------|
| Critical | < 5 min | Service down, data loss risk |
| High | < 30 min | High error rate, degraded service |
| Medium | < 4 hours | Elevated latency, resource warning |
| Low | Next business day | Non-critical warnings |

### Alert Fatigue Prevention
- Don't alert on every error
- Group related alerts
- Use appropriate thresholds
- Review and tune alerts regularly
