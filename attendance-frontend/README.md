# 考勤与排班 - 前端

第一步交付：登录页 + 主框架 + 系统管理（部门 / 用户 / 角色 / 菜单）CRUD 页面。

## 技术栈

- Vue 3 + Vite 5 + Element Plus 2 + Vue Router + Pinia + Axios

## 本地运行

```bash
npm install
npm run dev
```

默认访问 http://localhost:5173。开发时请求会代理到后端 `http://localhost:8080`（见 `vite.config.js`）。

## 默认账号

需先执行后端提供的 `sql/init-admin.sql`，默认管理员：**admin / 123456**。

## 构建

```bash
npm run build
```

产出在 `dist/`，部署时需将 `/api` 反向代理到后端服务。
