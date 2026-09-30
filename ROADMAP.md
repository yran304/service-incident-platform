# Service Incident & Status Management Platform — 阶段性补全计划

> 目的：把当前项目从「本地跑 Postgres + 手动 gradlew test」推进到「容器化 + CI + 可选部署」，
> 用于提升简历关键词覆盖率（Docker、CI/CD、AWS）以及补全面试故事的完整度。
> 原则：**每一步做完都要能诚实写进简历/PROJECT_SUMMARY，不做"跑一遍教程"式的表面工作。**
> 参考现状见仓库根目录 `PROJECT_SUMMARY.md` 第 6、8 节（未实现/局限部分）。

---

## 优先级说明

按性价比（收益 / 时间投入）从高到低排列，**按顺序做，不要跳步**：

| 阶段 | 内容 | 预估时间 | 状态 |
|---|---|---|---|
| 1 | 后端 Dockerize | 0.5 天 | ✅ 已完成 |
| 2 | GitHub Actions CI（跑测试） | 1-2 小时 | ⬜ 未开始 |
| 3（可选） | AWS 部署 | 1-2 天 | ⬜ 暂缓，视具体 JD 需求再启动 |

---

## 阶段 1：后端 Dockerize

**当前状态**：`compose.yaml` 只编排了 `postgres` 一个 service，没有 backend 的 Dockerfile。

**目标**：`docker compose up` 一键起 backend + postgres，不再需要手动 `./gradlew bootRun`。

### 任务清单

- [x] 在 `backend/` 下写 `Dockerfile`
  - 用多阶段构建（multi-stage build）：第一阶段用 `gradle:9.5.1-jdk21`（与 `gradle-wrapper.properties` 版本一致）跑 `./gradlew build -x test --no-daemon` 打包成 jar，第二阶段用精简的 `eclipse-temurin:21-jre-alpine` 只跑 jar，减小镜像体积。
  - 明确写清楚基础镜像版本，不要用 `latest`。
- [x] 更新 `compose.yaml`，新增 `backend` service：
  - `build: ./backend`
  - `depends_on: { postgres: { condition: service_healthy } }`（复用已有的 `pg_isready` 健康检查）
  - 数据库连接信息通过环境变量传入（`SPRING_DATASOURCE_URL/USERNAME/PASSWORD`），不要硬编码进 Dockerfile
  - 暴露 `8080` 端口，映射到宿主机 `8080:8080`
  - backend 自身也配置了 healthcheck（`wget --spider` 打 `/actuator/health`）
- [x] 验证：`docker compose up --build` 后，两个容器均显示 `healthy`，`curl localhost:8080/actuator/health` 及创建/查询 organization 的 API 均验证通过
  - 注意：这一步只验证运行时容器化，不代表测试环境也被容器化了——21 个集成测试仍然连宿主机本地 Postgres（见下方"诚实边界"），不要把"跑一遍 API"和"跑测试"混为一谈
- [x] 更新 `README.md` 的启动步骤，新增 `docker compose up --build` 一键启动方式，保留手动 `./gradlew bootRun` 作为调试备选
- [x] 更新 `PROJECT_SUMMARY.md` 第 2、6、7、8 节（去掉"未 Docker 化后端"，前端仍标注未容器化）

**诚实边界（面试可能被问到，提前想清楚怎么答）**：
- 测试仍然连真实本地 Postgres（第 5.2 节的问题依然存在），Dockerize 不解决这个，不要因为加了 Docker 就顺带在简历里暗示"测试环境隔离"。
- 前端是否一起 Dockerize，取决于你的时间——如果只做 backend，PROJECT_SUMMARY 里要如实写"前端未容器化"。

---

## 阶段 2：GitHub Actions CI

**当前状态**：仓库无 `.github/workflows/`，README 里提到的"GitHub Actions backend checks"只是计划项，未实现。

**目标**：PR / push 到 main 时自动跑 21 个已有的集成测试，产出一个可以截图/链接的 CI 状态徽章。

### 任务清单

- [ ] 新建 `.github/workflows/backend-ci.yml`
  - 触发条件：`push` 和 `pull_request` 到 `main`
  - 用 `services:` 字段在 CI runner 里起一个 Postgres 17 容器（因为测试直接连真实 Postgres，不是 Testcontainers，见 PROJECT_SUMMARY 5.2 节），配置和 `application.properties` 一致的账号密码
  - 跑 `./gradlew test`
- [ ] 本地先跑一遍确认 workflow 语法正确（可以用 [act](https://github.com/nektos/act) 本地模拟，或直接 push 到一个测试分支观察 Actions 面板）
- [ ] 确认 21 个测试全部在 CI 环境里通过（本地和 CI 环境的差异，比如时区、编码，可能导致个别测试失败，需要排查）
- [ ] 确认 Flyway migration 在 CI 的全新空 Postgres 容器里能从零一次跑通全部 V1-V4（本地库可能因为反复手动跑而处于"脏"状态，掩盖了 migration 本身的问题；CI 首次跑相当于额外验证了 migration 的可重复性，这也是一条可以诚实写进简历的点）
- [ ] 在 `README.md` 顶部加 CI 状态徽章（GitHub 会自动生成 badge markdown）
- [ ] 更新 `PROJECT_SUMMARY.md` 第 6 节（去掉"没有 CI/CD"）和第 8 节对应条目

**诚实边界**：
- 这只是"跑测试"的 CI，不是 CD（不会自动部署）。不要在简历里写成"CI/CD pipeline"，除非阶段 3 也做了部署自动化——否则准确说法是 "set up CI to run automated tests on every push"。
- 如果面试问"为什么不用 Testcontainers"，如实按 PROJECT_SUMMARY 5.2/9.5 节的答案回答（现状 + 改进方向），不要因为加了 CI 就假装这个问题已经解决。

---

## 阶段 3（可选，视具体 JD 再启动）：AWS 部署

**暂不启动**，除非某个具体投递的 JD 明确要求 AWS 实操经验且是你判断值得投入的岗位。启动前请先回来跟我过一遍再动手，因为：

- 需要决定用 ECS / Elastic Beanstalk / 裸 EC2 中的哪一种，三者的面试追问深度和你需要掌握的知识点不同
- 涉及真实云账号、可能产生费用，需要提前想好资源清理（防止忘记关闭产生账单）
- 面试追问会比 Docker/CI 更深（健康检查策略、密钥管理、多环境配置、网络安全组等），如果只是走通一次部署而没有真正理解，容易在追问下露馅

**如果启动，大致任务方向**（先记录，不展开）：
- [ ] 选择部署方式并说明理由
- [ ] 数据库改用 RDS（而不是容器里的 Postgres），处理连接字符串和安全组
- [ ] 密钥/环境变量管理（不要硬编码，之前 `application.properties` 里的硬编码密码要处理掉）
- [ ] 更新 PROJECT_SUMMARY 部署章节
- [ ] 想清楚"多少条简历 bullet 能诚实地写这个"，不要为了一个部署动作写出好几条夸大的 bullet

---

## 完成阶段 1+2 后，simultaneously 更新的地方

- [ ] `PROJECT_SUMMARY.md`：第 6 节、7 节（可以新增可写进简历的事实）、8 节（划掉已解决的局限）
- [ ] 回到简历项目里，告诉我"Incident Platform 已经 Docker 化 + 有 CI 了"，我会更新对应简历 bullet 的写法（预计会在 Docker Compose 那条基础上，补一条关于 backend 容器化和 CI 的独立 bullet，具体怎么写等你做完再定，避免我现在先写、你还没做完的情况）

