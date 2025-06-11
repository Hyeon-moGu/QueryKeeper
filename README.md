# 🔭 QuerySentinel
JPA Query Verification & Performance Testing Annotations

**QuerySentinel** is an annotation-based library for verifying SQL behavior in `Spring Boot` + `JPA` tests.  
It tracks **query count**, **execution time**, and **database access** without relying on external agents or JDBC proxies.

> ✅ Catch performance regressions during refactoring <br>
> ✅ Intuitive annotations like `@ExpectQuery`, `@ExpectTime`, `@ExpectNoDb` , `@ExpectNoTx` <br>
> ✅ Detect `N+1 queries`, `unintended DB calls`, and `slow queries` during testing  <br>
> ✅ No external agents — just pure, lightweight instrumentation <br>

🇰🇷 [Korean](./README.ko.md)

---

## 1️⃣ Features

| Annotation            | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `@EnableQuerySentinel` | Activates all QuerySentinel test assertions                                |
| `@ExpectQuery`         | Tracks and verifies the number of executed SQL queries during the test     |
| `@ExpectLazyLoad`      | Detects unexpected lazy loading and captures LazyInitializationExceptions  |
| `@ExpectTime`          | Asserts that the test completes within a specified time limit              |
| `@ExpectNoDb`          | Fails if any database interaction (e.g., query, update) is detected        |
| `@ExpectNoTx`          | Ensures the test runs outside of any transactional context                 |

## Detailed Usage 

### `@ExpectQuery`
Logs and optionally verifies SQL queries executed during the test.
- **Parameters:**
  - `select` *(optional, default: -1)* — Expected number of SELECT queries
  - `insert` *(optional, default: -1)* — Expected number of INSERT queries
  - `update` *(optional, default: -1)* — Expected number of UPDATE queries
  - `delete` *(optional, default: -1)* — Expected number of DELETE queries
- **How it works:**  
By default, this annotation logs all executed SQL queries along with their parameters and execution times.
If one or more expected counts (select, insert, etc.) are specified (i.e., ≥ 0), the test will fail if the actual counts don't match. <br>

### `@ExpectLazyLoad`
Detects lazy-loading behavior and optionally fails on exceptions.
- **Parameters:**
  - `entity` *(optional, default: "")* — Specific entity class name to monitor (e.g. `"User"`). **If empty, all are monitored.**
  - `maxCount` *(optional, default: 0)* — Maximum allowed number of lazy loads
  - `includeException` *(optional, default: true)* — Whether to fail on `LazyInitializationException`

- **How it works:**  
  Tracks when a lazily loaded field is accessed. If an exception occurs or the number of lazy loads exceeds `maxCount`, the test fails. Helps enforce explicit fetching and avoid runtime errors. The entity parameter matches the simple class name. <br>

### `@ExpectTime`
Ensures the test completes within the given time.

- **Parameters:**
  - `value` *(required)* — Maximum allowed execution time in milliseconds

- **How it works:**  
 Measures total execution time of the test method, including all setup and database operations. Useful for catching performance regressions. <br>

### `@ExpectNoDb`

Asserts that no database queries are executed.

- **Parameters:** *(none)*

- **How it works:**  
  If any query (SELECT, INSERT, etc.) is executed, the test fails. Helpful for validating pure logic or cache-layer tests. <br>

### `@ExpectNoTx`
Ensures the test runs outside of a transaction.

- **Parameters:**
  - `strict` *(optional, default: true)* — If `true`, read-only transactions are also disallowed

- **How it works:**  
  Detects if a transaction is active during test execution. With `strict=true`, even `@Transactional(readOnly = true)` will cause the test to fail. <br>
---

## 2️⃣ Installation

### Logging Note

QuerySentinel uses SLF4J for logging.  
If you're using Spring Boot, no action is needed (Logback is included by default).  
For non-Spring Boot environments, be sure to include a compatible SLF4J backend:

```groovy
runtimeOnly 'ch.qos.logback:logback-classic:1.4.14'
```

### A. Publish to local Maven repository

```bash
make publish
```

```groovy
dependencies {
    testImplementation 'com.querysentinel:querysentinel:1.0.0'
}
```

### B. Use standalone JAR

```groovy
testImplementation files('libs/querysentinel-1.0.0.jar')
```

### Optional: Enhanced test logging configuration (in build.gradle)
```groovy
test {
    useJUnitPlatform()

    testLogging {
        events "passed", "skipped", "failed"
        showStandardStreams = true
    }
}
```

### Code Example

```java
@SpringBootTest
@EnableQuerySentinel
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @ExpectQuery(select = 1, insert = 1)  // Expected SELECT count is set to 1 to demonstrate a failure (actual: 2)
    @ExpectNoDb                           // Will fail since database access is performed in this test
    @ExpectTime(300)
    @ExpectNoTx(strict = false)
    void testUser() {
        saveUser();
        List<User> users = loadUsers();
        assertThat(users).hasSize(1);
    }

    private void saveUser() {
        userRepository.save(new User("Alice", "alice@example.com"));
    }

    private List<User> loadUsers() {
        return userRepository.findAll();
    }
}
```

### Output Example

```text
[QuerySentinel] ExpectNoTx ✅ PASSED - No transaction in testUser()
[QuerySentinel] ExpectTime ✅ PASSED - testUser took 262ms (expected <= 300ms)
[QuerySentinel] ExpectQuery ❌ FAILED
--------------------------------------------------------
Expected - SELECT: 1, INSERT: 1
Actual   - SELECT: 2, INSERT: 1
--------------------------------------------------------
Total Queries: 3
--------------------------------------------------------
1. [SELECT] (2 ms)
SQL     : select next value for users_seq
Caller  : com.example.demo.UserRepositoryTest#saveUser:36
--------------------------------------------------------
2. [INSERT] (1 ms)
SQL     : insert into users (email,name,id) values (?,?,?)
Params  : {1=alice@example.com, 2=Alice, 3=1}
Caller  : com.example.demo.UserRepositoryTest#saveUser:36
--------------------------------------------------------
3. [SELECT] (0 ms)
SQL     : select u1_0.id,u1_0.email,u1_0.name from users u1_0
Caller  : com.example.demo.UserRepositoryTest#loadUsers:40
--------------------------------------------------------

[QuerySentinel] ExpectNoDb ❌ FAILED - 3 DB queries were executed in testUser()
```

---

## 3️⃣ Recommended Environment

* Java 17+
* Spring Boot 2.7.x ~ Spring Boot 3.2+
* Hibernate 5.6.x ~ Hibernate 6.3+
* JUnit 5.9+

> Note: This library is designed for Spring Boot + JPA environments.
> Make sure the following dependencies are included in your project:
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.querysentinel:querysentinel:1.0.0'
}
```

---

<details>
<summary>Click to expand</summary>
spring boot jpa query count  <br>
hibernate query assertion  <br>
junit performance test for SQL  <br>
springboot prevent n+1 queries  <br>
jpa test query logging  <br>
junit measure sql execution time  <br>
test if service uses cache instead of db  <br>
custom datasource jdbc tracking  <br>
jdbc proxy alternative for JPA testing  <br>
</details>
