# JPA Helper (`jpa-helper`)

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021%20%7C%2025+-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%2B-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![JitPack](https://jitpack.io/v/Jiaul21/Spring-Boot-jpa-helper.svg)](https://jitpack.io/#Jiaul21/Spring-Boot-jpa-helper)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A foundational, high-performance Spring Data JPA library providing auditable base entities, dynamic Hibernate soft-delete filters, batch update engines, and dynamic nested fetch selectors.

Designed as a **professional thin library JAR**:
1. **Zero Classpath Pollution**: Framework dependencies (`spring-data-jpa`, `jakarta.persistence-api`, `hibernate-core`) are marked `provided` and `optional`. Your consumer application retains 100% control over Spring Data and Hibernate versions.
2. **IDE Friendly**: Bundles compiled source JAR (`-sources.jar`) and Javadoc JAR (`-javadoc.jar`) for instant code completion and documentation.
3. **Java Baseline**: Built for Java 17 minimum, fully compatible with Java 21, 25, and future LTS releases.

---

## 📋 Compatibility Matrix & System Requirements

| Component | Minimum Version | Supported Range | Notes |
|---|---|---|---|
| **Java (JDK)** | `17` | `17`, `21`, `25+` (LTS) | Compiled with Java 17 bytecode target (`--release 17`). |
| **Spring Boot** | `3.0.0` | `3.0.0+` (3.x, 4.x+) | Full Jakarta EE 10 / Spring Boot 3.x+ ecosystem support. |
| **Spring Data JPA** | `3.0.0` | `3.0.0+` | Provided transitively by Spring Boot. |
| **Hibernate ORM** | `6.0.0` | `6.0.x` – `6.x+` | Standard provider in Spring Boot 3.x+; leverages HQL `id(e)` & `@FilterDef`. |
| **Jakarta Persistence** | `3.0` | `3.0+` | Uses standard `jakarta.persistence.*` namespace. |

> **Hibernate 6+ Availability Note:**
> All Spring Boot 3.x and future releases natively embed **Hibernate 6.x+** by default:
> 1. Spring Boot 3.0.x embeds Hibernate 6.1
> 2. Spring Boot 3.1.x embeds Hibernate 6.2
> 3. Spring Boot 3.2.x embeds Hibernate 6.4
> 4. Spring Boot 3.3.x embeds Hibernate 6.5
> 5. Spring Boot 3.4.x embeds Hibernate 6.6
>
> Because `jpa-helper` marks `hibernate-core` as `<scope>provided</scope>`, any Spring Boot 3.x+ consumer automatically provides a fully compatible Hibernate runtime without manual dependency overrides.

---

## 📦 Installation

### Option 1: Via JitPack (Recommended for Remote Consumption)

[JitPack](https://jitpack.io/#Jiaul21/Spring-Boot-jpa-helper) builds and serves the library directly from GitHub release tags or the `main` branch.

#### Maven (`pom.xml`)
```xml
<!-- 1. Add JitPack repository -->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<!-- 2. Add dependency -->
<dependencies>
    <dependency>
        <groupId>com.github.Jiaul21</groupId>
        <artifactId>Spring-Boot-jpa-helper</artifactId>
        <version>${version}</version> <!-- e.g., v0.0.1 or main-SNAPSHOT -->
    </dependency>
</dependencies>
```

#### Gradle (Groovy - `build.gradle`)
```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Jiaul21:Spring-Boot-jpa-helper:<version>'
}
```

#### Gradle (Kotlin DSL - `build.gradle.kts`)
```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.Jiaul21:Spring-Boot-jpa-helper:${version}")
}
```

---

### Option 2: Local Development Cache (`~/.m2`)

If you clone and build locally on your machine via `mvn clean install`, use the local snapshot coordinates:

#### Maven (`pom.xml`)
```xml
<dependency>
    <groupId>com.jiaul</groupId>
    <artifactId>jpa-helper</artifactId>
    <version>${version}</version> <!-- e.g., 0.0.1-SNAPSHOT -->
</dependency>
```

#### Gradle (Groovy)
```groovy
repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation 'com.jiaul:jpa-helper:<version>'
}
```

#### Gradle (Kotlin DSL - `build.gradle.kts`)
```kotlin
repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.jiaul:jpa-helper:<version>")
}
```

---

### Option 3: GitHub Packages

Published packages can also be consumed from GitHub Packages:

#### Maven (`pom.xml`)
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Jiaul21/Spring-Boot-jpa-helper</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.jiaul</groupId>
        <artifactId>jpa-helper</artifactId>
        <version>${version}</version>
    </dependency>
</dependencies>
```

---

## 🚀 Features & Usage

### 1. Auditable & Soft-Delete Base Entity (`BaseEntity`)

Extend [BaseEntity](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/entity/BaseEntity.java) on your JPA entities to automatically inherit:
1. **Audit Timestamps**: `createdAt` and `updatedAt` (`LocalDateTime`).
2. **Auditor Tracking**: `createdBy` and `updatedBy` (strings up to 320 characters, compatible with Spring Data Auditing).
3. **Soft-Delete Flags**: `deleted` (`boolean`) and `deletedAt` (`LocalDateTime`).
4. **Resilient Fallback**: If Spring Data Auditing is not configured, `@PrePersist` and `@PreUpdate` lifecycle hooks automatically populate `"SYSTEM"` and `LocalDateTime.now()`.
5. **Pre-Configured Hibernate Filter**: Annotated with `@FilterDef` and `@Filter(name = DeleteFilter.FILTER)`.

```java
import com.jiaul.jpahelper.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
}
```

> **Auditor Configuration:**
> By default, [BasicAuditorAware](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/config/BasicAuditorAware.java) is auto-configured to supply `"SYSTEM"` as the default auditor. To supply authenticated user identities (e.g. from Spring Security), simply define your own `@Bean public AuditorAware<String> auditorProvider()` bean, and `jpa-helper` will automatically yield to your custom auditor.

---

### 2. Dynamic Hibernate Soft-Delete Filters (`HibernateFilterManager`)

Unlike hardcoded JPA `@SQLDelete` or custom `WHERE` clauses, `jpa-helper` provides **dynamic, session-level filtering** via [HibernateFilterManager](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/hibernate/HibernateFilterManager.java).

#### Key Benefits:
1. **Zero Query Pollution**: You write standard JPQL/SQL queries (`SELECT s FROM Student s`) and Hibernate automatically appends `AND deleted = false` when the filter is active.
2. **Dynamic Control**: Toggle between active records, soft-deleted records (e.g. trash/recycle bin), or all records on the fly without changing repository methods.
3. **Spring Boot Auto-Configuration**: Automatically configured as a Spring bean when `jpa-helper.hibernate-filter.enabled=true` (enabled by default).

#### Configuration (`application.yaml`):
```yaml
jpa-helper:
  hibernate-filter:
    enabled: true # default is true
```

#### Usage Example:
```java
import com.jiaul.jpahelper.hibernate.HibernateFilterManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentService {

    private final HibernateFilterManager filterManager;
    private final StudentRepository studentRepository;

    public StudentService(HibernateFilterManager filterManager, StudentRepository studentRepository) {
        this.filterManager = filterManager;
        this.studentRepository = studentRepository;
    }

    public List<Student> getActiveStudents() {
        filterManager.enableDeletedFilter(); // filters out soft-deleted records (deleted = false)
        return studentRepository.findAll();
    }

    public List<Student> getTrashStudents() {
        filterManager.enableDeletedFilter(true); // returns ONLY soft-deleted records (deleted = true)
        return studentRepository.findAll();
    }

    public List<Student> getAllIncludingDeleted() {
        filterManager.disableDeletedFilter(); // disables filter, returns all records
        return studentRepository.findAll();
    }
}
```

#### Available Filter Methods:
| Method | Description |
|---|---|
| `enableDeletedFilter()` | Excludes soft-deleted records (`deleted = false`). |
| `enableDeletedFilter(boolean deleted)` | Filters by exact boolean (`true` for recycle bin, `false` for active). |
| `disableDeletedFilter()` | Disables filter; queries return both active and soft-deleted rows. |
| `isDeletedFilterEnabled()` | Returns `true` if soft-delete filter is currently active. |
| `enableFilter(filter, value)` | Enables any custom [HibernateFilterable](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/hibernate/HibernateFilterable.java) implementation with parameter value. |
| `disableFilter(filter)` | Disables any custom filter definition. |
| `isFilterEnabled(filter)` | Checks if a custom filter definition is active. |

---

### 3. Unified Entity Helper Architecture (`EntityHelper` & `EntityHelperImpl`)

`jpa-helper` provides a complete service-layer facade through [EntityHelper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/EntityHelper.java) and [EntityHelperImpl](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/EntityHelperImpl.java). Instead of writing boilerplate service methods for repetitive CRUD, bulk updating, and dynamic multi-level joins, your entity helpers inherit a complete operational stack.

#### Modular Architectural Stack:
1. **[Helper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/Helper.java)**: Abstract base component that extracts and caches JPA Metamodel attributes, entity name, and primary key name (`idName`) at startup with zero runtime reflection overhead.
2. **[BaseHelper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/base/BaseHelper.java)** / **[BaseHelperImpl](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/base/BaseHelperImpl.java)**: Core CRUD, existence validation (`existsByIdOrThrow`), projection lookups (`findById(id, DTO.class)`), ID verification, and transactional proxies.
3. **[UpdateHelper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/updater/UpdateHelper.java)** / **[UpdateHelperImpl](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/updater/UpdateHelperImpl.java)**: Targeted single-field updates, dynamic map updates, and high-performance CASE-WHEN bulk batch updates.
4. **[SelectHelper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/selector/SelectHelper.java)** / **[SelectHelperImpl](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/selector/SelectHelperImpl.java)**: Dynamic nested relation selector (`fetchByID`, `fetchBy`, `fetchByIDIn`) with Cartesian product protection and metamodel join caching.
5. **[EntityHelper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/EntityHelper.java)** / **[EntityHelperImpl](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/EntityHelperImpl.java)**: Top-level unified facade uniting the entire stack.

#### Quick Implementation in Consumer Projects:

##### 1. Define the Repository extending `BaseRepository`:
Extend [BaseRepository](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/repository/BaseRepository.java) to inherit Spring Data JPA CRUD, criteria specifications, and polymorphic ID batch querying:
```java
package com.example.repository;

import com.jiaul.jpahelper.repository.BaseRepository;
import com.example.entity.Student;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends BaseRepository<Student, Long> {
    // Custom Spring Data queries if needed
}
```

##### 2. Define the Helper Interface extending `EntityHelper`:
```java
package com.example.service;

import com.jiaul.jpahelper.service.EntityHelper;
import com.example.entity.Student;

public interface StudentHelper extends EntityHelper<Student, Long> {
    // Custom domain helper methods if needed
}
```

##### 3. Implement the Helper Service extending `EntityHelperImpl`:
```java
package com.example.service.impl;

import com.jiaul.jpahelper.service.EntityHelperImpl;
import com.example.entity.Student;
import com.example.repository.StudentRepository;
import com.example.service.StudentHelper;
import org.springframework.stereotype.Service;

@Service
public class StudentHelperImpl extends EntityHelperImpl<Student, Long> implements StudentHelper {

    public StudentHelperImpl(StudentRepository repository) {
        super(Student.class, repository);
    }
}
```

##### 4. Inject and Use in Controllers or Services:
Because `StudentHelper` extends `EntityHelper`, your application has instant access to all CRUD, dynamic fetch selectors, and bulk update operations through a single injected bean:

```java
package com.example.controller;

import com.example.entity.Student;
import com.example.service.StudentHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentHelper studentHelper;

    public StudentController(StudentHelper studentHelper) {
        this.studentHelper = studentHelper;
    }

    // 1. Dynamic fetch join by ID (SelectHelper feature)
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable Long id) {
        Student student = studentHelper.fetchByID(id, List.of("department", "profile.advisor", "courses.teacher"));
        return ResponseEntity.ok(student);
    }

    // 2. Existence verification (BaseHelper feature)
    @GetMapping("/{id}/check")
    public ResponseEntity<Void> checkStudent(@PathVariable Long id) {
        studentHelper.existsByIdOrThrow(id); // throws EntityNotFoundException if not found
        return ResponseEntity.ok().build();
    }

    // 3. Multi-condition search with joins (SelectHelper feature)
    @PostMapping("/search")
    public ResponseEntity<List<Student>> search(@RequestBody Map<String, Object> filters) {
        List<Student> results = studentHelper.fetchBy(filters, List.of("department"));
        return ResponseEntity.ok(results);
    }

    // 4. In-place managed update (UpdateHelper feature)
    @PatchMapping("/{id}/email")
    public ResponseEntity<Student> updateEmail(@PathVariable Long id, @RequestParam String newEmail) {
        Student updated = studentHelper.updateEntity(id, student -> student.setEmail(newEmail));
        return ResponseEntity.ok(updated);
    }

    // 5. Direct atomic attribute update (UpdateHelper feature)
    @PutMapping("/{id}/status")
    public ResponseEntity<Integer> updateStatus(@PathVariable Long id, @RequestParam String status) {
        int updatedCount = studentHelper.updateFieldByID(id, "status", status);
        return ResponseEntity.ok(updatedCount);
    }
}
```

---

### 4. Dynamic Fetch Joins & Query Selector (`SelectHelper`)

[SelectHelper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/selector/SelectHelper.java) eliminates `LazyInitializationException` and N+1 query problems by allowing callers to specify any arbitrary list of dotted relation paths to eagerly initialize via `LEFT JOIN FETCH`.

#### Key Innovations:
1. **Zero-Allocation Single Condition**: `fetchBy("email", value, joins)` builds queries directly without creating intermediate `Map` objects.
2. **Automatic Condition Joins**: If filtering by a nested relation property (e.g. `department.code`), `SelectHelper` automatically adds `LEFT JOIN FETCH e.department` even if omitted from `joinFields`.
3. **Cartesian Product Protection**: If multiple collection relations are requested (e.g. `courses` and `clubs`), it groups requested paths and joins only the first collection group to avoid Hibernate's `MultipleBagFetchException` and exponential row multiplication.
4. **Metamodel Caching**: Validates paths dynamically against the JPA Metamodel and caches validated relations in concurrent sets for near-zero runtime overhead.

#### Usage Examples:
```java
@Service
public class StudentQueryService {

    private final StudentHelper studentHelper;

    public StudentQueryService(StudentHelper studentHelper) {
        this.studentHelper = studentHelper;
    }

    // 1. Fetch single student with multi-level nested relations
    public Student getStudentDetails(Long id) {
        return studentHelper.fetchByID(id, List.of("department", "profile.advisor", "courses.teacher"));
    }

    // 2. Fetch by single condition with automatic relation join
    public List<Student> getStudentsByDeptCode(String code) {
        // "department" is automatically joined because "department.code" is in the condition
        return studentHelper.fetchBy("department.code", code, List.of("courses"));
    }

    // 3. Fetch by multiple conditions (Map)
    public List<Student> searchStudents(String name, String bloodGroup) {
        Map<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("name", name);
        conditions.put("profile.bloodGroup", bloodGroup);
        return studentHelper.fetchBy(conditions, List.of("department", "profile.advisor"));
    }

    // 4. Batch fetch by IDs with relations
    public List<Student> getStudentsByIds(Collection<Long> ids) {
        return studentHelper.fetchByIDIn(ids, List.of("department", "profile"));
    }

    // 5. Fetch first match safely (no NonUniqueResultException)
    public Student getByEmail(String email) {
        return studentHelper.fetchOne("email", email, List.of("profile"));
    }
}
```

#### Available Selector Methods:
| Method | Description |
|---|---|
| `fetchByID(id, joinFields)` | Fetches entity by primary key with relations. Direct L1 cache hit if `joinFields` is empty. |
| `fetchByID(id)` | Fetches entity by ID using direct L1 cache lookup. |
| `fetchBy(cName, cValue, joinFields)` | Queries entities matching a single condition with optional eager relations. |
| `fetchBy(conditions, joinFields)` | Queries entities matching multiple `AND` conditions with optional eager relations. |
| `fetchByIDIn(ids, joinFields)` | Batch fetches entities where `id(e) IN :ids` with relations. |
| `fetchOne(cName, cValue, joinFields)` | Returns the first matching entity or `null` without throwing `NonUniqueResultException`. |
| `fetchOne(conditions, joinFields)` | Returns the first matching entity from multiple conditions or `null`. |
| `buildFetchQuery(...)` | Returns configured `TypedQuery<E>` for custom pagination or streaming. |
| `buildFetchByIDInQuery(ids, joinFields)`| Returns configured `TypedQuery<E>` for `IN :ids` queries. |

---

### 5. High-Performance Direct & Bulk Updates (`UpdateHelper`)

[UpdateHelper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/updater/UpdateHelper.java) provides both in-place managed mutations and atomic SQL/JPQL bulk updates. Direct updates clear the persistence context (`entityManager.clear()`) after execution to prevent stale L1 cache data.

#### Key Update Strategies:

##### 1. Managed In-Place Updates:
Loads the entity, applies mutations inside a lambda, and lets dirty checking commit changes:
```java
Student updated = studentHelper.updateEntity(studentId, student -> {
    student.setName("Updated Name");
    student.setEmail("updated@example.com");
});
```

##### 2. Direct Attribute & Condition Updates:
Updates columns directly in SQL without loading entities into memory:
```java
// Single field by ID
studentHelper.updateFieldByID(studentId, "status", "ACTIVE");

// Multiple fields across ID collection
studentHelper.updateFieldsByIDIn(List.of(1L, 2L, 3L), Map.of("status", "INACTIVE", "updatedBy", "ADMIN"));

// Multi-field update by condition map
studentHelper.updateFieldsByConditions(
    Map.of("status", "ALUMNI"),
    Map.of("graduationYear", 2024, "department.code", "CSE")
);
```

##### 3. High-Performance Bulk CASE-WHEN Batch Updates:
Updates dozens or hundreds of different entities with different values in a **single atomic SQL query** instead of executing separate statements in a loop. Application DTOs or Java records can directly implement [IdValues](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/dao/IdValues.java) or [IdFieldValue](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/dao/IdFieldValue.java):

1. **Uniform (Even) Fields via `IdValues`:** Updates the same set of columns across all rows. Any DTO or Java record can implement `IdValues`:
```java
// Step 1: DTO implements IdValues
public record StudentGradeDto(Long studentId, String grade, Integer credits) implements IdValues {
    @Override
    public Object getID() { return studentId; }

    @Override
    public List<Object> getValues() { return List.of(grade, credits); }
}

// Step 2: Batch update all DTO rows in 1 atomic query:
studentHelper.updateBulkEvenFields(List.of("grade", "credits"), studentGradeDtoList);
```

2. **Heterogeneous (Uneven) Fields via `IdFieldValue`:** Different rows update different subsets of columns; unmentioned columns retain their existing database values via `ELSE column`:
```java
// Step 1: Patch DTO implements IdFieldValue
public record StudentPatchDto(Long studentId, String email, String phone) implements IdFieldValue {
    @Override
    public Object getID() { return studentId; }

    @Override
    public Map<String, Object> getFieldValue() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (email != null) map.put("email", email);
        if (phone != null) map.put("phone", phone);
        return map;
    }
}

// Step 2: Batch update heterogeneous DTO rows in 1 atomic query:
studentHelper.updateBulkUnevenFields(patchDtoList);
```

#### Available Updater Methods:
| Method | Description |
|---|---|
| `updateEntity(id, updater)` | Loads managed entity, applies consumer mutation, and returns updated entity. |
| `updateFieldByID(id, field, value)` | Direct JPQL single attribute update by primary key. |
| `updateFieldsByIDIn(ids, fields)` | Direct JPQL multi-attribute update across an ID collection. |
| `updateField(field, val, cName, cVal)` | Direct JPQL single attribute update by condition. |
| `updateFieldsByConditions(fields, conds)`| Direct JPQL multi-attribute update by multiple `AND` conditions. |
| `updateBulkEvenFields(fields, values)` | High-performance single-query batch update for uniform fields using SQL `CASE`. |
| `updateBulkUnevenFields(dataList)` | High-performance single-query batch update for heterogeneous fields using SQL `CASE ... ELSE`. |

---

### 6. Type Conversion Utility (`Converter`)

[Converter](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/utility/Converter.java) converts raw String inputs (e.g. from HTTP query parameters, filter strings, or form inputs) into strongly-typed Java objects based on target classes or JPA entity metamodel attributes.

#### Supported Conversions:
1. **Primitive & Wrapper Types**: `Long`, `Integer`, `Short`, `Byte`, `Double`, `Float`, `Boolean`, `Character`, and `String`.
2. **Temporal & Date-Time Types**: `LocalDate`, `LocalDateTime`, and `LocalTime` (ISO-8601 formatted).
3. **Unique Identifiers**: `UUID` from standard UUID string representations.
4. **Enumerations**: Dynamic `Enum.valueOf(enumType, string)` parsing.
5. **Metamodel-Driven Type Resolution**: Automatically looks up entity attribute types via JPA `EntityType<?>` metamodel introspection.

#### Usage Examples:
```java
import com.jiaul.jpahelper.utility.Converter;

// 1. Direct type conversion
Long id = (Long) Converter.convert("123", Long.class);
LocalDate date = (LocalDate) Converter.convert("2026-11-04", LocalDate.class);
UUID uuid = (UUID) Converter.convert("c9a646d3-9c61-4cc9-bc55-e4604e0e4708", UUID.class);

// 2. Conversion using JPA Metamodel attribute type discovery
EntityType<Student> studentMeta = entityManager.getMetamodel().entity(Student.class);
Object typedValue = Converter.convertValue("credits", "4", studentMeta); // returns Integer 4
```

---

### 7. Spring Boot Auto-Configuration & Auditing Setup

`jpa-helper` automatically configures essential beans via [JpaHelperAutoConfiguration](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/config/JpaHelperAutoConfiguration.java):

#### Auto-Configured Components:
1. **Hibernate Filter Manager**: Automatically registered when `EntityManager` is present and `jpa-helper.hibernate-filter.enabled` is `true` (default). You can toggle it off in `application.yaml`:
   ```yaml
   jpa-helper:
     hibernate-filter:
       enabled: false
   ```
2. **Fallback Auditor Provider**: [BasicAuditorAware](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/config/BasicAuditorAware.java) provides `"SYSTEM"` as the default author if Spring Security or another `AuditorAware` bean is absent.
3. **Seamless Custom Security Integration**: To supply the logged-in user identity from Spring Security, declare your own `AuditorAware<String>` bean — `BasicAuditorAware` automatically backs off thanks to `@ConditionalOnMissingBean`:
   ```java
   @Bean
   public AuditorAware<String> auditorProvider() {
       return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
               .filter(Authentication::isAuthenticated)
               .map(Authentication::getName)
               .or(() -> Optional.of("SYSTEM"));
   }
   ```

---

## 🛠️ Building From Source

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Jiaul21/Spring-Boot-jpa-helper.git
   cd Spring-Boot-jpa-helper
   ```
2. **Run test suite:**
   ```bash
   mvn clean test
   ```
3. **Build and install JARs to local `~/.m2` repository:**
   ```bash
   mvn clean install
   ```
