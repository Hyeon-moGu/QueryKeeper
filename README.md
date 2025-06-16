# 🌱 QueryKeeper
**JPA 쿼리 실행 검증 및 성능 테스트를 위한 어노테이션 라이브러리**

**QueryKeeper**는 `Spring Boot` + `JPA` 테스트 코드에서 실행되는 SQL 쿼리 수, 실행 시간, DB 접근 여부 등을 어노테이션 기반으로 검증하는 테스트 전용 라이브러리입니다.
외부 APM이나 JDBC 프록시 없이, 순수 Java 코드로 구현되었습니다. 핵심 JDBC 구성 요소(`PreparedStatement`, `Connection`, `DataSource`)를 직접 감싸 낮은 수준에서 쿼리를 추적합니다.

✔️ Java 8 ~ 17+, Spring Boot 2.7 ~ 3.2+, Hibernate 5.6 ~ 6.3+, JUnit 5.8+ 환경을 지원합니다.

> ✅ 별도 설정 없이 바로 사용 가능. 테스트 클래스에 `@EnableQueryKeeper` 만 추가 <br>
> ✅ 쿼리 성능 회귀를 테스트 단계에서 감지 <br>
> ✅ `@ExpectQuery`, `@ExpectDetachedAccess`, `@ExpectTime`, `@ExpectDuplicateQuery` 같은 직관적인 어노테이션으로 구현 <br>
> ✅ `N+1 문제`, `불필요한 DB 호출`, `느린 쿼리`를 테스트 중 탐지 <br>
> ✅ `PreparedStatement`, `Connection` 및 `DataSource`를 직접 래핑 <br>

---

## 1️⃣ 기능 소개

| 어노테이션                   | 설명                                                                          |
|---------------------------|------------------------------------------------------------------------------|
| `@EnableQueryKeeper`      | 모든 QueryKeeper 기능을 활성화                                                   |
| `@ExpectQuery`            | 테스트 중 실시 수행된 추적의 개수를 로깅 및 검사                                        |
| `@ExpectDuplicateQuery`   | 동일한 SQL 쿼리(파라미터 포함)가 반복 실행될 경우 테스트를 실패 처리                        |
| `@ExpectDetachedAccess`   | 트랜잭션이 종료된 후 LAZY 필드에 접근하여 발생하는 `LazyInitializationException`를 감지   |
| `@ExpectTime`             | 테스트 실행 시간 제한 (ms)                                                        |
| `@ExpectNoDb`             | 테스트 중 DB 접근이 없어야 통과                                                     |
| `@ExpectNoTx`             | 테스트 중 트랜잭션이 활성화되어 있으면 실패 (strict = true일 경우, 읽기 전용도 실패)          |

<details> <summary><strong>📘 어노테이션별 상세 설명 (클릭하여 펼치기)</strong></summary>

### `@ExpectQuery`

테스트 중 실행된 SQL 쿼리 수를 기록하고 검증합니다.

* **파라미터:**

  * `select` *(기본값: -1)* — 예상 SELECT 쿼리 수
  * `insert` *(기본값: -1)* — 예상 INSERT 쿼리 수
  * `update` *(기본값: -1)* — 예상 UPDATE 쿼리 수
  * `delete` *(기본값: -1)* — 예상 DELETE 쿼리 수

* **동작 방식:**
기본적으로 이 어노테이션은 실행된 모든 SQL 쿼리를 파라미터를 포함한 완전한 형태로 출력하며, 실행 시간과 호출 위치도 함께 로깅합니다.
만약 `select`, `insert` 등의 기대 횟수(0 이상)가 지정된 경우, 실제 실행된 쿼리 수와 일치하지 않으면 테스트는 실패 처리됩니다.
기대값이 설정되지 않더라도 모든 테스트에서 쿼리 목록은 항상 동일한 형식으로 출력됩니다.
출력 내용은 쿼리 유형, 실행 시간(ms), 파라미터가 포함된 실제 SQL문, 호출 위치(클래스명#메서드:라인 번호) 등을 포함하며 디버깅이나 성능 분석에 매우 유용합니다.

### `@ExpectDuplicateQuery`

동일한 SQL 쿼리(파라미터 포함)가 여러 번 실행될 경우 테스트를 실패 처리합니다.

- **파라미터:**
  - `max` *(선택, 기본값: 0)* — 허용되는 중복 쿼리의 최대 개수

- **작동 방식:**  
  테스트 실행 중 발생한 모든 SQL 쿼리와 그 파라미터를 추적하여,  
  동일한 쿼리(문자열 및 파라미터 조합)가 반복 실행될 경우 중복으로 판단합니다.  
  이 중복 쿼리 수가 `max` 값을 초과하면 테스트는 실패하게 됩니다.

> 루프 내 동일 SELECT 반복, 실수로 발생한 N+1 문제 등을 조기에 감지하는 데 유용합니다.

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
  </details>

#### 코드 예시

> 아래 예시는 일부 테스트가 실패하도록 설계되어 있습니다.

```java
@Test
@ExpectQuery(select = 1, insert = 1) // ❌ 실패
@ExpectTime(500)                     // ✅ 성공
@ExpectNoTx(strict = false)          // ✅ 성공
@ExpectNoDb                          // ❌ 실패
@ExpectDuplicateQuery                // ❌ 실패
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
    2025-06-16 19:39:14.450  INFO 2484 --- [    Test worker] c.q.junit.QueryKeeperExtension           : 
    [QueryKeeper] ▶ ExpectNoTx ✓ PASSED - No transaction in testCombinedAssertions()
    [QueryKeeper] ▶ ExpectTime ✓ PASSED - testCombinedAssertions took 11ms (expected <= 500ms)
    [QueryKeeper] ▶ ExpectQuery X FAILED
    --------------------------------------------------------
    Expected - (SELECT: 1, INSERT: 1), Actual - (SELECT: 2, INSERT: 3)
    --------------------------------------------------------
    Total Queries: 8
    --------------------------------------------------------
    1. [OTHER] (0 ms)
    SQL     : call next value for hibernate_sequence
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:48
    --------------------------------------------------------
    2. [OTHER] (0 ms)
    SQL     : call next value for hibernate_sequence
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:48
    --------------------------------------------------------
    3. [OTHER] (0 ms)
    SQL     : call next value for hibernate_sequence
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:48
    --------------------------------------------------------
    4. [INSERT] (0 ms)
    SQL     : insert into users (email, name, id) values ('alice@example.com', 'Alice', 3)
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:48
    --------------------------------------------------------
    5. [INSERT] (0 ms)
    SQL     : insert into roles (name, user_id, id) values ('ADMIN', 3, 4)
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:48
    --------------------------------------------------------
    6. [INSERT] (0 ms)
    SQL     : insert into roles (name, user_id, id) values ('USER', 3, 5)
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:48
    --------------------------------------------------------
    7. [SELECT] (0 ms)
    SQL     : select user0_.id as id1_1_, user0_.email as email2_1_, user0_.name as name3_1_ from users user0_
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:49
    --------------------------------------------------------
    8. [SELECT] (0 ms)
    SQL     : select user0_.id as id1_1_, user0_.email as email2_1_, user0_.name as name3_1_ from users user0_
    Caller  : com.example.demo.UserRepositoryTest#testCombinedAssertions:52
    --------------------------------------------------------
    [QueryKeeper] ▶ ExpectNoDb X FAILED - 8 DB queries were executed in testCombinedAssertions()
    [QueryKeeper] ▶ ExpectDuplicateQuery X FAILED - Found 1 duplicate queries (allowed: 0)
      • Duplicate [2x] → select user0_.id as id1_1_, user0_.email as email2_1_, user0_.name as name3_1_ from users user0_
```

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

---

## 3️⃣ 권장 사용 환경

* Java 8 ~ Java 17+
* Spring Boot 2.7.x ~ 3.2+
* Hibernate 5.6.x ~ 6.3+
* JUnit Jupiter 5.8+

> 이 라이브러리는 Spring Boot + JPA 환경을 전제로 아래 의존성이 함께 있어야 정상적으로 동작합니다
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.querykeeper:querykeeper:1.1.0'
}
```

