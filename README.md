# 🌱 QueryKeeper
**SQL Query Monitoring for JPA Tests (Annotation-driven, Lightweight)**

QueryKeeper is a lightweight testing utility for verifying SQL activity in `Spring Boot` + `JPA` projects.  
It uses intuitive annotations to monitor **query count**, **execution time**, and **unintended DB access**,
Without relying on external agents or JDBC proxies.

> ✅ Catch performance regressions during refactoring <br>
> ✅ Use annotations like `@ExpectQuery`, `@ExpectLazyLoad`, `@ExpectTime` <br>
> ✅ Detect N+1 queries, unexpected lazy loads, and slow queries during test execution <br>
> ✅ No agents, no proxies <br>

🇰🇷 [Korean](./README.ko.md)

---

## 1️⃣ Features

| Annotation            | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `@EnableQueryKeeper` | Activates all QueryKeeper test assertions                                |
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
> All SQL queries including `SELECT NEXT VALUE FOR` (e.g. for sequences) are counted.  <br>

### `@ExpectLazyLoad`
Monitors lazy-loading behavior and optionally fails if unexpected access or exceptions occur.
- **Parameters:**
  - `entity` *(optional, default: "")* — Specific entity class name to monitor (e.g. `"User"`). If omitted, all entities are included.
  - `maxCount` *(optional, default: 0)* — Maximum allowed number of lazy loads during the test
  - `includeException` *(optional, default: true)* — Whether to fail the test when a `LazyInitializationException` is detected.

- **How it works:**  
This annotation detects when JPA entities lazily load related data at runtime. If the test accesses an uninitialized proxy or triggers a `LazyInitializationException`, it can be recorded and validated.
It observes method executions and captures lazy-loading activity without requiring any changes to your application code.<br>
Use this to catch unintended lazy loads and ensure that your entity relationships are explicitly fetched when needed. <br>
> ⚠️ Lazy-loading is tracked via AOP. <br>
> Make sure the lazy field access occurs inside a Spring-managed bean (e.g., a `@Service` method).  
> Direct access from within the test method may not be detected.

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

QueryKeeper uses SLF4J for logging.  
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
    testImplementation 'com.querykeeper:querykeeper:1.1.0'
}
```

### B. Use standalone JAR

```groovy
testImplementation files('libs/querykeeper-1.1.0.jar')
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

> **Note**: This example intentionally triggers some annotation failures to showcase QueryKeeper’s detection features

```java
@Test
@ExpectQuery(select = 1, insert = 1) // ❌ fail
@ExpectTime(500)                     // ✅ pass
@ExpectNoTx(strict = false)          // ✅ pass
@ExpectNoDb                          // ❌ fail
void testCombinedAssertions() {
    User user = new User("Alice", "alice@example.com");
    user.addRole(new Role("ADMIN"));
    user.addRole(new Role("USER"));
    userRepository.save(user);
    userRepository.findAll();

    entityManager.clear();
    List<User> users = userRepository.findAll();
    users.get(0).getRoles().size();

    int sum = 0;
    for (int i = 0; i < 1000; i++)
        sum += i;
    assertThat(sum).isGreaterThan(0);
}

@Test
@ExpectLazyLoad(maxCount = 0)        // ❌ fail
void testLazyLoad() {
    userService.triggerLazyException();
}
```

### Output Example

> **Note**: Log format is simplified for clarity. In actual tests, timestamps and class names will appear in SLF4J format.

```text
UserRepositoryTest > testCombinedAssertions() STANDARD_OUT
    [QueryKeeper] ▶ ExpectNoTx ✓ PASSED - No transaction in testCombinedAssertions()
    [QueryKeeper] ▶ ExpectTime ✓ PASSED - testCombinedAssertions took 10ms (expected <= 500ms)
    [QueryKeeper] ▶ ExpectQuery X FAILED
    --------------------------------------------------------
    Expected - (SELECT: 1, INSERT: 1), Actual - (SELECT: 4, INSERT: 3)
    --------------------------------------------------------
     Total Queries: 7
    --------------------------------------------------------
    1. [SELECT] (0 ms)
    SQL     : select next value for users_seq
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:43
    --------------------------------------------------------
    2. [SELECT] (0 ms)
    SQL     : select next value for roles_seq
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:43
    --------------------------------------------------------
    3. [INSERT] (1 ms)
    SQL     : insert into users (email,name,id) values (?,?,?)
    Params  : {1=alice@example.com, 2=Alice, 3=2}
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:43
    --------------------------------------------------------
    4. [INSERT] (0 ms)
    SQL     : insert into roles (name,user_id,id) values (?,?,?)
    Params  : {1=ADMIN, 2=2, 3=2}
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:43
    --------------------------------------------------------
    5. [INSERT] (1 ms)
    SQL     : insert into roles (name,user_id,id) values (?,?,?)
    Params  : {1=USER, 2=2, 3=3}
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:43
    --------------------------------------------------------
    6. [SELECT] (1 ms)
    SQL     : select u1_0.id,u1_0.email,u1_0.name from users u1_0
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:44
    --------------------------------------------------------
    7. [SELECT] (0 ms)
    SQL     : select u1_0.id,u1_0.email,u1_0.name from users u1_0
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:47
    --------------------------------------------------------

    [QueryKeeper] ▶ ExpectNoDb X FAILED - 7 DB queries were executed in testCombinedAssertions()
    [QueryKeeper] ▶ testCombinedAssertions failed with 2 assertion(s)
    [QueryKeeper] ▶ SELECT mismatch: expected=1, actual=4
    [QueryKeeper] ▶ Expected no DB access, but found 7 queries

UserRepositoryTest > testLazyLoad() STANDARD_OUT
    [QueryKeeper] ▶ LazyLoadException (!) DETECTED - Entity: com.example.demo.User, Field: roles
    [QueryKeeper] ▶ testLazyLoad failed with 1 assertion(s)
    [QueryKeeper] ▶ ExpectLazyLoad X FAILED - Entity: Role, Expected Max: 0, Actual: 0, Exception: true
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
    testImplementation 'com.querykeeper:querykeeper:1.1.0'
}
```

---

<details>
<summary>SEO</summary>
spring boot jpa query count  <br>
hibernate query assertion  <br>
junit performance test for SQL  <br>
springboot prevent n+1 queries  <br>
jpa test query logging  <br>
junit measure sql execution time  <br>
test if service uses cache instead of db  <br>
custom datasource jdbc tracking  <br>
jdbc proxy alternative for JPA testing  <br>
jpa query count assertion  <br>
jpa lazyinitializationexception unit test  <br>
spring boot test assert no sql query  <br>
jpa fetch join verification test  <br>
</details>
