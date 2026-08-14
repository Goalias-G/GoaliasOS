# GoaliasOS 协作指南

## 项目定位

GoaliasOS 是以“个人成长”为核心的生活管理服务端。系统隐喻是：目标是进程、习惯是服务、反思是日志、成长是持续更新的内核。功能设计应支持“记录真实状态 → 分析/调度 → 反馈行动 → 复盘迭代”的闭环，而非仅新增孤立 CRUD。

## 技术与运行

- Java 17、Spring Boot 3.4.4、Maven 多模块。
- MyBatis-Plus + MySQL；Redis；Sa-Token；MinIO；Spring AI / LangChain4j。
- 默认服务端口 `7000`、上下文路径 `/goalias-os`；环境由 Maven profile `dev` / `prod` 与 `application*.yml` 控制。

```powershell
mvn clean package -DskipTests
mvn spring-boot:run -pl os-startup -am
mvn test
```

本地启动前根据 `.env.example`、`application-dev.yml` 准备 MySQL、Redis 及所需对象存储/AI 配置。密钥、Token、连接串仅通过环境变量或部署注入，绝不写入代码、SQL 或文档示例。

## 分层与模块边界

```text
os-startup/  应用入口、运行装配、全局/环境配置、定时任务
os-web/      REST Controller、请求参数、权限入口、接口编排
os-service/  领域模型、Mapper、Service 接口与业务实现
os-common/   Web、安全、缓存、OSS、通知、AI、限流等公共能力
sql/         数据库脚本
docs/        项目文档
```

`os-web` 只负责 HTTP 边界与编排，不承载复杂业务或 SQL；业务实体、Mapper 和 Service 放入相应 `os-service` 子模块；可复用基础能力才进入 `os-common`；启动、调度和环境配置属于 `os-startup`。按既有模块归属扩展，勿制造跨层反向依赖。

现有核心领域包括：用户/认证与权限、生活记录与分类、每日健康/知识/首页统计、财务分类与流水、AI 模型/会话/消息/SSE/Token 统计、知识库附件解析/分片/向量检索、系统配置与定时任务。

## 接口与业务实现

常见链路为：`Controller → Service → Mapper → MySQL/Redis/外部服务`。

- 新接口先在相邻模块找同类实现；Controller 做 `@Valid` 参数校验、权限声明和轻量编排，Service 承担事务与业务规则，Mapper 专注数据访问。
- 单体结果使用 `R<T>`，分页复用 `PageQuery` 与 `TableDataInfo`；不要自建不兼容的响应/分页格式。
- 业务错误抛 `ServiceException`，交由全局异常处理；不在 Controller 吞异常或拼装错误响应。
- 复用审计字段、逻辑删除、MyBatis-Plus 约定与现有基础实体；SQL/字段变更同步更新 `sql/` 和相关 VO/DTO/Mapper。
- 缓存复用 `RedisService`，文件复用 `MinioService`/OSS 服务；避免直接复制基础设施实现。
- AI/SSE/外部调用必须考虑超时、取消、失败降级与幂等；敏感用户数据与密钥不得进入日志。关键业务保留有意义的上下文日志。

## 安全与配置

- 优先复用 Sa-Token、权限注解、XSS 过滤、参数校验、`@Sensitive` 脱敏、重复提交防护及限流熔断能力。
- 请求输入、文件上传和外部回调都要显式校验；变更开放路径、CORS、安全排除项须谨慎并说明理由。
- 配置优先使用 `@ConfigurationProperties`；`application.yml` 为公共项，环境差异放 `application-dev.yml` / `application-prod.yml`。

## 代码风格与检查

- 遵循现有包名、命名与 Lombok/注解风格；类名 PascalCase，方法/字段 camelCase，接口与实现职责清晰。
- 注释说明业务意图、规则或边界，不复述代码；中文文本使用 UTF-8，新增文件无 BOM。
- 改动前先检查相关子模块、实体、接口与 SQL，避免重复造轮子；保留用户已有无关改动。
- 至少运行受影响模块的 Maven 编译/测试；跨模块、依赖或启动配置变动时运行根目录 `mvn clean package -DskipTests`。