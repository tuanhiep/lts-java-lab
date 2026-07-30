# Verification matrix

The matrix records what each executable experiment establishes and, equally
importantly, what it does not establish. A passing test is evidence for the
narrow claim in its row—not a universal correctness, performance or production
readiness statement.

## Standalone JDK labs

| Experiment | Asserted contract | Observation or boundary |
|---|---|---|
| `HashMapContracts` | equal-key lookup, null policy, `containsKey` semantics | capacity, hash spreading and treeification remain runtime details |
| `ConcurrentMapAtomicity` | one successful `computeIfAbsent` mapping for one absent key under contention | bin locks, CAS paths and counter cells are not asserted |
| `ReferenceReachability` | a strongly reachable referent remains retrievable | weak-reference clearing and enqueue timing are observed |
| `StringInterning` | literal identity, `new String` identity and `intern()` contract | compact-string encoding is a JDK implementation detail |
| `LambdaCaptureSemantics` | captured values and functional behavior | lambda allocation and instance reuse are observed |
| `PatternMatching` | record patterns, guards, flow scoping and sealed exhaustiveness | requires Java 21+ language semantics |
| `EqualityHashingOrdering` | equality/hash contracts and the `BigDecimal` ordering exception | lookup after mutating a hash key is not asserted |
| `RecordSerialization` | record field mutation is rejected and deserialization invokes the canonical constructor | Java native serialization is not recommended for untrusted data |
| `EnumSemantics` | constant behavior, reflective construction rejection and `EnumSet` membership | `EnumSet` storage representation is not asserted |
| `DeserializationFilters` | a stream-scoped allow-list accepts and rejects as configured | filtering reduces risk; it does not make native serialization intrinsically safe |
| `VirtualThreadAdmissionControl` | virtual-thread tasks complete while a semaphore caps downstream concurrency | no throughput or latency conclusion is drawn |

## Spring and service-engineering tests

| Test | Executable claim | Boundary |
|---|---|---|
| `BeanLifecycleLabTest` | lifecycle callback order, prototype creation and destruction ownership | isolated application context |
| `TransactionBoundaryLabTest` | proxy self-invocation behavior and checked-exception rollback rules | local Spring transactions with H2 |
| `JpaPersistenceLabTest` | dirty checking, N+1 observation and fetch-plan repair | statement counts are scenario-specific |
| `OptimisticLockingPostgresIT` | two PostgreSQL writers loaded at one version cannot both commit | PostgreSQL 16 and one aggregate shape |
| `OAuth2ResourceServerLabTest` | unauthenticated, authenticated-but-forbidden and authorized requests remain distinct | JWT cryptography and network key discovery are outside MockMvc |
| `MetricsInstrumentationLabTest` | bounded tags, timer recording and application ownership of gauge state | no backend aggregation or production retention test |
| `AutoConfigurationLabTest` | property-independent default creation and user-bean backoff | focused context, not a full starter publication test |
| `WebContractLabTest` | body/method validation returns RFC 9457-compatible problem responses | representative endpoints, not a complete API surface |
| `SchemaAndPoolingPostgresIT` | additive migration, JDBC batching and explicit HikariCP limit | no load-based pool-sizing claim |
| `IdempotencyOutboxLabTest` | deduplication, local effect and outbox record share one transaction | broker publication and remote side effects remain separate |
| `RuntimeBaselineLabTest` | Spring Boot, JUnit and Java bytecode baselines are explicit | version presence, not framework-wide compatibility |
| `OperationsConfigurationLabTest` | graceful-shutdown, probe and Actuator exposure configuration | no orchestrator or load-balancer handshake |

## Packaging checks

| Artifact | Check | Boundary |
|---|---|---|
| Executable JAR | Spring Boot layers contain `dependencies`, `spring-boot-loader`, `snapshot-dependencies` and `application` | does not assess image vulnerability or runtime policy |
| Docker image | multi-stage extraction succeeds; final image uses Java 25 JRE and a non-root user | build success is not a deployment acceptance test |

## Reproduce

```bash
./verify.sh "$(/usr/libexec/java_home -v 25)/bin/java" --docker
./verify.sh "$(/usr/libexec/java_home -v 21)/bin/java" --no-containers
```

The authoritative evidence is the command exit code and generated Maven test
reports under `target/surefire-reports` and `target/failsafe-reports`.
