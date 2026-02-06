# Inter-Module Communication Patterns

## Rule: Modules Must Not Directly Access Each Other's Internal Details

### FORBIDDEN
```java
// In FeedService
@Service
public class FeedService {
    private final UserRepository userRepository; // Accessing other module's repository
    
    public void createFeed(FeedRequest request) {
        User user = userRepository.findById(request.userId()); // Direct entity access
        // ...
    }
}
```

### ALLOWED: Query via QueryService

```java
// In FeedQueryService
@Service
@Transactional(readOnly = true)
public class FeedQueryService {
    private final FeedQueryRepository feedQueryRepository;
    private final UserQueryService userQueryService; // Use QueryService
    private final CompanionQueryService companionQueryService; // Multiple modules OK
    
    public FeedDetailDTO getFeedDetail(Long feedId) {
        FeedDTO feed = feedQueryRepository.findById(feedId)
            .orElseThrow(() -> new FeedNotFoundException("Feed not found"));
        
        // Query user info from another module
        UserProfileDTO author = userQueryService.getProfile(feed.authorId());
        
        // Query companion info if exists
        CompanionDTO companion = feed.companionId() != null
            ? companionQueryService.findById(feed.companionId())
            : null;
        
        return FeedDetailDTO.of(feed, author, companion);
    }
}
```

---

## Pattern 1: Synchronous Query (for Read operations)

### Use Case
When you need current state of another domain's data.

### Example: Feed needs User profile information

```java
// user module - exposes query service
package com.wit.user.application;

@Service
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {
    
    // This method is designed for inter-module queries
    @Override
    public UserProfileDTO getProfile(Long userId) {
        return userQueryRepository.findProfileById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
    
    // Batch query for performance
    @Override
    public Map<Long, UserProfileDTO> getProfiles(List<Long> userIds) {
        return userQueryRepository.findProfilesByIds(userIds)
            .stream()
            .collect(Collectors.toMap(UserProfileDTO::id, Function.identity()));
    }
}
```

```java
// feed module - uses query service
package com.wit.feed.application;

@Service
@Transactional(readOnly = true)
public class FeedQueryServiceImpl implements FeedQueryService {
    
    private final FeedQueryRepository feedQueryRepository;
    private final UserQueryService userQueryService; // Injected from user module
    
    @Override
    public List<FeedWithAuthorDTO> getRecentFeeds(Pageable pageable) {
        List<FeedDTO> feeds = feedQueryRepository.findRecent(pageable);
        
        // Get all author ids
        List<Long> authorIds = feeds.stream()
            .map(FeedDTO::authorId)
            .distinct()
            .toList();
        
        // Batch query to avoid N+1
        Map<Long, UserProfileDTO> authorMap = userQueryService.getProfiles(authorIds);
        
        // Combine feed and author info
        return feeds.stream()
            .map(feed -> FeedWithAuthorDTO.of(feed, authorMap.get(feed.authorId())))
            .toList();
    }
}
```

### Key Points
- Returns DTO, never Entity
- Read-only operation
- Synchronous (immediate response)
- Use batch queries to avoid N+1 problems

---

## Pattern 2: Event-Driven (for Write operations)

### Use Case
When a domain change should trigger actions in other domains.

### Example: User creation triggers welcome notification

```java
// user module - publishes event after creation
package com.wit.user.application;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    public UserResponse create(UserRequest request) {
        User user = User.builder()
            .email(request.email())
            .name(request.name())
            .build();
        
        User saved = userRepository.save(user);
        
        // Publish event - fire and forget
        eventPublisher.publishEvent(new UserCreatedEvent(saved.getId()));
        
        return UserResponse.from(saved);
    }
}
```

```java
// user module - event definition
package com.wit.user.event;

public record UserCreatedEvent(Long userId) {}
```

```java
// notification module - listens to event
package com.wit.notification.listener;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    
    private final NotificationService notificationService;
    
    @EventListener
    @Async // Optional: for non-blocking execution
    public void handleUserCreated(UserCreatedEvent event) {
        // React to user creation
        notificationService.sendWelcomeNotification(event.userId());
    }
}
```

### Key Points
- Events are past tense (facts): `UserCreatedEvent`, not `CreateUserEvent`
- Fire-and-forget (no return value)
- Asynchronous processing
- Multiple listeners can react to same event
- Easy to migrate to Kafka/RabbitMQ later

---

## Pattern 3: Command + Event Flow

### Example: Companion creation triggers chat room creation

```java
// companion module - service
package com.wit.companion.application;

@Service
@Transactional
public class CompanionServiceImpl implements CompanionService {
    
    private final CompanionRepository companionRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    public CompanionResponse create(CompanionRequest request) {
        Companion companion = Companion.builder()
            .title(request.title())
            .creatorId(request.creatorId())
            .maxParticipants(request.maxParticipants())
            .build();
        
        Companion saved = companionRepository.save(companion);
        
        // Publish event for side effects
        eventPublisher.publishEvent(
            new CompanionCreatedEvent(saved.getId(), saved.getCreatorId())
        );
        
        return CompanionResponse.from(saved);
    }
}
```

```java
// companion module - event
package com.wit.companion.event;

public record CompanionCreatedEvent(
    Long companionId,
    Long creatorId
) {}
```

```java
// chat module - listener
package com.wit.chat.listener;

@Component
@RequiredArgsConstructor
public class CompanionEventListener {
    
    private final ChatService chatService;
    
    @EventListener
    @Transactional
    public void handleCompanionCreated(CompanionCreatedEvent event) {
        // Automatically create chat room when companion is created
        chatService.createRoomForCompanion(event.companionId(), event.creatorId());
    }
}
```

```java
// notification module - another listener
package com.wit.notification.listener;

@Component
@RequiredArgsConstructor
public class CompanionEventListener {
    
    private final NotificationService notificationService;
    
    @EventListener
    @Async
    public void handleCompanionCreated(CompanionCreatedEvent event) {
        // Send notification to nearby users
        notificationService.notifyNearbyUsers(event.companionId());
    }
}
```

---

## Pattern 4: Complex Flow with Multiple Modules

### Example: Feed publish flow

```
1. User publishes feed (feed module)
   |
2. FeedPublishedEvent emitted
   |
3. Multiple listeners react:
   - notification: notify followers
   - companion: check if feed relates to companion
   - home: update home feed cache
```

```java
// feed module
@Service
@Transactional
public class FeedServiceImpl implements FeedService {
    
    private final FeedRepository feedRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserQueryService userQueryService; // Query user info
    
    @Override
    public FeedResponse publish(FeedRequest request) {
        // Validate user exists
        UserProfileDTO author = userQueryService.getProfile(request.authorId());
        
        Feed feed = Feed.builder()
            .authorId(request.authorId())
            .content(request.content())
            .location(request.location())
            .build();
        
        Feed saved = feedRepository.save(feed);
        
        // Publish event with rich context
        eventPublisher.publishEvent(new FeedPublishedEvent(
            saved.getId(),
            saved.getAuthorId(),
            saved.getLocation()
        ));
        
        return FeedResponse.from(saved);
    }
}
```

```java
// Multiple modules listen
@Component
public class NotificationEventListener {
    @EventListener
    public void handleFeedPublished(FeedPublishedEvent event) {
        // Notify followers
    }
}

@Component
public class HomeEventListener {
    @EventListener
    public void handleFeedPublished(FeedPublishedEvent event) {
        // Update home feed cache
    }
}

@Component
public class CompanionEventListener {
    @EventListener
    public void handleFeedPublished(FeedPublishedEvent event) {
        // Check if feed is related to active companion
    }
}
```

---

## Anti-Patterns to Avoid

### Don't Return Values from Events

```java
// Bad - events shouldn't return values
@EventListener
public boolean handleUserCreated(UserCreatedEvent event) {
    return notificationService.send(event.userId()); // Wrong
}
```

### Don't Call Other Module's Command Service

```java
// Bad - modules shouldn't directly modify other modules
@Service
public class FeedService {
    private final NotificationService notificationService; // Command service
    
    public void publish(FeedRequest request) {
        Feed feed = save(request);
        notificationService.create(...); // Direct modification
    }
}
```

### Don't Share Entities Between Modules

```java
// Bad - exposing internal entity
@Service
public class UserQueryService {
    public User findById(Long id) { // Returns entity
        return userRepository.findById(id);
    }
}

// Good - return DTO
@Service
public class UserQueryService {
    public UserDTO findById(Long id) { // Returns DTO
        return userQueryRepository.findDTOById(id);
    }
}
```

---

## Testing Inter-Module Communication

```java
// Test query service interaction
@SpringBootTest
class FeedQueryServiceTest {
    
    @Autowired
    private FeedQueryService feedQueryService;
    
    @MockBean
    private UserQueryService userQueryService; // Mock other module
    
    @Test
    void shouldGetFeedWithAuthor() {
        // Given
        Long feedId = 1L;
        UserProfileDTO mockAuthor = new UserProfileDTO(1L, "John", "image.jpg");
        when(userQueryService.getProfile(1L)).thenReturn(mockAuthor);
        
        // When
        FeedDetailDTO result = feedQueryService.getFeedDetail(feedId);
        
        // Then
        assertThat(result.author()).isEqualTo(mockAuthor);
    }
}
```

```java
// Test event publishing and listening
@SpringBootTest
class UserEventTest {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @MockBean
    private NotificationService notificationService;
    
    @Test
    void shouldTriggerNotification_whenUserCreated() {
        // When
        eventPublisher.publishEvent(new UserCreatedEvent(1L));
        
        // Then
        verify(notificationService, timeout(1000))
            .sendWelcomeNotification(1L);
    }
}
```

---

## Migration to MSA

When moving to microservices, these patterns translate easily:

| Current (Monolith) | Future (MSA) |
|-------------------|-------------|
| QueryService call | Feign/gRPC call |
| ApplicationEvent | Kafka/RabbitMQ message |
| Direct method call | HTTP REST API |
| Single transaction | Saga pattern |

The business logic and patterns remain the same!
