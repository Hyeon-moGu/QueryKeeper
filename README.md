# 🔭 QuerySentinel
JPA Query Verification & Performance Testing Annotations

**QuerySentinel** is an annotation-based library for validating **SQL query count**, **execution time**, and **database access** in `Spring Boot` + `JPA` test code.  
It has no external APM or JDBC proxy dependencies. Instead, it wraps core JDBC components (`PreparedStatement`, `Connection`, and `DataSource`) directly for efficient and low-level query tracking.

> ✅ Catch performance regressions during refactoring  
> ✅ Intuitive annotations like `@ExpectQuery`, `@ExpectTime`, `@ExpectNoDb`  
> ✅ Detect `N+1 queries`, `unintended DB calls`, and `slow queries` during testing  
> ✅ No external agents — just pure, lightweight instrumentation

🇰🇷 [Korean](./README.ko.md)

---

## 1️⃣ Features

| Annotation                         | Description                                                      |
| ---------------------------------- | ---------------------------------------------------------------- |
| `@EnableQuerySentinel`             | Enables all QuerySentinel functionality                          |
| `@ExpectQuery(select=1, insert=1)` | Validates expected number of SELECT/INSERT/UPDATE/DELETE queries |
| `@ExpectTime(300)`                 | Fails if test method exceeds 300ms execution time                |
| `@ExpectNoDb`                      | Fails if any DB access occurs during test                        |

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

### Test Code Example

```java
@SpringBootTest
@EnableQuerySentinel
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    @ExpectQuery(select = 1, insert = 1)
    @ExpectTime(300)
    @ExpectNoDb
    void testUserQueries() {
        userRepository.save(new User("Alice", "alice@example.com"));
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
    }
}
```

### Test Output Example

```text
[QuerySentinel] ExpectTime ✅ PASSED - findAll_expect took 164ms (expected <= 300ms)

[QuerySentinel] ExpectQuery ✅ PASSED - findAll_expect()
--------------------------------------------------------
Total Queries: 3
--------------------------------------------------------
1. [SELECT] (1 ms)
SQL     : select next value for users_seq
Caller  : com.example.demo.UserRepositoryTest#saveUser:34
--------------------------------------------------------
2. [INSERT] (0 ms)
SQL     : insert into users (email,name,id) values (?,?,?)
Params  : {1=alice@example.com, 2=Alice, 3=1}
Caller  : com.example.demo.UserRepositoryTest#saveUser:34
--------------------------------------------------------
3. [SELECT] (0 ms)
SQL     : select u1_0.id,u1_0.email,u1_0.name from users u1_0
Caller  : com.example.demo.UserRepositoryTest#loadUsers:38
--------------------------------------------------------

[QuerySentinel] ExpectNoDb ❌ FAILED - 3 DB queries were executed in findAll_expect()
```

---

## 3️⃣ Recommended Environment

* Java 17+
* Spring Boot 3.2+
* Hibernate 6.3+
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

