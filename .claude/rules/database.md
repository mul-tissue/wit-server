---
name: database
description: Database migration rules using Flyway and schema conventions
---

# Database Rules

## Flyway Migration

### File Location
```
src/main/resources/db/migration/
```

### Naming Convention
```
V<version>__<action>__<target>__<description>.sql
```

- **Version**: Integer, sequential (V1, V2, V3...)
- **Action**: One of `create`, `add`, `drop`, `insert`, `update`, `delete`
- **Target**: Table name, column name, index name, etc.
- **Description**: Optional, brief description

### Examples
```
V1__init.sql                                    # Initial schema (all tables)
V2__create__survey_table.sql                    # Create new table
V3__add__users__phone_column.sql                # Add column to users
V4__create__idx_users_email.sql                 # Create index
V5__drop__legacy_table.sql                      # Drop table
V6__insert__terms__initial_data.sql             # Insert seed data
V7__update__users__status_default.sql           # Update default value
```

### Rules
1. **Never modify existing migration files** once committed
2. **One logical change per file** (don't mix table creation with data insertion)
3. **Always include rollback comment** at the end of file (for reference)
4. **Use lowercase snake_case** for all database identifiers

### Migration File Template
```sql
-- V<version>__<action>__<target>__<description>.sql
-- Description: Brief description of what this migration does

-- Migration
CREATE TABLE example (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Rollback (for reference only, Flyway doesn't support auto-rollback)
-- DROP TABLE example;
```

## Database Conventions

### Table Naming
- Use **plural lowercase snake_case**: `users`, `chat_rooms`, `user_term_agreements`
- Join tables: `<table1>_<table2>` alphabetically: `chat_participants`, `user_term_agreements`

### Column Naming
- Use **lowercase snake_case**: `user_id`, `created_at`, `profile_image_path`
- Primary key: always `id`
- Foreign keys: `<referenced_table_singular>_id` (e.g., `user_id`, `feed_id`)
- Timestamps: `created_at`, `updated_at`, `deleted_at`, `<action>_at`
- Booleans: `is_<adjective>` or just descriptive (e.g., `is_read`, `active`, `required`)

### Index Naming
```
idx_<table>_<column(s)>
```
Examples:
- `idx_users_email`
- `idx_feeds_user_id`
- `idx_feeds_country_district` (composite)
- `idx_feeds_location` (spatial)

### Constraint Naming
```
fk_<table>_<referenced_table>     # Foreign key
uk_<table>_<column(s)>            # Unique constraint
ck_<table>_<description>          # Check constraint
```
Examples:
- `fk_feeds_users`
- `uk_users_email`
- `uk_reactions_feed_user` (composite unique)

## PostgreSQL Specific

### Data Types
| Java Type | PostgreSQL Type |
|-----------|-----------------|
| Long | BIGINT |
| Integer | INT |
| String | VARCHAR(n) or TEXT |
| Boolean | BOOLEAN |
| LocalDateTime | TIMESTAMP |
| LocalDate | DATE |
| BigDecimal | DECIMAL(p, s) |
| UUID | BINARY(16) or UUID |
| Enum | VARCHAR(n) |

### Primary Key Pattern
```sql
id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY
```

### Timestamp Pattern
```sql
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

### Foreign Key Pattern
```sql
user_id BIGINT NOT NULL,
CONSTRAINT fk_feeds_users FOREIGN KEY (user_id) REFERENCES users(id)
```

### Spatial Index (PostGIS)
```sql
-- Requires PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create spatial index
CREATE INDEX idx_feeds_location ON feeds USING GIST (
    ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
);
```

## Entity Mapping

### Standard Entity Template
```java
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public User(String email, UserStatus status) {
        this.email = email;
        this.status = status != null ? status : UserStatus.PENDING_AGREEMENT;
    }

    // Business methods (NO SETTERS!)
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
}
```

## Initial Data (Seed Data)

For initial/seed data, create separate migration files:
```
V2__insert__terms__initial_data.sql
V3__insert__countries__initial_data.sql
```

Example:
```sql
-- V2__insert__terms__initial_data.sql
INSERT INTO terms (type, title, version, required, active, created_at, updated_at)
VALUES 
    ('TERMS_OF_SERVICE', '서비스 이용약관', 'v1.0', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PRIVACY_POLICY', '개인정보 처리방침', 'v1.0', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MARKETING', '마케팅 정보 수신 동의', 'v1.0', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

## Testing

### Test Database
- Use H2 in-memory database for unit tests
- Use Testcontainers with PostgreSQL for integration tests

### Test Migration
```yaml
# src/test/resources/application-test.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
```
