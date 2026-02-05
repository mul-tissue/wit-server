# Module Structure Detailed Examples

## Complete Module Example: User Module

```
com.wit.user/
├── api/
│   └── UserController.java
├── service/
│   ├── UserService.java          # Command (CUD)
│   └── UserQueryService.java     # Query (Read)
├── domain/
│   └── User.java
├── repository/
│   ├── UserRepository.java       # JPA
│   └── UserQueryRepository.java  # QueryDSL
├── event/
│   ├── UserCreatedEvent.java
│   └── UserUpdatedEvent.java
├── dto/
│   ├── request/
│   │   ├── CreateUserRequest.java
│   │   └── UpdateUserRequest.java
│   ├── response/
│   │   └── UserResponse.java
│   └── UserProfileDto.java       # For inter-module queries
└── exception/
    └── UserErrorCode.java
```

## Entity Example

```java
package com.wit.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String password;
    
    private String profileImageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Builder
    public User(String email, String name, String password, UserRole role) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role != null ? role : UserRole.USER;
    }
    
    public void updateProfile(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }
}

enum UserRole {
    USER, ADMIN
}
```

## Controller Example

```java
package com.wit.user.api;

import com.wit.user.dto.*;
import com.wit.user.service.UserService;
import com.wit.user.service.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final UserQueryService userQueryService;
    
    // Command endpoints
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.update(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // Query endpoints
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        UserDTO user = userQueryService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAll(Pageable pageable) {
        Page<UserDTO> users = userQueryService.findAll(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<UserDTO>> search(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<UserDTO> users = userQueryService.search(keyword, pageable);
        return ResponseEntity.ok(users);
    }
}
```

## Service Example (Command)

```java
package com.wit.user.service;

import com.wit.user.domain.User;
import com.wit.user.dto.*;
import com.wit.user.event.*;
import com.wit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    
    public UserResponse create(UserRequest request) {
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Email already exists: " + request.email());
        }
        
        // Create entity
        User user = User.builder()
            .email(request.email())
            .name(request.name())
            .password(passwordEncoder.encode(request.password()))
            .build();
        
        // Save
        User saved = userRepository.save(user);
        
        // Publish event
        eventPublisher.publishEvent(new UserCreatedEvent(saved.getId()));
        
        return UserResponse.from(saved);
    }
    
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        
        user.updateProfile(request.name(), request.profileImageUrl());
        
        eventPublisher.publishEvent(new UserUpdatedEvent(id));
        
        return UserResponse.from(user);
    }
    
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found: " + id);
        }
        
        userRepository.deleteById(id);
        eventPublisher.publishEvent(new UserDeletedEvent(id));
    }
}
```

## Service Example (Query)

```java
package com.wit.user.service;

import com.wit.user.dto.UserDTO;
import com.wit.user.dto.UserProfileDTO;
import com.wit.user.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {
    
    private final UserQueryRepository userQueryRepository;
    
    public UserDTO findById(Long id) {
        return userQueryRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }
    
    public Page<UserDTO> findAll(Pageable pageable) {
        return userQueryRepository.findAll(pageable);
    }
    
    public Page<UserDTO> search(String keyword, Pageable pageable) {
        return userQueryRepository.searchByKeyword(keyword, pageable);
    }
    
    // This method is for inter-module communication
    public UserProfileDTO getProfile(Long userId) {
        return userQueryRepository.findProfileById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}
```

## DTOs

```java
package com.wit.user.dto;

import com.wit.user.domain.User;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

// Request DTO
public record UserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be 2-50 characters")
    String name,
    
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
             message = "Password must be 8+ chars with letters and numbers")
    String password
) {}

// Update Request DTO
public record UserUpdateRequest(
    @NotBlank String name,
    String profileImageUrl
) {}

// Response DTO
public record UserResponse(
    Long id,
    String email,
    String name,
    String profileImageUrl,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getProfileImageUrl(),
            user.getCreatedAt()
        );
    }
}

// Query DTO (internal use)
public record UserDTO(
    Long id,
    String email,
    String name,
    String profileImageUrl,
    LocalDateTime createdAt
) {}

// For inter-module communication
public record UserProfileDTO(
    Long id,
    String name,
    String profileImageUrl
) {}
```

## Repositories

```java
package com.wit.user.repository;

import com.wit.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
```

```java
package com.wit.user.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wit.user.domain.QUser;
import com.wit.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    private static final QUser user = QUser.user;
    
    public Optional<UserDTO> findById(Long id) {
        UserDTO result = queryFactory
            .select(Projections.constructor(UserDTO.class,
                user.id,
                user.email,
                user.name,
                user.profileImageUrl,
                user.createdAt))
            .from(user)
            .where(user.id.eq(id))
            .fetchOne();
        
        return Optional.ofNullable(result);
    }
    
    public Page<UserDTO> findAll(Pageable pageable) {
        List<UserDTO> content = queryFactory
            .select(Projections.constructor(UserDTO.class,
                user.id,
                user.email,
                user.name,
                user.profileImageUrl,
                user.createdAt))
            .from(user)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        
        long total = queryFactory
            .selectFrom(user)
            .fetchCount();
        
        return new PageImpl<>(content, pageable, total);
    }
    
    public Optional<UserProfileDTO> findProfileById(Long userId) {
        UserProfileDTO result = queryFactory
            .select(Projections.constructor(UserProfileDTO.class,
                user.id,
                user.name,
                user.profileImageUrl))
            .from(user)
            .where(user.id.eq(userId))
            .fetchOne();
        
        return Optional.ofNullable(result);
    }
}
```

## Events

```java
package com.wit.user.event;

public record UserCreatedEvent(Long userId) {}

public record UserUpdatedEvent(Long userId) {}

public record UserDeletedEvent(Long userId) {}
```
