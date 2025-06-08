# 🔭 QuerySentinel
JPA 쿼리 실행 검증 & 성능 테스트 어노테이션

**QuerySentinel**은 `Spring Boot` + `JPA` 테스트 코드에서 실행되는 SQL 쿼리 수, 실행 시간, DB 접근 여부 등을 어노테이션 기반으로 검증하는 테스트 전용 라이브러리입니다.
외부 APM이나 JDBC 프록시 없이, 순수 Java 코드로 구현되었습니다. 핵심 JDBC 구성 요소(`PreparedStatement`, `Connection`, `DataSource`)를 직접 감싸 low-level에서 쿼리를 추적합니다.

> ✅ 쿼리 성능 회귀를 테스트 단계에서 감지 <br>
> ✅ `@ExpectQuery`, `@ExpectNoDb`, `@ExpectTime` 같은 직관적인 어노테이션으로 구현 <br>
> ✅ `N+1 문제`, `불필요한 DB 호출`, `느린 쿼리`를 테스트 중 탐지 <br>
> ✅ `PreparedStatement`, `Connection` 및 `DataSource`를 직접 래핑 <br>

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

### 로깅 주의사항

QuerySentinel은 로그 출력을 위해 SLF4J를 사용합니다.  
Spring Boot를 사용하는 경우 별도 설정이 필요하지 않습니다 (`spring-boot-starter-logging`에 포함)
Spring Boot가 아닌 환경에서는 다음 의존성을 추가:

```groovy
runtimeOnly 'ch.qos.logback:logback-classic:1.4.14'
```

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

### 옵션: 테스트 결과 출력(build.gradle 추가)
```groovy
test {
    useJUnitPlatform()

    testLogging {
        events "passed", "skipped", "failed"
        showStandardStreams = true
    }
}
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
