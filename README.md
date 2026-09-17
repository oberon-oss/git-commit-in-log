# Git Commit in Log (`git-commit-in-log`)

## Build status:

[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_git-commit-in-log&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=oberon-oss_git-commit-in-log)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_git-commit-in-log&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=oberon-oss_git-commit-in-log)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_git-commit-in-log&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=oberon-oss_git-commit-in-log)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_git-commit-in-log&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=oberon-oss_git-commit-in-log)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_git-commit-in-log&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=oberon-oss_git-commit-in-log)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_git-commit-in-log&metric=coverage)](https://sonarcloud.io/summary/new_code?id=oberon-oss_git-commit-in-log)

[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_git-commit-in-log&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=oberon-oss_git-commit-in-log)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_git-commit-in-log&metric=bugs)](https://sonarcloud.io/summary/new_code?id=oberon-oss_git-commit-in-log)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_git-commit-in-log&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=oberon-oss_git-commit-in-log)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_git-commit-in-log&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=oberon-oss_git-commit-in-log)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_git-commit-in-log&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=oberon-oss_git-commit-in-log)

---

**`git-commit-in-log`** is a lightweight Java library that loads Git repository and build metadata (such as commit hashes, branch names, build timestamps, and
dirty flags) from a `git.properties` file generated at build time and exposes them through SLF4J's **Mapped Diagnostic Context (MDC)**.

By storing Git metadata in the MDC, you can seamlessly include versioning and commit details in your application log patterns across logging frameworks (such as
**Logback**, **Log4j 2**, or **Spring Boot Logging**) without passing them manually to every log statement.

---

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation & Build Setup](#installation--build-setup)
    - [1. Add Dependency](#1-add-dependency)
    - [2. Configure `git-commit-id-maven-plugin`](#2-configure-git-commit-id-maven-plugin)
- [Usage](#usage)
    - [Loading Properties from `git.properties`](#loading-properties-from-gitproperties)
    - [Loading Custom Property Maps](#loading-custom-property-maps)
    - [Application Startup Integration](#application-startup-integration)
- [Logging Framework Configuration](#logging-framework-configuration)
    - [Logback (`logback.xml`)](#logback-logbackxml)
    - [Log4j 2 (`log4j2.xml`)](#log4j-2-log4j2xml)
    - [Spring Boot (`application.yml` / `application.properties`)](#spring-boot-applicationyml--applicationproperties)
- [Supported Git Properties](#supported-git-properties)
- [License](#license)

---

## Features

- **Automated SLF4J MDC Population**: Populate all Git properties into SLF4J's `MDC` using a single method call.
- **26 Standard Git Metadata Properties**: Full support for commit hashes (abbreviated and full), branch names, tags, dirty state, committer info, and build
  timestamps.
- **Built-in Type Converters**: Out-of-the-box converters for `String`, `Integer`, `Boolean`, and ISO `LocalDateTime`.
- **Sensible Defaults & Null Safety**: Missing or empty properties are safely assigned a default placeholder (`"** NOT FOUND **"`).
- **Framework Agnostic**: Works with any SLF4J-compatible logging framework (Logback, Log4j 2, java.util.logging via SLF4J, etc.).

---

## Requirements

- **Java**: 21 or newer
- **SLF4J**: 2.x (or compatible SLF4J binding)

---

## Installation & Build Setup

### 1. Add Dependency

Add the dependency to your project's `pom.xml`:

```xml

<dependency>
    <groupId>eu.oberon-oss.tools</groupId>
    <artifactId>git-commit-in-log</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Configure `git-commit-id-maven-plugin`

Configure the [git-commit-id-maven-plugin](https://github.com/git-commit-id/git-commit-id-maven-plugin) in your `pom.xml` to generate the `git.properties` file
in the build output directory (classpath root):

```xml

<build>
    <plugins>
        <plugin>
            <groupId>io.github.git-commit-id</groupId>
            <artifactId>git-commit-id-maven-plugin</artifactId>
            <version>10.0.1</version>
            <executions>
                <execution>
                    <id>get-the-git-infos</id>
                    <goals>
                        <goal>revision</goal>
                    </goals>
                    <phase>initialize</phase>
                </execution>
            </executions>
            <configuration>
                <generateGitPropertiesFile>true</generateGitPropertiesFile>
                <generateGitPropertiesFilename>${project.build.outputDirectory}/git.properties</generateGitPropertiesFilename>
                <commitIdGenerationMode>full</commitIdGenerationMode>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## Usage

### Loading Properties from `git.properties`

At application startup, load `git.properties` from the classpath directly into SLF4J MDC using `GitCommitMdc`:

```java
import eu.oberon.oss.tools.logging.gitid.GitCommitMdc;

import java.io.IOException;
import java.io.InputStream;

public class Application {
    static void main(String[] args) {
        try (InputStream inputStream = Application.class.getResourceAsStream("/git.properties")) {
            if (inputStream != null) {
                GitCommitMdc.loadProperties(inputStream);
            }
        } catch (IOException e) {
            // handle or log exception
        }

        // SLF4J MDC is now populated with Git metadata for the following logs
    }
}
```

### Loading Custom Property Maps

You can also parse the properties into a map first or pass custom properties programmatically:

```java
import eu.oberon.oss.tools.logging.gitid.AbstractGitProperty;
import eu.oberon.oss.tools.logging.gitid.GitCommitMdc;
import eu.oberon.oss.tools.logging.gitid.GitPropertyNames;

import java.io.InputStream;
import java.util.Map;

void example() {
// 1. Read git.properties into an unmodifiable Map<GitPropertyNames, Object>
    try (InputStream is = Application.class.getResourceAsStream("/git.properties")) {
        Map<GitPropertyNames, Object> properties = AbstractGitProperty.loadGitProperties(is);

// Access typed properties or converters
        String branch = (String) properties.get(GitPropertyNames.GIT_BRANCH);

// 2. Put into MDC
        GitCommitMdc.

                loadProperties(properties);
    }
}
```

### Application Startup Integration

#### Spring Boot

In a Spring Boot application, initialize the MDC properties inside a startup listener:

```java
import eu.oberon.oss.tools.logging.gitid.GitCommitMdc;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

public class GitCommitMdcInitializer implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        try (InputStream inputStream = new ClassPathResource("git.properties").getInputStream()) {
            GitCommitMdc.loadProperties(inputStream);
        } catch (Exception e) {
            // git.properties might be absent in local development without a build step
        }
    }
}
```

Register it in `src/main/resources/META-INF/spring.factories` (or via `SpringApplication.addListeners(...)`):

```properties
org.springframework.context.ApplicationListener=com.example.GitCommitMdcInitializer
```

---

## Logging Framework Configuration

When `GitCommitMdc.loadProperties(...)` is invoked, keys matching the enum names in `GitPropertyNames` (e.g. `GIT_COMMIT_ID_ABBREV`, `GIT_BRANCH`) are placed in
SLF4J's MDC. You can reference them in your log pattern using `%X{KEY}`.

### Logback (`logback.xml`)

Access individual MDC properties using `%X{KEY}` or `%X{KEY:-fallback}`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{GIT_COMMIT_ID_ABBREV:-unknown}:%X{GIT_BRANCH:-unknown}] - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

**Example Log Output:**

```text
2026-09-17 13:34:18.488 [main] INFO  com.example.MyService [f452bda:main] - Service started successfully
```

### Log4j 2 (`log4j2.xml`)

In Log4j 2, reference MDC properties via `%X{KEY}` or `%mdc{KEY}`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} [%X{GIT_COMMIT_ID_ABBREV}:%X{GIT_BRANCH}] - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

### Spring Boot (`application.yml` / `application.properties`)

Configure log patterns directly in your Spring Boot application configuration:

**`application.yml`**:

```yaml
logging:
    pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{GIT_COMMIT_ID_ABBREV:-unknown}:%X{GIT_BRANCH:-unknown}] - %msg%n"
```

**`application.properties`**:

```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{GIT_COMMIT_ID_ABBREV:-unknown}:%X{GIT_BRANCH:-unknown}] - %msg%n
```

---

## Supported Git Properties

The following table lists all 26 Git properties supported by `GitPropertyNames`, their keys in `git.properties`, their corresponding MDC key, and their target
data type:

| MDC Key (`%X{...}`)            | `git.properties` Key           | Type            | Description                                       |
|:-------------------------------|:-------------------------------|:----------------|:--------------------------------------------------|
| `GIT_BRANCH`                   | `git.branch`                   | `String`        | Current Git branch (e.g. `main`)                  |
| `GIT_BUILD_HOST`               | `git.build.host`               | `String`        | Hostname of the build machine                     |
| `GIT_BUILD_TIME`               | `git.build.time`               | `LocalDateTime` | Build timestamp                                   |
| `GIT_BUILD_USER_EMAIL`         | `git.build.user.email`         | `String`        | Email address of the build user                   |
| `GIT_BUILD_USER_NAME`          | `git.build.user.name`          | `String`        | Name of the build user                            |
| `GIT_BUILD_VERSION`            | `git.build.version`            | `String`        | Project build version                             |
| `GIT_CLOSEST_TAG_COMMIT_COUNT` | `git.closest.tag.commit.count` | `Integer`       | Number of commits since closest tag               |
| `GIT_CLOSEST_TAG_NAME`         | `git.closest.tag.name`         | `String`        | Name of the closest tag                           |
| `GIT_COMMIT_AUTHOR_TIME`       | `git.commit.author.time`       | `LocalDateTime` | Commit author timestamp                           |
| `GIT_COMMIT_COMMITTER_TIME`    | `git.commit.committer.time`    | `LocalDateTime` | Commit committer timestamp                        |
| `GIT_COMMIT_ID_ABBREV`         | `git.commit.id.abbrev`         | `String`        | Abbreviated commit SHA (e.g. `f452bda`)           |
| `GIT_COMMIT_ID_DESCRIBE`       | `git.commit.id.describe`       | `String`        | Output of `git describe`                          |
| `GIT_COMMIT_ID_DESCRIBE_SHORT` | `git.commit.id.describe-short` | `String`        | Short output of `git describe`                    |
| `GIT_COMMIT_ID_FULL`           | `git.commit.id.full`           | `String`        | Full 40-character commit SHA                      |
| `GIT_COMMIT_MESSAGE_FULL`      | `git.commit.message.full`      | `String`        | Full commit message                               |
| `GIT_COMMIT_MESSAGE_SHORT`     | `git.commit.message.short`     | `String`        | Short / first line commit message                 |
| `GIT_COMMIT_TIME`              | `git.commit.time`              | `LocalDateTime` | Commit timestamp                                  |
| `GIT_COMMIT_USER_EMAIL`        | `git.commit.user.email`        | `String`        | Commit author email                               |
| `GIT_COMMIT_USER_NAME`         | `git.commit.user.name`         | `String`        | Commit author name                                |
| `GIT_DIRTY`                    | `git.dirty`                    | `Boolean`       | Whether repository has uncommitted changes        |
| `GIT_LOCAL_BRANCH_AHEAD`       | `git.local.branch.ahead`       | `Integer`       | Number of commits local branch is ahead of remote |
| `GIT_LOCAL_BRANCH_BEHIND`      | `git.local.branch.behind`      | `Integer`       | Number of commits local branch is behind remote   |
| `GIT_REMOTE_ORIGIN_URL`        | `git.remote.origin.url`        | `String`        | Git remote origin URL                             |
| `GIT_TAG`                      | `git.tag`                      | `String`        | Current tag name if on a tag                      |
| `GIT_TAGS`                     | `git.tags`                     | `String`        | List of tags pointing to current commit           |
| `GIT_TOTAL_COMMIT_COUNT`       | `git.total.commit.count`       | `Integer`       | Total commit count in repository history          |

*Note: If a property is missing or blank in `git.properties`, it defaults to `"** NOT FOUND **"` (`GitProperty.NOT_FOUND`).*

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
