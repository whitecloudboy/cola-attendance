# 贡献指南

欢迎为考勤与排班（cola-attendance）项目贡献代码或文档。

## 如何参与

1. **Fork 本仓库**，在本地克隆你 Fork 后的仓库。
2. **创建分支**：`git checkout -b feature/your-feature` 或 `fix/your-fix`。
3. **修改与自测**：确保后端 `mvn test`、前端与 E2E 脚本（见 `scripts/`）按预期运行。
4. **提交**：请写清晰的 commit message，可引用 `TASKS.md` 中的任务描述。
5. **推送到你的 Fork**，然后在本仓库发起 **Pull Request**，简要说明改动目的与范围。

## 约定

- 后端：Java 21、Spring Boot 3、MyBatis-Plus，保持与现有包名与风格一致。
- 前端：Vue 3 + Vite + Element Plus，遵循现有目录与 API 封装方式。
- 配置与密钥：勿提交真实密码或 JWT 密钥，使用环境变量或占位符（见 README「环境变量」）。

## 讨论

如有功能建议或 Bug 反馈，欢迎提 Issue。
