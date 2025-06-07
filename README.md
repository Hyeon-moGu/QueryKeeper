# QuerySentinel
JPA Query Verification & Performance Testing Annotations

**QuerySentinel** is a lightweight annotation-based library that helps validate **SQL query count**, **execution time**, and **database access** in `Spring Boot` and `JPA` test code.

> ✅ Catch performance regressions during refactoring <br>
> ✅ Expressive annotations like `@ExpectQuery`, `@ExpectNoDb`, `@ExpectTime` <br>
> ✅ Detect `N+1 queries`, `unnecessary database access`, and `performance bottlenecks` in your tests <br>

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

```
[QuerySentinel] ExpectTime PASSED - Method testUserQueries took 178ms (expected <= 300ms)

[QuerySentinel] Query Expectation PASSED - testUserQueries()
--------------------------------------------------------
[SELECT] (1 ms)
SQL     : select next value for users_seq
Caller  : com.example.demo.UserRepositoryTest#saveUser:33
--------------------------------------------------------
[INSERT] (1 ms)
SQL     : insert into users (email,name,id) values (?,?,?)
Params  : {1=alice@example.com, 2=Alice, 3=1}
Caller  : com.example.demo.UserRepositoryTest#saveUser:33
--------------------------------------------------------
[SELECT] (0 ms)
SQL     : select u1_0.id,u1_0.email,u1_0.name from users u1_0
Caller  : com.example.demo.UserRepositoryTest#loadUsers:37
--------------------------------------------------------
 Total Queries: 3
```

---

## 3️⃣ Recommended Environment

* Java 17+
* Spring Boot 3.2+
* Hibernate 6.3+
* JUnit 5.9+

> Note: This library assumes a Spring Boot + JPA environment.
> You must include the following dependencies:
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
springboot @Transactional query detection  <br>
jpa test query logging  <br>
junit measure sql execution time  <br>
test if service uses cache instead of db  <br>
detect unexpected database access in unit test

</details>

