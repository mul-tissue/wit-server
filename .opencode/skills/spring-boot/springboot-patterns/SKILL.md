---
name: springboot-patterns
description: Spring Boot architecture patterns, REST API design, layered services, data access, caching, async processing, and logging. Use for Java Spring Boot backend work.
---

# Spring Boot Development Patterns

Spring Boot architecture and API patterns for scalable, production-grade services.

## REST API Structure

```java
@RestController
@RequestMapping("/api/v1/markets")
@Validated
class MarketController {
  private final MarketService marketService;
  private final MarketQueryService marketQueryService;

  MarketController(MarketService marketService, MarketQueryService marketQueryService) {
    this.marketService = marketService;
    this.marketQueryService = marketQueryService;
  }

  @GetMapping
  ResponseEntity<Page<MarketResponse>> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<MarketResponse> markets = marketQueryService.list(PageRequest.of(page, size));
    return ResponseEntity.ok(markets);
  }

  @PostMapping
  ResponseEntity<MarketResponse> create(@Valid @RequestBody CreateMarketRequest request) {
    MarketResponse response = marketService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
```

## Repository Pattern (Spring Data JPA)

```java
public interface MarketRepository extends JpaRepository<MarketEntity, Long> {
  @Query("SELECT m FROM MarketEntity m WHERE m.status = :status ORDER BY m.volume DESC")
  List<MarketEntity> findActive(@Param("status") MarketStatus status, Pageable pageable);
  
  @Query("SELECT m FROM MarketEntity m LEFT JOIN FETCH m.categories WHERE m.id = :id")
  Optional<MarketEntity> findWithCategories(@Param("id") Long id);
}
```

## Service Layer with Transactions

```java
@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {
  private final MarketRepository repo;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public MarketResponse create(CreateMarketRequest request) {
    MarketEntity entity = MarketEntity.from(request);
    MarketEntity saved = repo.save(entity);
    
    eventPublisher.publishEvent(new MarketCreatedEvent(saved.getId()));
    
    return MarketResponse.from(saved);
  }
}
```

## DTOs and Validation

```java
public record CreateMarketRequest(
    @NotBlank @Size(max = 200) String name,
    @NotBlank @Size(max = 2000) String description,
    @NotNull @FutureOrPresent Instant endDate,
    @NotEmpty List<@NotBlank String> categories
) {}

public record MarketResponse(Long id, String name, MarketStatus status, LocalDateTime createdAt) {
  public static MarketResponse from(Market market) {
    return new MarketResponse(market.id(), market.name(), market.status(), market.createdAt());
  }
}
```

## Exception Handling

```java
@RestControllerAdvice
class GlobalExceptionHandler {
  
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  
  @ExceptionHandler(BusinessException.class)
  ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
    log.warn("action=business_exception errorCode={} message={}", 
        ex.getErrorCode().getErrorName(), ex.getMessage());
    
    return ResponseEntity
        .status(ex.getErrorCode().getHttpStatus())
        .body(ErrorResponse.from(ex.getErrorCode()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .collect(Collectors.joining(", "));
    
    return ResponseEntity.badRequest()
        .body(new ErrorResponse("VALIDATION_ERROR", message));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    log.error("action=unhandled_exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("INTERNAL_ERROR", "Internal server error"));
  }
}
```

## Caching

Requires `@EnableCaching` on a configuration class.

```java
@Service
@RequiredArgsConstructor
public class MarketCacheService {
  private final MarketRepository repo;

  @Cacheable(value = "market", key = "#id")
  public Market getById(Long id) {
    return repo.findById(id)
        .map(Market::from)
        .orElseThrow(() -> new BusinessException(MarketErrorCode.NOT_FOUND));
  }

  @CacheEvict(value = "market", key = "#id")
  public void evict(Long id) {}
  
  @CacheEvict(value = "market", allEntries = true)
  public void evictAll() {}
}
```

## Async Processing

Requires `@EnableAsync` on a configuration class.

```java
@Service
public class NotificationService {
  
  @Async
  public CompletableFuture<Void> sendAsync(Notification notification) {
    // Send email/SMS asynchronously
    return CompletableFuture.completedFuture(null);
  }
}
```

## Logging (SLF4J)

```java
@Service
public class ReportService {
  private static final Logger log = LoggerFactory.getLogger(ReportService.class);

  public Report generate(Long marketId) {
    log.info("action=generate_report marketId={}", marketId);
    long start = System.currentTimeMillis();
    
    try {
      Report report = doGenerate(marketId);
      long duration = System.currentTimeMillis() - start;
      log.info("action=generate_report_success marketId={} durationMs={}", marketId, duration);
      return report;
    } catch (Exception ex) {
      log.error("action=generate_report_failed marketId={} reason={}", marketId, ex.getMessage(), ex);
      throw ex;
    }
  }
}
```

## Pagination and Sorting

```java
PageRequest page = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
Page<Market> results = marketService.list(page);
```

## Configuration Properties

```java
@ConfigurationProperties(prefix = "app.payment")
@Validated
public record PaymentProperties(
    @NotBlank String apiKey,
    @NotBlank String apiSecret,
    @Min(1000) @Max(60000) int timeoutMs,
    @Min(1) @Max(5) int maxRetries
) {}

// Enable in main class
@EnableConfigurationProperties(PaymentProperties.class)
```

## Production Defaults

- Prefer constructor injection, avoid field injection
- Enable `spring.mvc.problemdetails.enabled=true` for RFC 7807 errors (Spring Boot 3+)
- Configure HikariCP pool sizes for workload, set timeouts
- Use `@Transactional(readOnly = true)` for queries
- Keep transactions short (no external calls inside)
- Enforce null-safety via `@NonNull` and `Optional` where appropriate

**Remember**: Keep controllers thin, services focused, repositories simple, and errors handled centrally. Optimize for maintainability and testability.
