# 🔭 QuerySentinel
JPA 쿼리 실행 검증 및 성능 테스트를 위한 어노테이션

**QuerySentinel**은 `Spring Boot` + `JPA` 테스트 코드에서 실행되는 SQL 쿼리 수, 실행 시간, DB 접근 여부 등을 어노테이션 기반으로 검증하는 테스트 전용 라이브러리입니다.
외부 APM이나 JDBC 프록시 없이, 순수 Java 코드로 구현되었습니다. 핵심 JDBC 구성 요소(`PreparedStatement`, `Connection`, `DataSource`)를 직접 감싸 낮은 수준에서 쿼리를 추적합니다.

> ✅ 쿼리 성능 회귀를 테스트 단계에서 감지 <br>
> ✅ `@ExpectQuery`, `@ExpectNoDb`, `@ExpectTime`, `@ExpectNoTx` 같은 직관적인 어노테이션으로 구현 <br>
> ✅ `N+1 문제`, `불필요한 DB 호출`, `느린 쿼리`를 테스트 중 탐지 <br>
> ✅ `PreparedStatement`, `Connection` 및 `DataSource`를 직접 래핑 <br>

---

## 1️⃣ 기능 소개

| 어노테이션 | 설명 |
|-----------|------|
| `@EnableQuerySentinel` | 쿼리 감시 기능을 자동 설정 |
| `@ExpectQuery(select=1, insert=1)` | SELECT/INSERT/UPDATE/DELETE 쿼리 수 검증 |
| `@ExpectTime(300)` | 테스트 실행 시간 제한 (ms) |
| `@ExpectNoDb` | 테스트 중 DB 접근이 없어야 통과 |
| `@ExpectNoTx` | 테스트 중 트랜잭션이 활성화되어 있으면 실패 (strict = true일 경우, 읽기 전용도 실패) |

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

#### B. 직접 JAR 파일 사용
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

#### 코드 예시

```java
@SpringBootTest
@EnableQuerySentinel
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @ExpectQuery(select = 1, insert = 1)    // SELECT가 실제로는 2회 발생하므로 실패 의도
    @ExpectNoDb                             // DB접근하므로 실패 의도
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

#### 출력 예시

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

## 3️⃣ 권장 사용 환경
* Java 17+
* Spring Boot 3.2+
* Hibernate 6.3+
* JUnit 5.9+

> 이 라이브러리는 Spring Boot + JPA 환경을 전제로 아래 의존성이 함께 있어야 정상적으로 동작합니다
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.querysentinel:querysentinel:1.0.0'
}
```

---

<details>
<summary>확장</summary>
SpringBoot JPA 쿼리 수 검증  <br>
Hibernate 쿼리 검증  <br>
JUnit SQL 성능 테스트  <br>
SpringBoot N+1 문제 <br>
JPA 테스트 쿼리 로깅  <br>
JUnit SQL 실행 시간 측정  <br>
서비스가 DB 대신 캐시를 사용하는지 테스트  <br>
커스텀 DataSource 기반 JDBC 추적  <br>
JPA 테스트용 JDBC 프록시 대안  <br>
</details>
