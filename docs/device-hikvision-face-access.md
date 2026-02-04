# 海康威视人脸门禁对接说明

本文说明如何将**一款**海康人脸门禁设备对接到轻量级排班考勤平台（qianyi），形成「刷脸即打卡」的考勤采集。

---

## 1. 两种常见对接方式

| 方式 | 说明 | 适用 |
|------|------|------|
| **HTTP 回调** | 设备或海康云平台将通行事件 **POST 到我们提供的 URL** | 设备/平台支持「事件通知 URL」配置时优先用 |
| **SDK 连接** | 我们的服务 **主动连接设备**，通过海康 NetSDK 接收报警/事件回调 | 设备不支持 HTTP 上报时使用（如 ies 原工程） |

---

## 2. 方式一：HTTP 回调（推荐，平台已就绪）

### 2.1 平台已有能力

- **接口**：`POST /attendance/record/callback`（无需登录，已放行）
- **请求体（JSON）**：

```json
{
  "employeeNo": "工号或用户名，必填",
  "eventTime": "2025-02-05 08:30:00",
  "deviceCode": "设备编码，与 deviceIp 二选一",
  "deviceIp": "设备 IP，与 deviceCode 二选一",
  "temperature": "体温，可选",
  "eventType": "如 in/out，可选",
  "remark": "备注，可选"
}
```

- **逻辑**：根据 `employeeNo` 解析为 `sys_user`（先 username 后 display_name），根据 `deviceCode` 或 `deviceIp` 解析为 `attendance_device`，写入一条 `attendance_record` 并触发考勤规则引擎。

### 2.2 设备/平台侧需要做的

1. **确认型号是否支持 HTTP 通知**  
   在设备 Web 配置或海康云平台（如综合安防、云眸）中查看是否有「事件订阅」「HTTP 通知」「消息推送」等，并配置 **通知地址** 为：

   ```
   https://你的服务器域名/attendance/record/callback
   ```

2. **人员标识一致**  
   设备里的人脸/卡号对应的「工号」或「员工号」，需要与 `sys_user` 的 **username** 或 **display_name** 一致，这样 `employeeNo` 才能解析到对应用户。

3. **设备在平台中录入**  
   在考勤平台「考勤设备」中新增该设备，**设备编码** 或 **IP 地址** 与设备上报的 `deviceCode`/`deviceIp` 一致，便于关联到 `attendance_device`。

### 2.3 若设备推送格式与平台不一致

若海康推送的是**固定格式**（如 XML 或另一种 JSON），平台可增加一层**海康专用适配接口**，例如：

- `POST /attendance/record/callback/hikvision`  
  请求体按海康文档解析（如 `employeeNo`、`eventTime`、`deviceIp` 等），再转换为上述 JSON 调用内部 `saveFromDeviceCallback`。  
  这样设备配置的 URL 指向 `/attendance/record/callback/hikvision` 即可。

---

## 3. 方式二：SDK 连接设备（设备不支持 HTTP 上报时）

原 ies 工程采用此方式：使用海康 **HCNetSDK**，由**我们的程序**连接门禁设备，布防后通过 **报警回调** 收到通行事件。

### 3.1 数据流

1. 服务用 NetSDK 登录设备（IP、端口、用户名、密码）。
2. 调用布防接口，注册报警回调。
3. 设备有人脸通行时，SDK 回调上报：
   - 设备 IP
   - 工号：`dwEmployeeNo`
   - 时间：`struTime` → 转为 `yyyy-MM-dd HH:mm:ss`
   - 可选：体温、口罩等
4. 回调里调用「写入打卡记录」的逻辑。

### 3.2 在 qianyi 中的落地方案

- **不把 NetSDK 放进 attendance-backend**（避免 native 库、部署复杂）。
- 单独做一个**采集进程/服务**（如 Java 或 Python）：
  - 使用海康 SDK 连接指定型号设备；
  - 在报警回调里，将 `employeeNo`、`eventTime`、`deviceIp` 等组装成 JSON；
  - **HTTP POST 到** `http(s)://attendance-backend 地址/attendance/record/callback`（即复用当前通用回调接口）。
- 这样考勤平台仍只暴露 HTTP 回调，对接形态统一；设备差异由采集服务消化。

### 3.3 需要准备的

- 海康 NetSDK 开发包（含对应型号说明）。
- 设备网络可达、且已开启「上传事件」或「报警上传」。
- `sys_user` 中有一列与设备工号对应（如 username 存工号），或做一层工号↔用户映射。

---

## 4. 建议对接顺序（针对「一款」海康人脸门禁）

1. **查该型号/平台是否支持 HTTP 事件通知**  
   支持 → 优先用 **方式一**，在设备/平台配置 `POST /attendance/record/callback`（或后续的 `/callback/hikvision`）。
2. **人员与设备在平台中维护好**  
   用户：username/display_name 与设备工号一致；设备：在「考勤设备」中录好 deviceCode 或 IP。
3. **若仅支持 SDK**  
   采用 **方式二**：独立采集服务 + 内部调用现有 `POST /attendance/record/callback`，不修改核心后端即可完成对接。

当前 **设备回调模式** 与 **单条打卡 API** 已在 qianyi 中实现，按上述方式即可对接一款海康人脸门禁；若你提供该型号的 HTTP 推送示例或 SDK 回调字段，可以再细化成具体接口与字段映射（含是否增加 `/callback/hikvision` 适配层）。
