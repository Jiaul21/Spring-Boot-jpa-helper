# Developer & Contributor Guide (`jpa-helper`)

This guide provides technical reference and architectural principles for maintaining and extending `jpa-helper`.

---

## 🏛️ Architecture & Principles

`jpa-helper` provides production-grade JPA abstractions, auditable base entities, dynamic Hibernate filters, and advanced query/update engines without classpath pollution:

### 1. Technology Baseline & Compatibility Requirements
1. **Java Baseline**: Java 17 minimum (`--release 17`). Fully verified on LTS versions 17, 21, and 25+.
2. **Spring Boot Compatibility**: Spring Boot `3.0.0` or greater (`3.0.0+`, including `3.x`, `4.x+`, and future releases).
3. **Hibernate ORM Baseline**: Requires Hibernate `6.0+` (leveraging modern HQL standard features such as `id(e)` and session-level `@FilterDef`).
4. **Native Hibernate 6+ Support**: All Spring Boot 3.x and future releases natively bundle Hibernate 6.x+ by default:
   - Spring Boot 3.0.x $\rightarrow$ Hibernate 6.1
   - Spring Boot 3.1.x $\rightarrow$ Hibernate 6.2
   - Spring Boot 3.2.x $\rightarrow$ Hibernate 6.4
   - Spring Boot 3.3.x $\rightarrow$ Hibernate 6.5
   - Spring Boot 3.4.x $\rightarrow$ Hibernate 6.6
5. **Jakarta EE Namespace**: Uses `jakarta.persistence.*` (Jakarta Persistence 3.0+), which is the standard across Spring Boot 3.0.0 and higher.

### 2. Zero Classpath Pollution
1. `jakarta.persistence-api`, `hibernate-core`, `spring-data-jpa`, `jakarta.transaction-api`, `spring-boot-autoconfigure`, and `slf4j-api` are marked `<scope>provided</scope>` and `<optional>true</optional>`.
2. Consumer applications retain complete control over their framework versions without dependency conflicts or JAR bloat.

### 3. Standard Thin JAR
1. Packaged as a standard library JAR with attached `-sources.jar` and `-javadoc.jar`.
2. Never apply `spring-boot-maven-plugin` repackaging on this module so it remains fully linkable by consumer projects.

### 4. Auditing & Soft-Delete Architecture (`BaseEntity`)
1. [BaseEntity](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/entity/BaseEntity.java) is annotated with `@MappedSuperclass` and `@EntityListeners(AuditingEntityListener.class)`.
2. Defines `@FilterDef(name = DeleteFilter.FILTER)` and `@Filter(name = DeleteFilter.FILTER, condition = DeleteFilter.CONDITION)`.
3. Implements resilient fallback hooks (`@PrePersist` and `@PreUpdate`) that supply safe defaults (`"SYSTEM"`, `LocalDateTime.now()`) if an application lacks an `AuditorAware` bean or runs in isolated unit tests.

### 5. Dynamic Session-Level Filters (`HibernateFilterManager`)
1. Dynamic filters operate at the Hibernate `Session` level via [HibernateFilterManager](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/hibernate/HibernateFilterManager.java).
2. Decoupled via [HibernateFilterable](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/hibernate/HibernateFilterable.java) interface, allowing consumers to register custom filters beyond [DeleteFilter](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/hibernate/DeleteFilter.java) (e.g. multi-tenant `tenantId = :tenantId`).
3. Filter status is scoped per active persistence context transaction.

### 6. Modular Helper Service Hierarchy
The helper service suite follows a modular inheritance chain:
1. [Helper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/Helper.java): Abstract metamodel resolver caching `entity`, `entityName`, and `idName` using JPA Metamodel API.
2. [BaseHelper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/base/BaseHelper.java) / [BaseHelperImpl](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/base/BaseHelperImpl.java): Basic CRUD operations, existence checks, projection lookups, and programmatic transaction boundaries (`transactionalProxy`) with automatic commit and rollback.
3. [UpdateHelper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/updater/UpdateHelper.java) / [UpdateHelperImpl](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/updater/UpdateHelperImpl.java): Targeted single-field updates, dynamic map updates, and CASE-WHEN bulk batch updates.
4. [SelectHelper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/selector/SelectHelper.java) / [SelectHelperImpl](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/selector/SelectHelperImpl.java): Dynamic nested fetch selector with Cartesian product prevention and metamodel relation caching.
5. [EntityHelper](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/EntityHelper.java) / [EntityHelperImpl](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/service/EntityHelperImpl.java): Unified facade uniting all capabilities into a single inject-ready helper.

### 7. Query Engine & Selector Internals (`SelectHelperImpl`)
1. **N+1 Problem Elimination at Once**: Constructs dynamic JPQL `LEFT JOIN FETCH` clauses on the fly, loading parent entities and all requested multi-level nested relations in a single database round-trip without triggering N+1 child queries or `LazyInitializationException`.
2. **Metamodel Caching**: Validates dotted paths against JPA Metamodel (`ManagedType<?>`, `Attribute<?, ?>`) and stores paths in thread-safe concurrent sets (`singleRelations` and `collectionRelations`) to eliminate reflection on repeated queries.
3. **Cartesian Product Protection**: Inspects all requested join paths. If multiple parallel collections are specified, it partitions paths by collection root and fetch-joins only the first collection group, preventing Hibernate `MultipleBagFetchException` and memory explosion.
4. **Zero-Allocation Single Condition**: `buildFetchQuery(String, Object, List)` constructs JPQL directly without wrapping inputs in intermediate `Map` instances.
5. **Dynamic Condition Joins**: If a condition path traverses an unjoined relation (e.g., `department.code`), `buildConditionClause` dynamically synthesizes and appends `LEFT JOIN FETCH e.department j0`.

### 8. Direct & Bulk Update Engine Internals (`UpdateHelperImpl`)
1. **Direct JPQL Execution**: Bypasses entity loading for massive throughput improvements on bulk attribute modifications.
2. **L1 Cache Invalidation**: Calls `entityManager.clear()` after any update modifying rows (`updated > 0`) to prevent stale entity state in subsequent queries within the same transaction.
3. **CASE-WHEN Bulk Expressions**:
   - `updateBulkEvenFields`: Generates `e.f = CASE id(e) WHEN :id_i THEN :v_i END` for uniform column updates.
   - `updateBulkUnevenFields`: Generates `e.f = CASE id(e) WHEN :id_i THEN :v_i ELSE e.f END` for heterogeneous updates, falling back to existing column values when a row omits a field.

### 9. Repository Layer Architecture (`BaseRepository`)
1. **Generic Repository Contract**: [BaseRepository](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/repository/BaseRepository.java) extends both `JpaRepository<E, ID>` and `JpaSpecificationExecutor<E>`.
2. **SpEL Entity Name Resolution**: Uses Spring Data's `#{#entityName}` SpEL expression to dynamically resolve the table/entity name in generic queries (`SELECT e FROM #{#entityName} e WHERE id(e) IN :ids`).
3. **Hibernate Polymorphic Identifier**: Leverages Hibernate 6's standard `id(e)` expression to query by primary key regardless of the entity's actual ID field name (`id`, `studentId`, `uuid`, etc.).

### 10. High-Performance Bulk Data Contracts (`IdValues` & `IdFieldValue`)
1. **Direct DTO/Record Implementation**: Consumer applications can have their DTOs or Java records directly implement [IdValues](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/dao/IdValues.java) or [IdFieldValue](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/dao/IdFieldValue.java).
2. **Zero-Reflection Parameter Extraction**: Values are streamed directly from getters into JPQL positional/named parameters, eliminating runtime reflection overhead.
3. **Uniform vs Heterogeneous Batching**:
   - `IdValues`: Best for tabular updates where all rows update identical columns.
   - `IdFieldValue`: Best for patch updates where each row modifies an arbitrary subset of columns.

### 11. Type Conversion Engine (`Converter`)
1. **Metamodel-Driven Type Introspection**: [Converter](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/utility/Converter.java) dynamically resolves target Java types using JPA `EntityType<?>` metamodel introspection.
2. **Rich Type Parsing**: Built-in support for primitives, wrapper types, Temporal dates (`LocalDate`, `LocalDateTime`, `LocalTime`), `UUID`, and Enums.
3. **Fail-Safe Fallback**: Throws clear `IllegalArgumentException` on unparseable inputs or unmapped types.

### 12. Auto-Configuration & Fallback Auditing (`JpaHelperAutoConfiguration` & `BasicAuditorAware`)
1. **Spring Boot Autoconfigure Import**: Configured in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` for instant plug-and-play capability.
2. **Conditional Filter Activation**: [JpaHelperAutoConfiguration](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/config/JpaHelperAutoConfiguration.java) activates `HibernateFilterManager` conditionally (`jpa-helper.hibernate-filter.enabled`, defaulting to `true`).
3. **Graceful Auditor Fallback**: [BasicAuditorAware](file:///d:/enterprise-system/spring-helper/jpa-helper/src/main/java/com/jiaul/jpahelper/config/BasicAuditorAware.java) is annotated with `@ConditionalOnMissingBean(AuditorAware.class)`, providing a default `"SYSTEM"` auditor that automatically backs off when a custom auditor bean is supplied.

### 13. Compile-Time Metamodel Synergy (`metamodel-generator`)
1. **CharSequence Interoperability**: `metamodel-generator` emits `ModelField<T, R>` instances implementing `java.lang.CharSequence`. Because `SelectHelper` and `UpdateHelper` accept property strings or `CharSequence`, metamodel constants (e.g. `StudentFields.department.name`) can be passed directly without `.toString()`.
2. **Zero-Reflection DTO Streaming**: The generated `toValues()` and `toNonNullMap()` methods feed directly into `IdValues` and `IdFieldValue` bulk update contracts, allowing developers to execute high-throughput batch SQL queries without reflection or manual map construction.
3. **Decoupled Architecture**: `jpa-helper` remains 100% decoupled from `metamodel-generator` at runtime; consumer applications choose whether to use raw strings or compile-time metamodels.

---

## 🚀 Common Commands

### 1. Build and Run Unit Tests
```bash
mvn clean test
```

### 2. Full Build with Javadoc and Source Packaging
```bash
mvn clean install
```
This compiles the code, executes tests, packages `-sources.jar` and `-javadoc.jar`, and installs the artifact to `~/.m2/repository/com/jiaul/jpa-helper/`.

---

## 📦 Distribution & Publishing

### 1. JitPack
1. Push tags (e.g. `v0.0.1`) to GitHub to enable instant JitPack resolution.
2. JitPack builds directly from the GitHub repository release commit.

### 2. GitHub Packages
1. Authenticate with GitHub Packages token.
2. Publish releases using GitHub Actions (`mvn deploy`).
