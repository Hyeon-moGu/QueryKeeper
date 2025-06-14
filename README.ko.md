# 🌱 QueryKeeper
**JPA 쿼리 실행 검증 및 성능 테스트를 위한 어노테이션 라이브러리**

**QueryKeeper**는 `Spring Boot` + `JPA` 테스트 코드에서 실행되는 SQL 쿼리 수, 실행 시간, DB 접근 여부 등을 어노테이션 기반으로 검증하는 테스트 전용 라이브러리입니다.
외부 APM이나 JDBC 프록시 없이, 순수 Java 코드로 구현되었습니다. 핵심 JDBC 구성 요소(`PreparedStatement`, `Connection`, `DataSource`)를 직접 감싸 낮은 수준에서 쿼리를 추적합니다.

> ✅ 쿼리 성능 회귀를 테스트 단계에서 감지 <br>
> ✅ `@ExpectQuery`, `@ExpectDetachedAccess`, `@ExpectTime`, `@ExpectNoTx` 같은 직관적인 어노테이션으로 구현 <br>
> ✅ `N+1 문제`, `불필요한 DB 호출`, `느린 쿼리`를 테스트 중 탐지 <br>
> ✅ `PreparedStatement`, `Connection` 및 `DataSource`를 직접 래핑 <br>

---

## 1️⃣ 기능 소개

| 어노테이션 | 설명 |
|-----------------------|------------------------------------------------------------------------------|
| `@EnableQueryKeeper`      | 모든 QueryKeeper 기능을 활성화                                                |
| `@ExpectQuery`            | 테스트 중 실시 수행된 추적의 개수를 로깅 및 검사                                     |
| `@ExpectDetachedAccess`   | 트랜잭션이 종료된 후 LAZY 필드에 접근하여 발생하는 `LazyInitializationException`를 감지|
| `@ExpectTime`             | 테스트 실행 시간 제한 (ms)                                                      |
| `@ExpectNoDb`             | 테스트 중 DB 접근이 없어야 통과                                                   |
| `@ExpectNoTx`             | 테스트 중 트랜잭션이 활성화되어 있으면 실패 (strict = true일 경우, 읽기 전용도 실패)        |

## 어노테이션별 상세 설명

### `@ExpectQuery`

테스트 중 실행된 SQL 쿼리 수를 기록하고 검증합니다.

* **파라미터:**

  * `select` *(기본값: -1)* — 예상 SELECT 쿼리 수
  * `insert` *(기본값: -1)* — 예상 INSERT 쿼리 수
  * `update` *(기본값: -1)* — 예상 UPDATE 쿼리 수
  * `delete` *(기본값: -1)* — 예상 DELETE 쿼리 수

* **동작 방식:**
  별도의 값을 지정하지 않으면 쿼리 로그만 출력됩니다. 하나 이상의 값이 0 이상으로 지정된 경우, 실제 쿼리 수가 기대치와 다르면 테스트가 실패합니다.
  `SELECT NEXT VALUE FOR`와 같은 시퀀스 관련 쿼리도 SELECT로 계산됩니다.

### `@ExpectDetachedAccess`

트랜잭션이 종료된 상태에서 지연 로딩 필드에 잘못 접근할 경우 발생하는 LazyInitializationException 을 감지합니다. 
즉, JPA 엔티티가 detached 상태일 때 발생하는 잘못된 Lazy 필드 접근을 테스트 중 조기에 확인할 수 있습니다.

* **파라미터:** 없음

* **동작 방식:**
  테스트 실행 중 발생한 `LazyInitializationException`을 AOP로 가로채어,
  어떤 엔티티의 어떤 필드가 잘못 접근되었는지 기록합니다.
  이를 통해 테스트에서 예상치 못한 Lazy 접근을 빠르게 감지할 수 있습니다.

> ⚠️ 트랜잭션 외부에서의 비정상적인 Lazy 접근(LazyInitializationException) 만 탐지합니다.

### `@ExpectTime`

테스트가 지정된 시간 내에 완료되어야 합니다.

* **파라미터:**

  * `value` *(필수)* — 허용되는 최대 테스트 실행 시간 (ms 단위)

* **동작 방식:**
  테스트 실행 전체 시간(설정, DB 쿼리 등 포함)을 측정하며, 설정한 시간 이상 소요되면 실패합니다.

### `@ExpectNoDb`

테스트 중 어떤 형태의 데이터베이스 접근도 없어야 합니다.

* **파라미터:** 없음

* **동작 방식:**
  SELECT, INSERT, UPDATE, DELETE 등 모든 쿼리 실행을 감지하며, 단 하나라도 발생하면 실패합니다.
  순수 로직 또는 캐시 단위 테스트에 유용합니다.

### `@ExpectNoTx`

테스트가 트랜잭션 외부에서 실행되어야 함을 검증합니다.

* **파라미터:**

  * `strict` *(기본값: true)* — `readOnly` 트랜잭션까지 금지할지 여부

* **동작 방식:**
  테스트 실행 중 활성 트랜잭션이 존재하는지 확인합니다. `strict=true`인 경우, `@Transactional(readOnly = true)`도 실패 처리됩니다.
---

## 2️⃣ 설치방법

### 로깅 주의사항

Querykeeper은 로그 출력을 위해 SLF4J를 사용합니다.  
Spring Boot를 사용하는 경우 별도 설정이 필요하지 않습니다 (`spring-boot-starter-logging`에 포함)<br>
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
    testImplementation 'com.querykeeper:querykeeper:1.1.0'
}
```

#### B. 직접 JAR 파일 사용
```groovy
testImplementation files('libs/querykeeper-1.1.0.jar')
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

> 아래 예시는 일부 애너테이션이 실패하도록 설계되어 있습니다.

```java
@Test
@ExpectQuery(select = 1, insert = 1) // ❌ 실패
@ExpectTime(500)                     // ✅ 성공
@ExpectNoTx(strict = false)          // ✅ 성공
@ExpectNoDb                          // ❌ 실패
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
@ExpectDetachedAccess      // ❌ 실패
void testDetachedAccess() {
    userService.triggerDetachedAccess();
}
```

#### 출력 예시

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
```

---

## 3️⃣ 권장 사용 환경

* Java 17+
* Spring Boot 2.7 ~ 3.2+
* Hibernate 5.6 ~ 6.3+
* JUnit 5.9+

> 이 라이브러리는 Spring Boot + JPA 환경을 전제로 아래 의존성이 함께 있어야 정상적으로 동작합니다
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
SpringBoot JPA 쿼리 수 검증  <br>
Hibernate 쿼리 검증  <br>
JUnit SQL 성능 테스트  <br>
SpringBoot N+1 문제 탐지  <br>
JPA 테스트 쿼리 로깅 방법  <br>
JUnit으로 SQL 실행 시간 측정  <br>
서비스가 DB 대신 캐시를 사용하는지 검증  <br>
커스텀 DataSource 기반 쿼리 추적  <br>
JPA 테스트용 JDBC 프록시 대체 도구  <br>
JPA LazyInitializationException 테스트  <br>
트랜잭션 없이 테스트 실행 검증  <br>
JPA Fetch Join 테스트 방법  <br>
쿼리 없는 테스트 검증 방법  <br>
</details>
