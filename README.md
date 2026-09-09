# learn-fullstack —— 五脏俱全的全栈学习项目

一个为「理解软件完整生命周期」而设计的最小项目：
**PostgreSQL + Redis + Kafka 三大主流中间件协作**，覆盖 开发 → 测试 → CI → 部署 全流程。

## 技术栈（个人开发者主流选型）

| 层 | 技术 | 说明 |
|---|---|---|
| 前端 | Vue 3 + Vite + Axios | 组合式 API，国内个人开发者主流 |
| 后端 | Spring Boot 3 + Java 17 | 行业事实标准 |
| ORM | Spring Data JPA (Hibernate) | 方法名即 SQL |
| 数据库 | **PostgreSQL 16** | 新项目主流首选（JSONB/并发/生态） |
| 缓存 | Redis 7 | Cache Aside 缓存 + 原子计数器 |
| 消息队列 | Kafka 3.7 (KRaft 无 Zookeeper) | 异步解耦、削峰 |
| 部署 | Docker + Docker Compose + Nginx | 反向代理 + 多阶段构建 |
| CI | GitHub Actions | push 自动跑测试 |
| 测试 | JUnit 5 + MockMvc | 快速、隔离的单元测试 |
| 可观测性 | Spring Actuator | 健康检查 `/actuator/health` |

## 核心数据流（务必读懂这条链路）

```
浏览器(Vue) --POST /api/messages--> Spring Boot
    1. 写入 PostgreSQL（status=PENDING，持久化是事实来源）
    2. 发送消息到 Kafka topic "messages"
浏览器 --GET /api/messages--> Spring Boot
    3. 先查 Redis 缓存（Cache Aside），未命中查 PG 并回填 60s
Kafka 消费者（同一后端内）
    4. 消费消息 → 状态改为 CONSUMED → Redis 计数器 +1
前端每 2 秒轮询，可亲眼看到状态从 PENDING 变为 CONSUMED
```

**这条链路演示了三者最经典的协作方式：数据库保真、队列解耦、缓存提速。**

## 快速开始（开发模式）

前置要求：JDK 17、Maven 3.8+、Node 18+、Docker Desktop

```bash
# 1. 启动基础设施（只启动 PG / Redis / Kafka 三个容器）
docker compose up -d

# 2. 启动后端（http://localhost:8080）
cd backend && mvn spring-boot:run

# 3. 启动前端（http://localhost:5173）
cd frontend && npm install && npm run dev
```

打开 http://localhost:5173 发一条消息，观察状态变化；
用 `docker exec -it demo-redis redis-cli` 敲 `KEYS *`、`GET metrics:consumed`
直接观察 Redis 里发生了什么 —— 这是最直观的学习方式。

## 测试

```bash
cd backend
mvn test        # 单元测试不需要任何中间件，秒级完成
```

测试理念（看 `MessageControllerTest`）：
- 单元测试要**快**且**隔离**，所以用 `@WebMvcTest` 只加载 Web 层、Mock 掉 Service
- 依赖真实中间件的测试叫集成测试（进阶可用 Testcontainers，见下文学习路线）

## CI 持续集成

项目已含 `.github/workflows/ci.yml`。把项目推到 GitHub 仓库，
每次 push 会自动：后端跑测试 → 前端构建，失败会挡住合并。
这就是"持续集成"的最小可用形态。

## 部署（体验完整上线）

```bash
docker compose --profile app up -d --build
# 打开 http://localhost（80 端口，nginx 托管前端并反代 /api）
```

体验点：
- `--profile app`：compose profiles，区分"基础设施"与"完整部署"
- 多阶段 Dockerfile：构建环境（Maven/Node）不进最终镜像，体积小更安全
- nginx.conf：静态托管 + SPA 路由回退 + API 反向代理 —— 生产标准做法
- 环境变量覆盖（`SPRING_DATASOURCE_URL` 等）：同一份代码跑不同环境，不改编译产物

## 软件生命周期在本项目中的映射

| 阶段 | 在哪里体现 |
|---|---|
| 需求/设计 | 本 README 的数据流设计 |
| 开发 | backend / frontend 源码，注释即教程 |
| 测试 | `src/test`（单元测试）、curl 手工验证 |
| 集成 | CI workflow，push 即验证 |
| 部署 | docker-compose --profile app |
| 运维/监控 | Actuator `/actuator/health`、日志、Redis 指标 |
| 迭代 | 改代码 → 提交 → CI → 重新部署，回到起点 |

## 进阶学习路线（按顺序）

1. **Flyway**：数据库版本化管理（替代 JPA 的 ddl-auto: update）
2. **Testcontainers**：测试中用真实 PG/Redis/Kafka 容器做集成测试
3. **Prometheus + Grafana**：把 Actuator 的 metrics 接入可视化监控
4. **Dockerfile 优化**：JVM 分层 jar、前端加 npm ci 缓存
5. **云部署**：买台便宜云主机，用 GitHub Actions SSH 部署上去，走完真正的"上线"
6. **认证**：加 Spring Security + JWT，体验登录态管理
