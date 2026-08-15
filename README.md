# prometria

A Spring Data–style query library for Prometheus.

[English](README.md) · [한국어](README.ko.md)

Just declare an interface with `@PromQL`-annotated queries, and the library handles query execution (HTTP) and response mapping (records) for you. No query-invocation code, HTTP client boilerplate, or DTO mapping required.

```java

@PrometheusRepository
public interface CpuRepository {

    @PromQL("up{job=\"${job}\"}")
    InstantQueryResult up(@Param("job") String job, InstantQueryCondition condition);
}
```

## Features

- **Zero implementation code** — just define the interface and Spring beans are created automatically (JDK dynamic proxy)
- **Startup-time validation** — mistakes such as a missing annotation or a return-type mismatch surface immediately as a Spring startup failure. All violations are listed at once
- **21 operations** — 3 PromQL template query types (instant/range/format) + 18 non-PromQL operations (series, targets, rules, status, etc.)
- **Type safe** — every response is mapped to a record
- **Spring-free core** — `prometria-core` uses only the JDK `HttpClient` + Jackson

## Requirements

- Java 21+
- Spring Boot 3.2+ (when using `prometria-spring-boot-starter`)

## Installation

Currently available via **Maven Local** only. Build once from source, then add the following to your consuming project.

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

## Quick start

**1. Set the Prometheus URL.**

```yaml
prometria:
  prometheus:
    # The library appends /query, /query_range, /series ... paths to this URL
    base-url: http://localhost:9090/api/v1
```

**2. Define a repository interface.**

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

The interface is discovered by classpath scanning and becomes a Spring bean with the **bean name = interface name**.

**3. Inject and call it.**

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

## Repositories (PromQL queries)

A repository is **dedicated to queries that carry a PromQL template**.
The point of a repository is to keep frequently used fixed queries in the codebase for reuse. Non-PromQL operations (series, labels, etc.) use the built-in APIs below.

### Method rules

The proxy validates **every** method on the interface when the bean is created (during app startup), and fails startup with a `PrometheusRepositoryException` if any violation is found. This happens at startup, not on the first query, and shows all violations at once.

| # | Rule                                                                        |
|---|-----------------------------------------------------------------------------|
| 1 | Every method requires `@PromQL`, and the template **must not be empty**     |
| 2 | **Exactly one** condition parameter                                         |
| 3 | Return type must exactly match the result type of the condition (see the "Supported operations" table below) |
| 4 | Every `${key}` in the template must resolve to a method parameter           |

Type-mismatch errors report both the **reason and how to fix it**.

```
Invalid Prometheus repository com.example.CpuRepository:
  - cpuUsageByMode: return type RangeQueryResult does not match the result type
    InstantQueryResult of the handler for InstantQueryCondition; each condition type is
    bound to exactly one result type, so declare InstantQueryResult or use
    RangeQueryCondition instead, whose handler returns RangeQueryResult.
```

### Template substitution

- `${key}` in a `@PromQL` template is substituted from the method arguments. The key is the value of `@Param("key")`, or the **compile-time parameter name** if `@Param` is absent.
- Values are converted to strings on insertion. Binding two parameters to the same key is an error.

## Built-in API (non-PromQL operations)

Only 3 operations require PromQL; the 18 that run on query parameters alone are exposed directly through 4 API beans in the `io.github.dodogamaru.prometria.api` package. They take plain parameters with no condition object, and method names match the endpoint names. For optional parameters, just use the overloads that omit them.

| Bean               | Methods                                                                                       |
|--------------------|-----------------------------------------------------------------------------------------------|
| `DiscoveryApi`     | `series` · `labels` · `labelValues` · `metadata`                                              |
| `TargetsApi`       | `targets` · `relabelSteps`                                                                    |
| `RulesAlertsApi`   | `rules` · `alerts` · `alertmanagers` · `scrapePools`                                          |
| `StatusApi`        | `config` · `flags` · `runtimeInfo` · `buildInfo` · `tsdb` · `tsdbBlocks` · `selfMetrics` · `features` |

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

All beans are `@ConditionalOnMissingBean`, so user-defined registrations take precedence.

## Conditions and time values

All conditions are immutable objects built with Lombok builders.

```java
import io.github.dodogamaru.prometria.model.time.TimeUnit;
import io.github.dodogamaru.prometria.model.time.TimeValue;

// Instant — time defaults to now when omitted
InstantQueryCondition instant = InstantQueryCondition.builder()
        .time(LocalDateTime.now().minusMinutes(5))
        .timeout(TimeValue.of(30, TimeUnit.SECONDS))
        .build();

        // Range — start/end/step are required
        RangeQueryCondition range = RangeQueryCondition.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now())
                .step(TimeValue.of(15, TimeUnit.SECONDS))
                .build();

        // Format — no fields
        FormatQueryCondition format = new FormatQueryCondition();
```

- `LocalDateTime` is converted to epoch seconds using the **system default time zone**.
- `TimeValue` builds Prometheus duration strings:
  `TimeValue.of(30, TimeUnit.SECONDS)` → `30s`. `TimeUnit` supports only `s`/`m`/`h`.
- instant/range conditions can additionally carry `timeout`, `dedup`, `partialResponse`,
  `limit`, `lookbackDelta`, and `stats`.

## Reading results

Every result is a record of the form `(String status, data)`.
The shape of `data` differs per operation and is accessed directly via record accessors.

```java
// ── Instant query (resultType = "vector") ───────────────────────────
InstantQueryResult result = cpuRepository.up("application", instant);
result.status();                    // "success"
result.data().resultType();         // "vector"
for (InstantQuerySeries s : result.data().result()) {
    Map<String, String> labels = s.metric();      // {"job":"application","instance":"host-1",...}
    PrometheusSample sample = s.value();
    double ts = sample.timestamp();               // epoch seconds
    double v = sample.value();
}

// ── Range query (resultType = "matrix") ─────────────────────────────
RangeQueryResult rangeResult = cpuRepository.cpuUsageByMode("node-exporter", range);
for (RangeQuerySeries s : rangeResult.data().result()) {
    List<PrometheusSample> samples = s.values();  // samples over the evaluated range
}

// ── Built-in API ────────────────────────────────────────────────────
List<String> labelNames = discoveryApi.labels().data();
List<String> jobs = discoveryApi.labelValues("job").data();
List<Map<String, String>> series = discoveryApi.series(List.of("up")).data();
```

`PrometheusSample` is a `record(double timestamp, double value)`.

## Query usage example

A real-world example modeled on a production metrics ETL job: collect one day of HTTP request counts per (method, uri, status) label combination.

**1. Repository** — an instant query enumerates the label combinations that exist, a range query fetches each combination's series.

```java
@PrometheusRepository
public interface HttpRequestsCountRepository {

    // instant query — enumerate the label combinations of the counter
    @PromQL("""
        http_server_requests_seconds_count{
          job="${job}",
          uri!~".*(prometheus|metrics).*"
        }
        """)
    InstantQueryResult countLabels(String job, InstantQueryCondition condition);

    // range query — increase per combination over a 2m window
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

**2. Call** — enumerate with the instant query first, then fetch each series with the range query.

```java
@Service
public class HttpRequestsCountJob {

    private final HttpRequestsCountRepository repository;

    public HttpRequestsCountJob(HttpRequestsCountRepository repository) {
        this.repository = repository;
    }

    public void collect(LocalDate targetDate, String job) {

        // 1) instant query evaluated at the end of the target day
        InstantQueryCondition instant = InstantQueryCondition.builder()
                .time(targetDate.plusDays(1).atStartOfDay())
                .build();
        InstantQueryResult labels = repository.countLabels(job, instant);

        // 2) range condition — the target day in 2-minute steps
        RangeQueryCondition range = RangeQueryCondition.builder()
                .start(targetDate.atStartOfDay())
                .end(targetDate.plusDays(1).atStartOfDay())
                .step(TimeValue.of(2, TimeUnit.MINUTES))
                .build();

        // 3) fetch one series per label combination
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
                // accumulate the day's samples ...
            }
        }
    }
}
```

## Exception handling

| Exception                           | When                    | Meaning                                                    |
|-------------------------------------|-------------------------|------------------------------------------------------------|
| `PrometheusRepositoryException`     | bean creation (startup) | repository interface misconfiguration (includes all violations) |
| `PrometheusQueryException`          | method invocation       | HTTP 4xx/5xx, I/O error, response is not JSON              |

Both extend `PrometriaException` (`RuntimeException`), so no `throws` declaration is needed.

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

Note: Prometheus can answer with HTTP 200 OK while returning `status: "error"`. Checking `result.status()` in business code is recommended.

## Supported operations

### Template queries (via repository, 3 types)

Each condition is fixed 1:1 to its return type. A condition + handler + result live together in the `io.github.dodogamaru.prometria.query.<operation>` package.

| Operation     | Condition             | Return type          | Endpoint            | Notes                                       |
|---------------|-----------------------|----------------------|---------------------|---------------------------------------------|
| Instant query | `InstantQueryCondition` | `InstantQueryResult` | `GET /query`        | `time` is optional                          |
| Range query   | `RangeQueryCondition`   | `RangeQueryResult`   | `GET /query_range`  | `start`/`end`/`step` are **required**       |
| Format query  | `FormatQueryCondition`  | `FormatQueryResult`  | `GET /format_query` | the expression to format is the method's `@PromQL` template |

### Non-PromQL operations (via built-in API, 18 types)

Handled by the 4 beans in the "Built-in API" section (no repository needed, no condition).

| Bean               | Endpoints                                                                                                                                       |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `DiscoveryApi`     | `/series` · `/labels` · `/label/{name}/values` · `/metadata`                                                                                     |
| `TargetsApi`       | `/targets` · `/targets/relabel_steps`                                                                                                                                                     |
| `RulesAlertsApi`   | `/rules` · `/alerts` · `/alertmanagers` · `/scrape_pools`                                                                                                                                 |
| `StatusApi`        | `/status/config` · `/status/flags` · `/status/runtimeinfo` · `/status/buildinfo` · `/status/tsdb` · `/status/tsdb/blocks` (3.6+) · `/status/self_metrics` (3.6+) · `/features` (3.8+) |

- The `match[]` parameter takes a list of PromQL instant selectors to narrow down target series.
- Result records live in the `io.github.dodogamaru.prometria.model.{discovery,targets,rules,status}` packages.
- **Not implemented** (candidates): `parse_query`, `query_exemplars`, `targets/metadata`, admin/write API.

## Extending: adding a new operation

If you need an API the library does not handle yet (e.g. `/parse_query`), create the condition · result · handler trio and register the handler as a bean. A registered handler is then automatically available in **every** `@PrometheusRepository`.

```java
// 1) condition — implements Condition (Lombok @Builder recommended)
public class ParseQueryCondition implements Condition {
}

// 2) result record
public record ParseQueryResult(String status, String data) {
}

// 3) handler — common parameter/HTTP flow is handled by AbstractQueryHandler
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
        return QueryParams.create();   // parameterless operation
    }
}

// 4) bean registration
@Bean
ParseQueryHandler parseQueryHandler(PrometheusHttpClient client) {
    return new ParseQueryHandler(client);
}
```

Then just add a method to the repository — the proxy routes to the new handler based on the condition type.

```java

@PromQL("avg without (mode) (node_load1)")
ParseQueryResult parse(ParseQueryCondition condition);
```

## Limitations / caveats

| Item          | Details                                                                                                                                          |
|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| `@Param`      | When omitted, the **compile-time parameter name** becomes the substitution key. If the consuming app compiles without `-parameters`, it becomes `arg0` and substitution fails. **Explicitly specifying `@Param` is recommended** |
| Time zone     | `LocalDateTime` → epoch conversion is based on the **system default time zone**. The value sent depends on the server's TZ                      |
| Bean name     | A scanned repository's bean name is the interface **SimpleName**. Two repositories with the same name in different packages collide             |
| Scan scope    | Based on the `@SpringBootApplication` class package. In test environments where multiple contexts coexist, the first one is used                |
| range query   | `start`/`end`/`step` are required                                                                                                               |
| metadata filter | 3.x reads `metric` (first value), 2.x reads `metric[]`. The library sends both so filtering works on both versions                           |
| read timeout  | None. Only the 10s connect timeout applies                                                                                                       |
| distribution  | Maven Local only (local). External distribution is TBD                                                                                          |

## Development

```bash
./gradlew build              # compile + unit tests + coverage threshold check
./gradlew integrationTest    # integration tests against a real Prometheus server
./gradlew jacocoMergedReport # merged unit + integration coverage
./gradlew publishToMavenLocal
```

- `build` runs only unit tests, without external dependencies.
- `integrationTest` sends queries to a real Prometheus at `http://localhost:11999/api/v1` (a server is required).
- JaCoCo thresholds: `prometria-core` instruction 50%, `prometria-spring-boot-starter` 90% (adjustable per module via `jacocoMinimumCoverage`).

### Package structure

`prometria-core` is organized as layered packages. A lower layer must never reference a higher one, and this direction is pinned down by an ArchUnit test (`PackageDependencyTest`).

```
io.github.dodogamaru.prometria
├── exception  — PrometriaException, PrometheusQueryException, PrometheusRepositoryException
├── model      — PrometheusSample, result records (discovery/targets/rules/status), time
├── client     — PrometheusHttpClient (shared transport), QueryParams
├── api        — DiscoveryApi, TargetsApi, RulesAlertsApi, StatusApi
├── query      — Condition, QueryHandler, AbstractQueryHandler
│   └── instant / range / format — condition + handler + result
└── repository — @PrometheusRepository, @PromQL, @Param, proxy/factory

prometria-spring-boot-starter
└── io.github.dodogamaru.prometria.spring — auto-configuration, scanner, factory bean
```

### How it works

1. If `prometria.prometheus.base-url` is set,
   `PrometriaAutoConfiguration` registers `PrometheusHttpClient` (JDK `HttpClient`, 10s connect timeout), the 3 template handlers, the 4 API beans,
   and `PrometheusRepositoryScanner`. If it is not set, no bean is registered.
2. The scanner (a `BeanDefinitionRegistryPostProcessor`) classpath-scans for
   `@PrometheusRepository` interfaces under the `@SpringBootApplication`
   package and registers a `PrometheusRepositoryFactoryBean`.
3. The FactoryBean collects every `QueryHandler` bean in the context by condition type and builds a `PrometheusRepositoryProxy` (JDK `InvocationHandler`).
4. On bean creation, the proxy validates the entire interface and fails startup if there are violations.
5. On method invocation, `${...}` placeholders in the `@PromQL` template are substituted → the handler runs according to a plan precomputed at creation time (no per-invocation reflection) → the result record is mapped with Jackson.
6. HTTP 4xx/5xx or a non-JSON response fails with `PrometheusQueryException`
   (status + response body snippet).
