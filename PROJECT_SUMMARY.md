# Service Incident & Status Management Platform — 项目总结

> 本文档基于对代码库的实际阅读整理，所有技术栈、行数、测试数量均来自代码统计，未做美化。仓库地址：`github.com/yran304/service-incident-platform`。

## 1. 一句话说明

一个面向 SaaS 团队内部的**服务事故（Incident）管理与状态发布平台**：管理员在内部系统里创建组织、登记被监控的服务、记录并推进事故生命周期（调查中→已定位→观察中→已解决），并可发布面向客户的事故更新——类似 Atlassian Statuspage / Cachet 的简化版。当前代码只实现了后端 REST API + 一个最小的组织管理前端页面，尚未做到"给谁用"的完整闭环（见第 8 节）。

## 2. 技术栈（均为代码中实际使用的版本）

**后端**（[backend/build.gradle.kts](backend/build.gradle.kts)）
- Java 21（Gradle toolchain）
- Spring Boot 4.1.0（`spring-boot-starter-webmvc`、`spring-boot-starter-data-jpa`、`spring-boot-starter-validation`、`spring-boot-starter-actuator`、`spring-boot-starter-flyway`）
- Flyway + `flyway-database-postgresql`（数据库迁移）
- PostgreSQL（`postgres:17-alpine`，JDBC 驱动 `org.postgresql:postgresql`）
- JUnit 5（`junit-platform-launcher`）+ Spring 各 starter 对应的 `-test` 依赖（MockMvc 等）
- Gradle（Kotlin DSL），Gradle Wrapper 管理版本

**前端**（[frontend/package.json](frontend/package.json)）
- React 19.2.7 + ReactDOM 19.2.7
- TypeScript ~6.0.2
- Vite 8.1.1（构建/开发服务器）
- ESLint 10 + typescript-eslint 8（无测试框架，`package.json` 中没有 Vitest/Jest）

**基础设施**
- Docker Compose（[compose.yaml](compose.yaml)）—— 编排 **PostgreSQL + 后端**两个服务。后端用[多阶段 Dockerfile](backend/Dockerfile)构建（`gradle:9.5.1-jdk21` 构建阶段 + `eclipse-temurin:21-jre-alpine` 运行阶段），`docker compose up` 一键启动，backend 通过 `depends_on.condition: service_healthy` 等待数据库健康后再启动。前端仍未容器化。

## 3. 架构与目录结构

### 3.1 整体架构

```
React (Vite dev server, :5173) → Spring Boot REST API (:8080) → PostgreSQL (:5432, Flyway 管理 schema)
```

这是一个**模块化单体（modular monolith）**，按业务域（而非技术分层）组织后端代码，详见 [docs/architecture.md](docs/architecture.md)。

### 3.2 后端目录结构

```
backend/src/main/java/com/yran304/incidentplatform/
├── IncidentPlatformApplication.java      # Spring Boot 入口
├── common/
│   └── ApiExceptionHandler.java          # 全局异常 → HTTP 状态码 + ProblemDetail 映射
├── organizations/                        # Organization 域：Entity/Repository/Service/Controller/DTO/异常
├── services/                             # TrackedService 域（"服务" = 被监控的对象，如 API、网站）
├── incidents/                            # Incident 域：事故本体 + 状态机
└── incidentupdates/                      # IncidentUpdate 域：事故的时间线更新，可发布/未发布
```

每个业务域内部统一遵循同一套六件套模式（以 `organizations` 为例）：
- `Organization.java` —— JPA `@Entity`，贫血模型，构造函数 + getter，无 setter（不可变风格）
- `OrganizationRepository.java` —— `JpaRepository` 接口，用 Spring Data 方法名派生查询（如 `existsBySlug`）
- `OrganizationService.java` —— 业务规则（唯一性校验、状态机、UUID/时间戳生成）
- `OrganizationController.java` —— `@RestController`，只做参数绑定和状态码，不含业务逻辑
- `Create*Request.java` / `*Response.java` —— 输入输出 DTO（Java `record`），用 Bean Validation 注解做校验
- `*NotFoundException.java` / `*AlreadyExistsException.java` —— 领域异常，交给 `ApiExceptionHandler` 统一转换成 `ProblemDetail`（RFC 7807）

### 3.3 前端目录结构

```
frontend/src/
├── main.tsx           # React 挂载入口
├── App.tsx            # 唯一页面组件：加载组织列表 + 创建组织表单（148 行含 api 层，无路由、无状态管理库）
├── api/organizations.ts  # 用 fetch 封装 GET/POST /api/organizations
└── App.css / index.css
```

前端目前**只覆盖 Organization 这一个域**：加载组织列表、创建组织表单。Services / Incidents / IncidentUpdates 只有后端 API，前端完全没有对应界面。

### 3.4 一个典型请求的完整链路

以"运营人员登记一次新事故"为例：

1. 前端调用（当前实际未实现该页面，仅描述后端链路）`POST /api/services/{serviceId}/incidents`，body 为 `{title, impact}`。
2. [IncidentController.createIncident](backend/src/main/java/com/yran304/incidentplatform/incidents/IncidentController.java:951) 接收请求，`@Valid` 触发 Bean Validation（`title` 非空≤200字符，`impact` 必须是 `MINOR|MAJOR|CRITICAL` 正则）。
3. 校验失败 → Spring 默认抛 `MethodArgumentNotValidException` → 400（未被 `ApiExceptionHandler` 自定义拦截，走 Spring 默认的 400 响应）。
4. 校验通过 → 调用 [IncidentService.createIncident](backend/src/main/java/com/yran304/incidentplatform/incidents/IncidentService.java:1127)：
   - 用 `TrackedServiceRepository.existsById` 校验 `serviceId` 是否存在，不存在抛 `TrackedServiceNotFoundException`。
   - 应用层生成 `UUID.randomUUID()` 作为主键、`Instant.now()` 作为时间戳，初始状态硬编码为 `"INVESTIGATING"`。
   - 调用 `IncidentRepository.save(...)`，Hibernate 生成 `INSERT`。
5. 若上一步抛出 `TrackedServiceNotFoundException` → [ApiExceptionHandler.handleTrackedServiceNotFound](backend/src/main/java/com/yran304/incidentplatform/common/ApiExceptionHandler.java:533) 捕获，转换成 `404` + `ProblemDetail{title: "Service not found"}`。
6. 成功则 `IncidentResponse.from(entity)` 把 Entity 转成 DTO 返回，Controller 上的 `@ResponseStatus(HttpStatus.CREATED)` 使响应码为 201。

事故状态推进走 `PATCH /api/incidents/{incidentId}/status`，同样经过状态机校验（见 4.2 节）。

## 4. 关键设计决策及原因

### 4.1 为什么按业务域（vertical slice）而不是按技术层（controller/service/repository 各一个大包）分层

[docs/architecture.md](docs/architecture.md) 明确写了"模块化单体"的意图：每个域（`organizations`/`services`/`incidents`/`incidentupdates`）内部自包含 Entity/Repository/Service/Controller/DTO/异常，域之间只通过 Repository/Service 接口互相依赖（例如 `IncidentService` 依赖 `TrackedServiceRepository` 来校验外键存在性）。好处是未来若要拆分微服务，边界已经天然存在；缺点是当前项目规模小，切分收益尚未体现，反而每个域都要重复写六个文件的样板代码。

### 4.2 状态一致性：应用层状态机 + 数据库 CHECK 约束的双重校验

事故状态流转规则 `INVESTIGATING → IDENTIFIED → MONITORING → RESOLVED`（线性，不可逆、不可跳过）在两处强制：
- 应用层：[IncidentService.isValidStatusTransition](backend/src/main/java/com/yran304/incidentplatform/incidents/IncidentService.java:1192) 用 `switch` 表达式显式列出合法转移，非法转移抛 `InvalidIncidentStatusTransitionException` → 409。
- 数据库层：[V3__create_incidents.sql](backend/src/main/resources/db/migration/V3__create_incidents.sql) 用 `CHECK (status IN (...))` 兜底非法值（防止绕过应用层直接写库）。

`resolvedAt` 字段只在状态变为 `RESOLVED` 时被设置（[IncidentService.java:1180](backend/src/main/java/com/yran304/incidentplatform/incidents/IncidentService.java:1180)），这是一个业务不变量，但**没有数据库层 CHECK 约束**保证"resolvedAt 非空 ⟺ status=RESOLVED"（对比 `incident_updates` 表对 `is_published`/`published_at` 的一致性是有 CHECK 约束的，见 [V4 迁移](backend/src/main/resources/db/migration/V4__create_incident_updates.sql)）——这是不一致的地方，是可以在面试中被追问、也值得改进的点。

### 4.3 唯一性约束：应用层"先查后插"+ 数据库唯一索引兜底

`slug` 唯一性（组织级全局唯一、服务在组织内唯一）都是先在 Service 层调用 `existsBySlug`/`existsByOrganizationIdAndSlug` 查询，再插入。这是一个**竞态条件（TOCTOU）**：两个并发请求可能同时查询到"不存在"，都执行插入，最终违反数据库唯一约束（`slug UNIQUE` / `uq_services_organization_slug`）抛出 `DataIntegrityViolationException`，而这个异常**没有被 `ApiExceptionHandler` 捕获**，会导致 500 而不是预期的 409。这是当前代码在并发场景下的真实缺口，不是"已处理并容错"，面试中如果被问"如何保证唯一性/如何处理并发冲突"应如实说明现状和改进方案（比如捕获 `DataIntegrityViolationException` 转 409，或用数据库唯一约束的错误作为唯一判定来源）。

### 4.4 主键生成策略：应用层生成 UUID，而非数据库自增或 `@GeneratedValue`

所有实体的 `id` 都是 Service 层 `UUID.randomUUID()` 显式赋值（例如 [OrganizationService.java:170](backend/src/main/java/com/yran304/incidentplatform/organizations/OrganizationService.java:170)），而不是让 JPA/数据库生成。好处是插入前就能拿到 ID（便于返回、便于测试断言），代价是失去数据库自增序列的省心和更好的索引局部性（UUID 是可能导致 B-tree 页分裂的经典问题），当前规模下无影响。

### 4.5 DTO 与 Entity 分离

每个域都有独立的 `*Response` record 而不是直接序列化 Entity（如 [OrganizationResponse](backend/src/main/java/com/yran304/incidentplatform/organizations/OrganizationResponse.java)），代码注释里写明原因是"控制 API 暴露的字段，未来可能和 Entity 字段产生分歧"。当前两者字段完全一致，是为将来（例如加鉴权后 Entity 上会有更多内部字段）预留的隔离层。

### 4.6 事务边界

集成测试类标注了 `@Transactional`（如 [IncidentControllerIntegrationTests.java:30](backend/src/test/java/com/yran304/incidentplatform/incidents/IncidentControllerIntegrationTests.java:30)），依赖 Spring Test 的"测试结束自动回滚"来隔离测试数据，而不是每个测试后手动清库。但**生产代码里 Service 层方法没有显式 `@Transactional` 注解**——目前每个 Service 方法只有一次 `save`/`findAll` 调用，尚未出现需要跨多次写操作保证原子性的场景，所以现状（依赖 Spring Data 仓库方法的隐式事务）能工作，但一旦出现"先扣减库存再插入订单"这类多步操作，需要显式加 `@Transactional`。

### 4.7 容错与并发处理的诚实现状

目前**没有**：重试机制、幂等键、乐观锁（`@Version`）、限流、熔断、消息队列缓冲写入峰值。所有写操作都是同步阻塞的单次数据库事务。这符合 [docs/architecture.md](docs/architecture.md) 里"Deliberate MVP exclusions"的声明（明确排除了 Kafka、Redis 等），是有意为之的范围控制，而不是遗漏，但面试中如果被问"生产环境怎么办"，要诚实说明这是 MVP 阶段的取舍。

## 5. 测试

### 5.1 测试类型与数量

全部是 **Spring Boot 集成测试**（`@SpringBootTest` + `@AutoConfigureMockMvc` + MockMvc 模拟 HTTP 请求），**没有纯单元测试**（没有对 Service 类用 Mockito mock Repository 的测试），也没有 Repository 层的 `@DataJpaTest`。

| 测试类 | 行数 | 覆盖场景数 |
| --- | --- | --- |
| [OrganizationControllerIntegrationTests](backend/src/test/java/com/yran304/incidentplatform/organizations/OrganizationControllerIntegrationTests.java) | 114 | 4（创建成功、校验失败、slug 重复冲突、列表查询）|
| [TrackedServiceControllerIntegrationTests](backend/src/test/java/com/yran304/incidentplatform/services/TrackedServiceControllerIntegrationTests.java) | 155 | 5（创建成功、校验失败、组织内 slug 重复、列表查询、组织不存在 404）|
| [IncidentControllerIntegrationTests](backend/src/test/java/com/yran304/incidentplatform/incidents/IncidentControllerIntegrationTests.java) | 204 | 7（创建成功、校验失败、列表查询、服务不存在 404、完整生命周期流转+resolvedAt断言、事故不存在 404、非法状态跳转 409）|
| [IncidentUpdateControllerIntegrationTests](backend/src/test/java/com/yran304/incidentplatform/incidentupdates/IncidentUpdateControllerIntegrationTests.java) | 169 | 4（创建已发布更新、校验失败、按创建时间排序列表、事故不存在 404）|
| [IncidentPlatformApplicationTests](backend/src/test/java/com/yran304/incidentplatform/IncidentPlatformApplicationTests.java) | 13 | 1（Spring 上下文能否成功加载）|

共 **5 个测试类、21 个 `@Test` 方法、655 行测试代码**（`find backend/src/test -name "*.java" | xargs wc -l` 统计）。

### 5.2 测试基础设施的真实情况（重要，容易被追问）

测试**没有使用 Testcontainers 或内嵌 H2**，而是直接连接 [application.properties](backend/src/main/resources/application.properties) 里配置的**真实本地 PostgreSQL**（`jdbc:postgresql://localhost:5432/incident_platform`）。这意味着：
- 运行测试前必须先 `docker compose up -d postgres`（README 也是这样写的）。
- 测试隔离完全依赖 `@Transactional` 自动回滚，测试之间不共享脏数据，但**测试环境和真实环境是同一个数据库连接配置**，没有独立的测试 profile（没有 `application-test.properties`）。
- 这不是 CI 友好的方案：如果要接入 GitHub Actions（README 里写的"计划中"），需要在 CI 里先起一个 Postgres service 容器，或者迁移到 Testcontainers。

### 5.3 覆盖的场景类型
- 正常路径（happy path）的创建/查询
- Bean Validation 失败 → 400
- 业务唯一性冲突 → 409
- 外键/关联资源不存在 → 404
- 状态机非法转移 → 409
- 状态机合法转移全链路 + 派生字段（`resolvedAt`）正确性

### 5.4 如何运行

```bash
docker compose up -d postgres
cd backend
./gradlew test
```

前端**没有任何测试**（`package.json` 里只有 `lint`/`build`/`dev`/`preview`，无 `test` 脚本，无 Vitest/Jest 依赖）。

## 6. 部署和运行方式

- **本地开发（容器化）**：`docker compose up --build` 从仓库根目录一键启动 PostgreSQL + 后端（8080），无需手动装 Gradle/JDK 或跑 `./gradlew bootRun`。前端仍需单独 `npm run dev`（5173）。
- **本地开发（非容器，调试用）**：`docker compose up -d postgres` 起数据库 → `./gradlew bootRun` 起后端 → `npm run dev` 起前端。二者可自由切换，取决于是否需要频繁重启调试后端。
- **数据库迁移**：Flyway 在应用启动时自动执行 `backend/src/main/resources/db/migration/V1__*.sql` ~ `V4__*.sql`，`spring.jpa.hibernate.ddl-auto=validate`（Hibernate 只校验 schema 与实体是否匹配，不会自动建表/改表，schema 变更完全由 Flyway 脚本驱动）。已验证 Flyway 在容器环境下同样能正确执行迁移、连接 `postgres` service（容器间用 service 名而非 `localhost` 通信）。
- **可观测性**：只开放了 `management.endpoints.web.exposure.include=health,info` 两个 Actuator 端点，没有 metrics/prometheus 端点暴露。`backend` 容器的 healthcheck 复用 `/actuator/health`（`wget --spider`），并用 `depends_on.condition: service_healthy` 确保先等 postgres 健康再启动，避免启动时连接失败。
- **Docker Compose 现状**：[compose.yaml](compose.yaml) 编排 `postgres` + `backend` 两个 service，backend 用[多阶段 Dockerfile](backend/Dockerfile)构建。**前端尚未容器化**，"完整 `docker compose up` 一键起全部三个服务"仍做不到。
- **配置传递方式**：backend 容器通过 `environment` 覆盖 `application.properties` 里的数据源配置（`SPRING_DATASOURCE_URL/USERNAME/PASSWORD`，Spring Boot relaxed binding 规则：property key 转大写、`.`→`_`），值目前仍是硬编码在 `compose.yaml` 里的本地开发默认值，不是通过 secret 管理注入的。
- **CI/CD**：仓库里没有 `.github/workflows/`，README 中"GitHub Actions backend checks"是[implementation-plan.md](docs/implementation-plan.md)里 8 月 10–14 日阶段的计划项，**尚未实现**。
- **配置管理**：数据库账号密码（`incident_app` / `incident_app_password`）目前是硬编码在 [application.properties](backend/src/main/resources/application.properties) 和 [compose.yaml](compose.yaml) 里的本地开发默认值，没有用环境变量或 secret 管理（生产环境不可直接照搬）。

## 7. 简历中可以准确写出的事实（附证据）

以下每一条都可以在代码里核实，建议按需挑选、不要照抄整段：

- 用 **Java 21 + Spring Boot 4.1.0** 实现了 4 个业务域（组织/服务/事故/事故更新）的 REST API，共 **33 个 Java 源文件、约 1,159 行生产代码**（`backend/src/main`）。
- 设计并用 **Flyway** 编写了 4 个版本化数据库迁移脚本（[V1](backend/src/main/resources/db/migration/V1__create_organizations.sql)–[V4](backend/src/main/resources/db/migration/V4__create_incident_updates.sql)），包含外键约束、复合唯一约束（组织内服务 slug 唯一）、CHECK 约束（状态枚举、发布状态一致性）。
- 实现了一个**事故生命周期状态机**（`INVESTIGATING→IDENTIFIED→MONITORING→RESOLVED`），应用层校验 + 数据库 CHECK 双重约束（[IncidentService.java:1192](backend/src/main/java/com/yran304/incidentplatform/incidents/IncidentService.java:1192)）。
- 用 **RFC 7807 ProblemDetail** 统一了全局异常处理（[ApiExceptionHandler.java](backend/src/main/java/com/yran304/incidentplatform/common/ApiExceptionHandler.java)），覆盖 6 类领域异常，映射到 404/409 等语义化状态码。
- 编写了 **21 个 Spring Boot 集成测试**（MockMvc + 真实 PostgreSQL），覆盖正常路径、校验失败、唯一性冲突、404、状态机非法转移等场景，共 655 行测试代码。
- 用 **Docker Compose** 编排 PostgreSQL 17 + Spring Boot 后端两个服务，后端用**多阶段 Dockerfile**（Gradle 构建阶段 + Alpine JRE 运行阶段）构建，两个 service 均配置健康检查（`pg_isready` / actuator `/health`），并用 `depends_on.condition: service_healthy` 保证依赖就绪顺序，`docker compose up --build` 一键启动、验证过完整的创建/查询 API 请求链路。
- 用 **React 19 + TypeScript + Vite 8** 实现了组织管理页面（列表 + 创建表单），封装类型安全的 fetch API 客户端。

## 8. 未实现 / 只做了一部分的功能（如实列出，不美化）

| 功能 | 状态 |
| --- | --- |
| **认证与鉴权**（Spring Security、JWT、ADMIN/EDITOR/VIEWER 角色） | **完全未实现**。所有 API 都是匿名可访问的。[docs/architecture.md](docs/architecture.md) 里设计了角色权限表，但代码里没有 `auth` 包，`build.gradle.kts` 里也没有 `spring-boot-starter-security` 依赖。 |
| **内部管理 Dashboard**（前端管理服务/事故/发布更新） | **只完成了组织管理这一个域的前端页面**。服务、事故、事故更新只有后端 API，没有任何前端界面。 |
| **公开状态页**（Public Status Page） | **完全未实现**，没有对应的 Controller、路由或前端页面。 |
| **用户/成员管理**（Organization 与 User 的关系） | 架构文档设计了 `User` 实体和 Organization-User 关系，代码里**没有 User 实体、没有对应的表迁移**。 |
| **CI（GitHub Actions）** | **未实现**，仓库无 `.github/workflows/`。 |
| **Docker 化后端/前端** | **后端已完成**（多阶段 Dockerfile + compose service，健康检查、依赖顺序、环境变量注入均已验证）。**前端仍未容器化**，仍需 `npm run dev` 本地跑。 |
| **API 文档**（OpenAPI/Swagger） | **未实现**，无 springdoc 依赖，无 Swagger UI。 |
| **分页**（Organization/Service/Incident 列表接口） | 未实现，`getOrganizations()`/`getServicesByOrganization()`/`getIncidentsByService()` 都是 `findAll()` 全量返回，没有 `Pageable`。 |
| **并发/唯一性冲突的优雅处理** | 见 4.3 节，存在竞态条件导致 500 而非预期 409 的风险，未做修复。 |
| **前端测试** | 完全没有测试文件和测试框架依赖。 |
| **Incident 的 `resolvedAt` 一致性约束** | 只在应用层保证，数据库层缺少对应 CHECK 约束（对比 IncidentUpdate 的 `is_published`/`published_at` 是有约束的）。 |

## 9. 面试中可能被追问的技术点

1. **"为什么用 UUID 做主键而不是自增 ID？"** —— 见 4.4 节，能说出取舍（提前拿到 ID / 分布式友好 vs 索引局部性变差），不要只说"更安全"。
2. **"两个并发请求同时创建同名组织会怎样？"** —— 如实说明当前是"先查后插"存在 TOCTOU 竞态，`DataIntegrityViolationException` 未被捕获会变成 500，说明你知道正确做法是依赖数据库唯一约束的异常作为唯一判定来源。
3. **"为什么没用 Spring Security？"** —— 如实说明这是 MVP 范围控制的产物（[implementation-plan.md](docs/implementation-plan.md) 里排在第三个里程碑，尚未开始），并能说出如果要加，鉴权应该加在哪一层（Controller 前的 Filter/拦截器，而不是散落在 Service 里做 if 判断）。
4. **"状态机为什么不用枚举类型（enum）而用 String？"** —— 数据库和 Java 两边都用字符串常量（`"INVESTIGATING"` 等），没有用 Java `enum` + JPA `@Enumerated`，是可以指出的改进点：用枚举能获得编译期检查，避免拼写错误导致的静默 bug。
5. **"为什么测试要连真实 Postgres 而不是 Testcontainers/H2？"** —— 如实说明现状（见 5.2 节），并能说出用 Testcontainers 的好处（测试环境隔离、可在 CI 中无需预装数据库运行、避免"本地能跑 CI 跑不了"的问题）。
6. **"DTO 和 Entity 现在字段完全一样，这层隔离是不是过度设计？"** —— 能说出当前确实是 1:1 映射，但一旦加鉴权/加内部字段（如软删除标记、审计字段）就会出现分歧，是防御性设计而非当前必需，属于合理的前瞻，但也可以承认"现在看有点浪费代码"。
7. **"`ddl-auto=validate` 和 Flyway 配合的原理？"** —— 能解释 Flyway 负责实际建表/改表，Hibernate 只在启动时校验 `@Entity` 映射与数据库表结构是否一致，不一致直接启动失败，这样能防止"代码里改了字段但忘记写迁移脚本"的问题。
8. **"如果要支持公开状态页，现有设计要改哪里？"** —— 能说出需要新增一个不需要鉴权的只读接口/域，只暴露 `IncidentUpdate.isPublished=true` 的数据，并且需要考虑该接口的缓存策略（当前完全没有缓存层）。
