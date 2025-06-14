# 🌱 QueryKeeper
**SQL Query Monitoring for JPA Tests (Annotation-driven, Lightweight)**

QueryKeeper is a lightweight testing utility for verifying SQL activity in `Spring Boot` + `JPA` projects.  
It uses intuitive annotations to monitor **query count**, **execution time**, and **unintended DB access**,
Without relying on external agents or JDBC proxies.

> ✅ No setup required. just add the library and annotate your tests (only `@EnableQueryKeeper` needed) <br>
> ✅ Catch performance regressions during refactoring <br>
> ✅ Use annotations like `@ExpectQuery`, `@ExpectDetachedAccess`, `@ExpectDuplicateQuery` <br>
> ✅ Detect N+1 queries, unexpected lazy loads, and slow queries during test execution <br>
> ✅ No agents, no proxies <br>

🇰🇷 [Korean](./README.ko.md)

---

## 1️⃣ Features

| Annotation                     | Description                                                                             |
|-------------------------------|------------------------------------------------------------------------------------------|
| `@EnableQueryKeeper`          | Activates all QueryKeeper test assertions                                                |
| `@ExpectQuery`                | Tracks and verifies the number of executed SQL queries during the test                   |
| `@ExpectDuplicateQuery`       | Fails the test if identical SQL queries (including parameters) are executed multiple times |
| `@ExpectDetachedAccess`       | Catches LazyInitializationException caused by accessing lazy fields outside a transaction|
| `@ExpectTime`                 | Asserts that the test completes within a specified time limit                            |
| `@ExpectNoDb`                 | Fails if any database interaction (e.g., query, update) is detected                      |
| `@ExpectNoTx`                 | Ensures the test runs outside of any transactional context                               |

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

### `@ExpectDuplicateQuery`
Detects and fails if the same SQL query is executed multiple times with identical parameters.
- **Parameters:**
  - `max` *(optional, default: 0)* — Maximum number of allowed duplicate queries
- **How it works:**  
  During test execution, QueryKeeper tracks each executed SQL statement along with its parameters.  
  If any identical query (including parameters) is executed more than once, it counts as a duplicate.  
  If the total number of duplicates exceeds the `max` value, the test fails.<br>
> This is useful for detecting inefficient patterns such as repeated queries inside loops or accidental N+1 issues.<br>

### `@ExpectDetachedAccess`
Detects unintended `LazyInitializationException` triggered by accessing lazy-loaded fields after the entity becomes detached.
- **Parameters:** *(none)*
- **How it works:**  
Captures and reports any `LazyInitializationException` during test execution, including the entity and field involved.<br>
  This is useful for verifying that no lazy-loading occurs outside of a transactional context.<br>
> ⚠️ It only detects invalid accesses that lead to `LazyInitializationException`. <br>

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
@ExpectDuplicateQuery                // ❌ fail
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
@ExpectDetachedAccess      // ❌ fail
void testDetachedAccess() {
    userService.triggerDetachedAccess();
}
```

### Output Example

```text
UserRepositoryTest > testDetachedAccess() STANDARD_OUT
    2025-01-01T12:00:00.000+00:00  INFO 7475 --- [    Test worker] c.q.junit.QueryKeeperExtension           : 
    [QueryKeeper] ▶ ExpectDetachedAccess X FAILED - Entity: Role
      • Field: roles
      • Access Path: User.roles
      • Root Entity: User

UserRepositoryTest > testCombinedAssertions() STANDARD_OUT
    2025-01-01T12:00:00.000+00:00  INFO 7475 --- [    Test worker] c.q.junit.QueryKeeperExtension           : 
    [QueryKeeper] ▶ ExpectNoTx ✓ PASSED - No transaction in testCombinedAssertions()
    [QueryKeeper] ▶ ExpectTime ✓ PASSED - testCombinedAssertions took 8ms (expected <= 500ms)
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
    3. [INSERT] (0 ms)
    SQL     : insert into users (email,name,id) values (?,?,?)
    Params  : {1=alice@example.com, 2=Alice, 3=2}
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:43
    --------------------------------------------------------
    4. [INSERT] (0 ms)
    SQL     : insert into roles (name,user_id,id) values (?,?,?)
    Params  : {1=ADMIN, 2=2, 3=2}
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:43
    --------------------------------------------------------
    5. [INSERT] (0 ms)
    SQL     : insert into roles (name,user_id,id) values (?,?,?)
    Params  : {1=USER, 2=2, 3=3}
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:43
    --------------------------------------------------------
    6. [SELECT] (0 ms)
    SQL     : select u1_0.id,u1_0.email,u1_0.name from users u1_0
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:44
    --------------------------------------------------------
    7. [SELECT] (0 ms)
    SQL     : select u1_0.id,u1_0.email,u1_0.name from users u1_0
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:47
    --------------------------------------------------------
    [QueryKeeper] ▶ ExpectNoDb X FAILED - 7 DB queries were executed in testCombinedAssertions()
    [QueryKeeper] ▶ ExpectDuplicateQuery X FAILED - Found 1 duplicate queries (allowed: 0)
      • Duplicate [2x] → select u1_0.id,u1_0.email,u1_0.name from users u1_0
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
