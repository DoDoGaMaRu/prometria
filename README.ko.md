# prometria

Spring Data 스타일의 Prometheus 쿼리 라이브러리.

`@PromQL` 어노테이션으로 쿼리를 선언하는 인터페이스를 정의하기만 하면, 쿼리 실행 (HTTP)과 응답 매핑 (record)을 라이브러리가 대신 해준다. 쿼리 호출 코드, HTTP 클라이언트 보일러플레이트,
DTO 매핑이 필요 없다.

```java

@PrometheusRepository
public interface CpuRepository {

    @PromQL("up{job=\"${job}\"}")
    InstantQueryResult up(@Param("job") String job, InstantQueryCondition condition);
}
```

## 특징

- **구현 코드 0** — 인터페이스만 정의하면 Spring 빈이 자동으로 만들어진다 (JDK 동적 프록시)
- **기동 시 검증** — 어노테이션 누락, 리턴 타입 불일치 같은 실수는 Spring 기동 실패로 즉시 드러난다. 모든 위반을 한 번에 나열
- **21종 운영** — PromQL 템플릿 쿼리 3종 (instant/range/format) + 비-PromQL 운영 18종 (series, targets, rules, status 등)
- **타입 안정** — 모든 응답이 record 로 매핑된다
- **Spring 없는 코어** — `prometria-core` 는 JDK `HttpClient` + Jackson 만 사용

## 요구 사항

- Java 21+
- Spring Boot 3.2+ (`prometria-spring-boot-starter` 사용 시)

## 설치

현재 **Maven Local** 로만 공급된다. 소스에서 한 번 빌드한 뒤, 소비자 프로젝트에 아래를 추가한다.

```bash
./gradlew publishToMavenLocal
```

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation "io.github.dodogamaru:prometria-spring-boot-starter:0.0.1-SNAPSHOT"
}
```

```xml

<dependency>
    <groupId>io.github.dodogamaru</groupId>
    <artifactId>prometria-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## 퀵스타트

**1. Prometheus 주소를 설정한다.**

```yaml
prometria:
  prometheus:
    # 라이브러리는 이 주소에 /query, /query_range, /series ... 경로를 붙여 호출한다
    base-url: http://localhost:9090/api/v1
```

**2. 리포지토리 인터페이스를 정의한다.**

```java

@PrometheusRepository
public interface CpuRepository {

    // instant query (GET /query)
    @PromQL("up{job=\"${job}\"}")
    InstantQueryResult up(@Param("job") String job, InstantQueryCondition condition);

    // range query (GET /query_range)
    @PromQL("sum by (mode) (rate(node_cpu_seconds_total{job=\"${job}\"}[5m]))")
    RangeQueryResult cpuUsageByMode(@Param("job") String job, RangeQueryCondition condition);
}
```

인터페이스는 클래스패스 스캔되어 **빈 이름 = 인터페이스 이름** 으로 Spring 빈이 된다.

**3. 주입해서 호출한다.**

```java

@Service
public class CpuService {

    private final CpuRepository cpuRepository;

    public CpuService(CpuRepository cpuRepository) {
        this.cpuRepository = cpuRepository;
    }

    public double upValueOf(String job) {
        InstantQueryResult result = cpuRepository.up(job, InstantQueryCondition.builder().build());
        return result.data().result().getFirst().value().value();
    }
}
```

## 리포지토리 (PromQL 쿼리)

리포지토리는 **PromQL 템플릿을 가진 쿼리 전용**이다.
"자주 쓰는 고정 쿼리를 코드베이스에 두고 재활용한다"가 리포지토리의 존재 이유다. PromQL 이 없는 운영 (series, labels 등) 은 아래 내장 API 를 사용한다.

### 메서드 규칙

프록시는 빈 생성 시점 (앱 기동 중)에 인터페이스의 **모든** 메서드를 검증하고, 위반이 있으면 `PrometheusRepositoryException` 으로 기동을 실패시킨다. 첫 쿼리가 아니라 기동 단계에서,
한 번에 모든 위반을 보여준다.

| # | 규칙                                                                        |
|---|-----------------------------------------------------------------------------|
| 1 | 모든 메서드에 `@PromQL` 필수, 템플릿 **비어 있으면 안 됨**                  |
| 2 | condition 파라미터는 **정확히 하나**                                        |
| 3 | 리턴 타입이 condition 의 결과 타입과 정확히 일치 (아래 "지원 운영" 표 참고) |
| 4 | 템플릿의 모든 `${key}` 가 메서드 파라미터로 해결되어야 함                   |

타입 불일치 에러는 **이유와 수정 방법**을 함께 알려준다.

```
Invalid Prometheus repository com.example.CpuRepository:
  - cpuUsageByMode: return type RangeQueryResult does not match the result type
    InstantQueryResult of the handler for InstantQueryCondition; each condition type is
    bound to exactly one result type, so declare InstantQueryResult or use
    RangeQueryCondition instead, whose handler returns RangeQueryResult.
```

### 템플릿 치환

- `@PromQL` 템플릿의 `${key}` 는 메서드 인자로 치환된다. 키는 `@Param("key")` 의 값이며, `@Param` 이 없으면 **컴파일 시 파라미터명**이 된다.
- 값은 문자열로 변환되어 들어간다. 같은 키에 두 파라미터를 바인딩하면 에러다.

## 내장 API (비-PromQL 운영)

PromQL 이 필요한 운영은 3종뿐이고, 쿼리 파라미터만으로 동작하는 18종은
`io.github.dodogamaru.prometria.api` 패키지의 API 빈 4개로 바로 사용한다. condition 객체 없이 파라미터를 그대로 받으며, 메서드명은 엔드포인트명과 같다. 선택 파라미터는
생략된 오버로드를 쓰면 된다.

| 빈               | 메서드                                                                                                |
|------------------|-------------------------------------------------------------------------------------------------------|
| `DiscoveryApi`   | `series` · `labels` · `labelValues` · `metadata`                                                      |
| `TargetsApi`     | `targets` · `relabelSteps`                                                                            |
| `RulesAlertsApi` | `rules` · `alerts` · `alertmanagers` · `scrapePools`                                                  |
| `StatusApi`      | `config` · `flags` · `runtimeInfo` · `buildInfo` · `tsdb` · `tsdbBlocks` · `selfMetrics` · `features` |

```java

@Service
public class OpsService {

    private final DiscoveryApi discoveryApi;
    private final TargetsApi targetsApi;
    private final StatusApi statusApi;

    public OpsService(DiscoveryApi discoveryApi, TargetsApi targetsApi, StatusApi statusApi) {
        this.discoveryApi = discoveryApi;
        this.targetsApi = targetsApi;
        this.statusApi = statusApi;
    }

    public List<String> jobNames() {
        return discoveryApi.labelValues("job").data();
    }

    public List<Map<String, String>> upSeries() {
        return discoveryApi.series(List.of("up{job=\"node-exporter\"}")).data();
    }

    public List<String> activeInstances() {
        return targetsApi.targets("active").data().activeTargets().stream()
                .map(target -> target.labels().get("instance"))
                .toList();
    }

    public String version() {
        return statusApi.buildInfo().data().version();
    }
}
```

모든 빈은 `@ConditionalOnMissingBean` 이므로 직접 등록하면 사용자 정의가 우선한다.

## 조건과 시간 값

모든 condition 은 Lombok 빌더로 만드는 불변 객체다.

```java
import io.github.dodogamaru.prometria.model.time.TimeUnit;
import io.github.dodogamaru.prometria.model.time.TimeValue;

// Instant — time 생략 시 현재 시각
InstantQueryCondition instant = InstantQueryCondition.builder()
        .time(LocalDateTime.now().minusMinutes(5))
        .timeout(TimeValue.of(30, TimeUnit.SECONDS))
        .build();

        // Range — start/end/step 필수
        RangeQueryCondition range = RangeQueryCondition.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now())
                .step(TimeValue.of(15, TimeUnit.SECONDS))
                .build();

        // Format — 필드 없음
        FormatQueryCondition format = new FormatQueryCondition();
```

- `LocalDateTime` 은 epoch 초로 변환되며, 기준은 **시스템 기본 타임존**이다.
- `TimeValue` 는 Prometheus duration 문자열을 만든다:
  `TimeValue.of(30, TimeUnit.SECONDS)` → `30s`. `TimeUnit` 은 `s`/`m`/`h` 만 지원한다.
- instant/range condition 은 `timeout`, `dedup`, `partialResponse`, `limit`,
  `lookbackDelta`, `stats` 도 추가로 달 수 있다.

## 결과 읽기

모든 결과는 `(String status, data)` 형태의 record 다.
`data` 의 구조는 운영마다 다르며 record accessor 로 직접 접근한다.

```java
// ── Instant query (resultType = "vector") ───────────────────────────
InstantQueryResult result = cpuRepository.up("application", instant);
result.

status();                    // "success"
result.

data().

resultType();         // "vector"

for(
InstantQuerySeries s :result.

data().

result()){
Map<String, String> labels = s.metric();      // {"job":"application","instance":"host-1",...}
PrometheusSample sample = s.value();
double ts = sample.timestamp();               // epoch 초
double v = sample.value();
}

// ── Range query (resultType = "matrix") ─────────────────────────────
RangeQueryResult rangeResult = cpuRepository.cpuUsageByMode("node-exporter", range);
for(
RangeQuerySeries s :rangeResult.

data().

result()){
List<PrometheusSample> samples = s.values();  // 평가 구간의 샘플 목록
}

// ── 내장 API ────────────────────────────────────────────────────────
List<String> labelNames = discoveryApi.labels().data();
List<String> jobs = discoveryApi.labelValues("job").data();
List<Map<String, String>> series = discoveryApi.series(List.of("up")).data();
```

`PrometheusSample` 은 `record(double timestamp, double value)` 이다.

## 쿼리 사용 예시

프로덕션 메트릭 ETL 잡을 바탕으로 한 실제 예시: (method, uri, status) 레이블 조합별로 하루 치 HTTP 요청 카운트를 수집한다.

**1. 리포지토리** — instant 쿼리로 존재하는 레이블 조합을 열거하고, range 쿼리로 조합별 시계열을 조회한다.

```java
@PrometheusRepository
public interface HttpRequestsCountRepository {

    // instant query — 카운터의 레이블 조합 열거
    @PromQL("""
        http_server_requests_seconds_count{
          job="${job}",
          uri!~".*(prometheus|metrics).*"
        }
        """)
    InstantQueryResult countLabels(String job, InstantQueryCondition condition);

    // range query — 조합별 2m 윈도우 증가분
    @PromQL("""
        increase(
          http_server_requests_seconds_count{
            error="${error}",
            exception="${exception}",
            instance="${instance}",
            job="${job}",
            method="${method}",
            outcome="${outcome}",
            status="${status}",
            uri="${uri}",
            uri!~".*(prometheus|metrics).*"
          }[2m]
        )
        """)
    RangeQueryResult count(
            String job,
            String error,
            String exception,
            String instance,
            String method,
            String outcome,
            String status,
            String uri,
            RangeQueryCondition condition);
}
```

**2. 호출** — 먼저 instant 쿼리로 열거한 뒤, 조합마다 range 쿼리로 시계열을 조회한다.

```java
@Service
public class HttpRequestsCountJob {

    private final HttpRequestsCountRepository repository;

    public HttpRequestsCountJob(HttpRequestsCountRepository repository) {
        this.repository = repository;
    }

    public void collect(LocalDate targetDate, String job) {

        // 1) instant query — 대상일 24시 시점에 평가
        InstantQueryCondition instant = InstantQueryCondition.builder()
                .time(targetDate.plusDays(1).atStartOfDay())
                .build();
        InstantQueryResult labels = repository.countLabels(job, instant);

        // 2) range condition — 대상일, 2분 스텝
        RangeQueryCondition range = RangeQueryCondition.builder()
                .start(targetDate.atStartOfDay())
                .end(targetDate.plusDays(1).atStartOfDay())
                .step(TimeValue.of(2, TimeUnit.MINUTES))
                .build();

        // 3) 레이블 조합마다 시계열 1개씩 조회
        for (InstantQuerySeries series : labels.data().result()) {
            Map<String, String> m = series.metric();
            RangeQueryResult result = repository.count(
                    job,
                    m.get("error"),
                    m.get("exception"),
                    m.get("instance"),
                    m.get("method"),
                    m.get("outcome"),
                    m.get("status"),
                    m.get("uri"),
                    range);

            for (RangeQuerySeries s : result.data().result()) {
                List<PrometheusSample> samples = s.values();
                // 하루 치 샘플 누적 ...
            }
        }
    }
}
```

## 예외 처리

| 예외                            | 발생 시점         | 의미                                                  |
|---------------------------------|-------------------|-------------------------------------------------------|
| `PrometheusRepositoryException` | 빈 생성 (기동) 시 | 리포지토리 인터페이스 설정 오류 (모든 위반 항목 포함) |
| `PrometheusQueryException`      | 메서드 호출 시    | HTTP 4xx/5xx, I/O 에러, 응답이 JSON 이 아님           |

둘 다 `PrometriaException`(`RuntimeException`) 을 상속하므로 `throws` 선언이 필요 없다.

```java
try{
        return cpuRepository.up(job, condition);
}catch(
PrometheusQueryException e){
        // "Prometheus query failed. status=400, body={\"status\":\"error\",...}"
        log.

error("query failed",e);
}
```

주의: Prometheus 는 HTTP 200 OK 와 함께 `status: "error"` 로 답할 수도 있다. 비즈니스 코드에서 `result.status()` 를 확인하는 것을 권장한다.

## 지원 운영

### 템플릿 쿼리 (리포지토리 경유, 3종)

각 condition 은 리턴 타입과 1:1 로 고정된다. 핸들러는 `io.github.dodogamaru.prometria.query.<operation>` 패키지에 condition + handler +
result 가 한 패키지로 묶여 있다.

| 운영          | condition               | 리턴 타입            | 엔드포인트          | 참고                                    |
|---------------|-------------------------|----------------------|---------------------|-----------------------------------------|
| Instant query | `InstantQueryCondition` | `InstantQueryResult` | `GET /query`        | `time` 선택                             |
| Range query   | `RangeQueryCondition`   | `RangeQueryResult`   | `GET /query_range`  | `start`/`end`/`step` **필수**           |
| Format query  | `FormatQueryCondition`  | `FormatQueryResult`  | `GET /format_query` | 포맷할 식어는 메서드의 `@PromQL` 템플릿 |

### 비-PromQL 운영 (내장 API 경유, 18종)

"내장 API" 절의 빈 4개가 담당한다 (리포지토리 불필요, condition 없음).

| 빈               | 엔드포인트                                                                                                                                                                            |
|------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DiscoveryApi`   | `/series` · `/labels` · `/label/{name}/values` · `/metadata`                                                                                                                          |
| `TargetsApi`     | `/targets` · `/targets/relabel_steps`                                                                                                                                                 |
| `RulesAlertsApi` | `/rules` · `/alerts` · `/alertmanagers` · `/scrape_pools`                                                                                                                             |
| `StatusApi`      | `/status/config` · `/status/flags` · `/status/runtimeinfo` · `/status/buildinfo` · `/status/tsdb` · `/status/tsdb/blocks` (3.6+) · `/status/self_metrics` (3.6+) · `/features` (3.8+) |

- `match[]` 파라미터는 PromQL 인스턴스 셀렉터 목록으로 대상 시리즈를 좁힌다.
- 결과 record 는 `io.github.dodogamaru.prometria.model.{discovery,targets,rules,status}` 패키지에 있다.
- **미구현** (후보): `parse_query`, `query_exemplars`, `targets/metadata`, admin/write API.

## 확장: 새 운영 추가

라이브러리가 아직 다루지 않는 API (예: `/parse_query`) 가 필요하면 condition · result · handler 3가지를 만들고 핸들러를 빈으로 등록한다. 등록된 핸들러는 **모든**
`@PrometheusRepository` 에서 자동으로 사용 가능하다.

```java
// 1) condition — Condition 구현 (Lombok @Builder 권장)
public class ParseQueryCondition implements Condition {
}

// 2) result record
public record ParseQueryResult(String status, String data) {
}

// 3) handler — 공통 파라미터/HTTP 플로우는 AbstractQueryHandler 가 처리
public class ParseQueryHandler extends AbstractQueryHandler<ParseQueryCondition, ParseQueryResult> {

    public ParseQueryHandler(PrometheusHttpClient client) {
        super(client);
    }

    @Override
    public Class<ParseQueryCondition> conditionType() {
        return ParseQueryCondition.class;
    }

    @Override
    public Class<ParseQueryResult> resultType() {
        return ParseQueryResult.class;
    }

    @Override
    protected String getPath(ParseQueryCondition c) {
        return "/parse_query";
    }

    @Override
    protected QueryParams queryParams(ParseQueryCondition c) {
        return QueryParams.create();   // 파라미터 없는 운영
    }
}

// 4) 빈 등록
@Bean
ParseQueryHandler parseQueryHandler(PrometheusHttpClient client) {
    return new ParseQueryHandler(client);
}
```

이후 리포지토리에 메서드 하나만 추가하면 된다 — 프록시가 condition 타입을 보고 새 핸들러로 라우팅한다.

```java

@PromQL("avg without (mode) (node_load1)")
ParseQueryResult parse(ParseQueryCondition condition);
```

## 한계 / 주의

| 항목          | 내용                                                                                                                                                |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `@Param`      | 생략 시 **컴파일 시 파라미터명**이 치환 키가 된다. 소비자 앱이 `-parameters` 없이 컴파일하면 `arg0` 이 되어 치환이 실패한다. **`@Param` 명시 권장** |
| 타임존        | `LocalDateTime` → epoch 변환이 **시스템 기본 타임존** 기준이다. 서버 TZ 에 따라 보내지는 값이 달라진다                                              |
| 빈 이름       | 스캔된 리포지토리의 빈 이름은 인터페이스 **SimpleName** 이다. 서로 다른 패키지에 같은 이름의 리포지토리가 두 개면 충돌                              |
| 스캔 범위     | `@SpringBootApplication` 클래스 패키지를 기준으로 한다. 여러 컨텍스트가 공존하는 테스트 환경에서는 첫 번째를 사용                                   |
| range 쿼리    | `start`/`end`/`step` 필수                                                                                                                           |
| metadata 필터 | 3.x 는 `metric` (첫 값), 2.x 는 `metric[]` 을 읽는다. 라이브러리는 둘 다 전송해 두 버전에서 모두 필터가 동작한다                                    |
| 읽기 타임아웃 | 없음. 연결 타임아웃 10s 만 적용된다                                                                                                                 |
| 배포          | Maven Local 전용 (로컬). 외부 공급은 미정                                                                                                           |

## 개발

```bash
./gradlew build              # 컴파일 + 단위 테스트 + 커버리지 하한 검증
./gradlew integrationTest    # 실제 Prometheus 서버 대상 통합 테스트
./gradlew jacocoMergedReport # 단위 + 통합 병합 커버리지
./gradlew publishToMavenLocal
```

- `build` 는 외부 의존 없이 단위 테스트만 실행한다.
- `integrationTest` 는 `http://localhost:11999/api/v1` 의 실제 Prometheus 에 쿼리를 보낸다 (서버 필요).
- JaCoCo 하한: `prometria-core` instruction 50%, `prometria-spring-boot-starter` 90% (각 모듈 `jacocoMinimumCoverage` 로 조정).

### 패키지 구조

`prometria-core` 는 레이어 우선 패키지로 구성된다. 아래 레이어는 위 레이어를 참조할 수 없으며, 이 방향은 ArchUnit 테스트 (`PackageDependencyTest`) 로 고정된다.

```
io.github.dodogamaru.prometria
├── exception  — PrometriaException, PrometheusQueryException, PrometheusRepositoryException
├── model      — PrometheusSample, result record 들 (discovery/targets/rules/status), time
├── client     — PrometheusHttpClient (공용 transport), QueryParams
├── api        — DiscoveryApi, TargetsApi, RulesAlertsApi, StatusApi
├── query      — Condition, QueryHandler, AbstractQueryHandler
│   └── instant / range / format — condition + handler + result
└── repository — @PrometheusRepository, @PromQL, @Param, 프록시/팩토리

prometria-spring-boot-starter
└── io.github.dodogamaru.prometria.spring — 자동설정, 스캐너, 팩토리 빈
```

### 동작 원리

1. `prometria.prometheus.base-url` 이 설정되어 있으면
   `PrometriaAutoConfiguration` 이 `PrometheusHttpClient` (JDK `HttpClient`, 연결 타임아웃 10s), 템플릿 핸들러 3종, API 빈 4종,
   `PrometheusRepositoryScanner`
   를 등록한다. 미설정이면 어떤 빈도 등록되지 않는다.
2. 스캐너 (`BeanDefinitionRegistryPostProcessor`) 는 `@SpringBootApplication`
   패키지 아래에서 `@PrometheusRepository` 인터페이스를 클래스패스 스캔해
   `PrometheusRepositoryFactoryBean` 을 등록한다.
3. FactoryBean 은 컨텍스트의 모든 `QueryHandler` 빈을 condition 타입 기준으로 모아 `PrometheusRepositoryProxy` (JDK `InvocationHandler`) 를
   만든다.
4. 빈 생성 시 프록시가 인터페이스 전체를 검증하고, 위반이 있으면 기동을 실패시킨다.
5. 메서드 호출 시 `@PromQL` 템플릿에서 `${...}` 치환 → 생성 시 미리 계산해 둔 계획 (plan) 을 따라 핸들러 실행 (매 호출마다 리플렉션 없음) → Jackson 으로 결과 record
   매핑.
6. HTTP 4xx/5xx 또는 비JSON 응답은 `PrometheusQueryException`
   (status + 응답 본문 스니펫) 로 실패한다.
