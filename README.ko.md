# QuerySentinel
JPA 쿼리 실행 검증 & 성능 테스트 어노테이션
**QuerySentinel**은 `Spring Boot` 기반 `JPA`, `Hibernate` 테스트 코드에서 실행되는 **SQL 쿼리 수**, **실행 시간**, **DB 접근 여부** 등을 어노테이션 기반으로 자동 검증하는 테스트 라이브러리입니다.

> ✅ 쿼리 성능 회귀를 테스트 단계에서 감지  
> ✅ `@ExpectQuery`, `@ExpectNoDb`, `@ExpectTime` 같은 직관적인 어노테이션으로 구현  
> ✅ `N+1 문제`, `불필요한 DB 호출`, `슬로우 쿼리`를 테스트 중 탐지

---

## 1️⃣ 기능 소개

| 어노테이션 | 기능 |
|-----------|------|
| `@EnableQuerySentinel` | 쿼리 감시 기능을 자동 설정 |
| `@ExpectQuery(select=1, insert=1)` | SELECT/INSERT/UPDATE/DELETE 쿼리 수 검증 |
| `@ExpectTime(300)` | 테스트 실행 시간 제한 (ms) |
| `@ExpectNoDb` | 테스트 중 DB 접근이 없어야 통과 |

---

## 2️⃣ 설치방법

#### A. 로컬 Maven에 배포 후 사용

```bash
make publish
```

```groovy
dependencies {
    testImplementation 'com.querysentinel:querysentinel:1.0.0'
}
```

#### B. 직접 JAR 파일을 사용하는 경우
```groovy
testImplementation files('libs/querysentinel-1.0.0.jar')
```

#### 코드예시

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

#### 테스트 출력 예시

```text
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

## 3️⃣ 권장 사용 환경
* Java 17+
* Spring Boot 3.2+
* Hibernate 6.3+
* JUnit 5.9+

> 이 라이브러리는 Spring Boot + JPA 환경을 전제로 아래 의존성이 함께 있어야 정상 동작합니다
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.querysentinel:querysentinel:1.0.0'
}
```

---

<details>
<summary> 관련 키워드 </summary>

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
