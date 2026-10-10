# JPA Helper (`jpa-helper`)

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021%20%7C%2025+-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%2B-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![JitPack](https://jitpack.io/v/Jiaul21/Spring-Boot-jpa-helper.svg)](https://jitpack.io/#Jiaul21/Spring-Boot-jpa-helper)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A foundational, zero-overhead Spring Boot library providing reusable JPA components, pagination helpers, auditable entities, and repositories.

Designed as a **professional thin library JAR**:
- **Zero Classpath Pollution**: Framework dependencies (`spring-data-jpa`, `jakarta.persistence-api`, `hibernate-core`) are marked `provided` and `optional`. Your consumer application retains 100% control over Spring Data and Hibernate versions.
- **IDE Friendly**: Bundles compiled source JAR (`-sources.jar`) and Javadoc JAR (`-javadoc.jar`) for instant code completion and documentation.
- **Java Baseline**: Built for Java 17 minimum, fully compatible with Java 21, 25, and future LTS releases.
- **Core Helper Integration**: Built on top of `core-helper` for unified API responses, exceptions, and utility functions.

---

## 📦 Installation

### Option 1: Via JitPack (Recommended for Remote Consumption)

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

If you build locally via `mvn clean install`:

#### Maven (`pom.xml`)
```xml
<dependency>
    <groupId>com.jiaul</groupId>
    <artifactId>jpa-helper</artifactId>
    <version>${version}</version> <!-- e.g., 0.0.1-SNAPSHOT -->
</dependency>
```

---

## 🛠️ Building From Source

```bash
# Clean and test
mvn clean test

# Build and install to local ~/.m2 repository
mvn clean install
```
