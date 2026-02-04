# cola-attendance

[中文](README.zh-CN.md) | [English](README.md)

![License](https://img.shields.io/badge/License-MIT-green.svg)
![Java](https://img.shields.io/badge/Java-21-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F.svg)
![Vue](https://img.shields.io/badge/Vue-3-42b883.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)

涓€涓潰鍚戝€肩彮/鑰冨嫟鍦烘櫙鐨勫紑婧愮郴缁燂紝瑕嗙洊锛?- 缁勭粐涓庢潈闄愶紙閮ㄩ棬銆佺敤鎴枫€佽鑹层€佽彍鍗曪級
- 鎺掔彮锛堢彮娆°€佹帓鐝粨鏋滐級
- 鑰冨嫟锛堣澶囥€佹墦鍗¤褰曘€佽€冨嫟缁撴灉锛?- 瑙勫垯寮曟搸锛圖SL/SpEL锛?- 瀹氭椂浠诲姟涓?E2E 鍏ㄩ摼璺剼鏈?
## 1. 鎶€鏈爤

- 鍚庣锛歚Spring Boot 3`銆乣JDK 21`銆乣MyBatis-Plus`銆乣Spring Security`銆乣JWT`
- 鍓嶇锛歚Vue 3`銆乣Vite`銆乣Element Plus`
- 鏁版嵁搴擄細`MySQL 8+`

## 2. 浠撳簱缁撴瀯

```text
cola-attendance/
鈹溾攢 attendance-backend/      # Java 鍚庣
鈹溾攢 attendance-frontend/     # Vue 鍓嶇
鈹溾攢 docs/                    # 鏁版嵁搴撹剼鏈€佽璁℃枃妗ｃ€丒2E璇存槑
鈹溾攢 scripts/                 # E2E 鑷姩鍖栬剼鏈紙Python + PowerShell锛?鈹溾攢 docker-compose.yml       # 鏈湴/Demo 杩愯缂栨帓
鈹斺攢 TASKS.md                 # 椤圭洰浠诲姟娓呭崟涓庨樁娈电姸鎬?```

## 3. 鏍稿績鍔熻兘

- 璁よ瘉涓庢潈闄愶細鐧诲綍杩斿洖 JWT锛岃姹傚ご浣跨敤 `token`
- System 妯″潡锛氶儴闂?鐢ㄦ埛/瑙掕壊/鑿滃崟 CRUD锛屾敮鎸佹爲缁撴瀯
- 鎺掔彮妯″潡锛氱彮娆＄鐞嗐€佹帓鐝淮鎶ゃ€佹壒閲忔帓鐝?鍙栨秷
- 鑰冨嫟妯″潡锛氳澶囩鐞嗐€佹墦鍗¤褰曪紙鍚?Excel 瀵煎叆銆佽澶囧洖璋冿級銆佽€冨嫟缁撴灉
- 瑙勫垯寮曟搸锛氭敮鎸侀€氳繃 DSL锛圫pEL锛夐厤缃笂涓嬬彮鍒ゅ畾瑙勫垯
- 瀹氭椂浠诲姟锛?  - 姣忔棩 `01:00` 鐢熸垚绌鸿€冨嫟璁板綍
  - 姣忔棩 `23:55` 鎵ц鏃ョ粓琛ュ綍

## 4. 鏈湴寮€鍙戝揩閫熷紑濮?
### 4.1 鍓嶇疆瑕佹眰

- JDK 21
- Maven 3.8+
- MySQL 8+
- Node.js 18+锛堜粎鍓嶇闇€瑕侊級

### 4.2 鍒濆鍖栨暟鎹簱

1. 鎵ц寤鸿〃鑴氭湰锛歚docs/attendance-db-schema.sql`  `docs/attendance-db-schema-alter.sql`
2. 鍒濆鍖栫鐞嗗憳锛歚attendance-backend/src/main/resources/sql/init-admin.sql`

榛樿绠＄悊鍛樿处鍙凤細
- 鐢ㄦ埛鍚嶏細`admin`
- 瀵嗙爜锛歚123456`

### 4.3 鍚姩鍚庣

```bash
cd attendance-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

鍚姩鍚庡彲璁块棶锛?- Swagger锛歚http://localhost:8080/swagger-ui.html`
- 鍋ュ悍妫€鏌ュ彲鐢ㄤ綘鐨勬帴鍙ｆ祴璇曞伐鍏风洿鎺ラ獙璇佺櫥褰曟帴鍙?
### 4.4 鍚姩鍓嶇锛堝彲閫夛級

```bash
cd attendance-frontend
npm install
npm run dev
```

榛樿鍦板潃锛歚http://localhost:5173`  
寮€鍙戜唬鐞嗭細`/api -> http://localhost:8080`锛堣 `attendance-frontend/vite.config.js`锛?
## 5. Docker 杩愯锛堝悗绔?+ MySQL锛?
```bash
docker compose up -d
```

璇存槑锛?- Compose 浼氬惎鍔?`mysql` 鍜?`backend`
- 鍚庣鍦板潃锛歚http://localhost:8080`
- Swagger锛歚http://localhost:8080/swagger-ui.html`
- 褰撳墠 Compose 榛樿涓嶄細鑷姩鎵ц SQL 鍒濆鍖栬剼鏈紝棣栨浠嶉渶鎵嬪伐瀵煎叆锛?  - `docs/attendance-db-schema.sql`
  - `docs/attendance-db-schema-alter.sql`
  - `attendance-backend/src/main/resources/sql/init-admin.sql`

## 6. E2E 鍏ㄩ摼璺祴璇?
瀹夎渚濊禆锛?
```bash
pip install -r scripts/requirements.txt
```

涓€閿窇鍏ㄩ摼璺細

```powershell
.\scripts\full_link_cycle.ps1 -Cycles 1 -Date 2025-02-05 -SampleCount 5 -ReportDir logs
```

娴佺▼鍖呭惈锛?- 娓呯悊娴嬭瘯鏁版嵁
- 鑷姩鎺掔彮
- 妯℃嫙鎵撳崱骞惰Е鍙戣鍒?- 鏃ョ粓琛ュ綍
- 姝ｇ‘鎬ф牎楠屽苟杈撳嚭鎶ュ憡

璇︾粏璇存槑瑙侊細`docs/e2e-attendance-test.md`

## 7. 鍏抽敭閰嶇疆椤?
寤鸿閫氳繃鐜鍙橀噺瑕嗙洊鏁忔劅閰嶇疆锛?
| 鍙橀噺鍚?| 璇存槑 | 绀轰緥 |
|---|---|---|
| `JWT_SECRET` | JWT 绛惧悕瀵嗛挜锛堢敓浜у繀璁撅紝寤鸿 >= 32 瀛楃锛?| `replace-with-random-secret` |
| `JWT_EXPIRATION_MS` | Token 鏈夋晥鏈燂紙姣锛?| `86400000` |
| `SPRING_DATASOURCE_URL` | 鏁版嵁搴?JDBC URL | `jdbc:mysql://mysql:3306/attendance?...` |
| `SPRING_DATASOURCE_USERNAME` | 鏁版嵁搴撶敤鎴峰悕 | `root` |
| `SPRING_DATASOURCE_PASSWORD` | 鏁版嵁搴撳瘑鐮?| `strong-password` |

鑴氭湰鐩稿叧锛?- `COLA_ATTENDANCE_BASE_URL`锛欵2E 鑴氭湰璇锋眰鐨勫悗绔湴鍧€锛堥粯璁?`http://localhost:8080`锛?
## 8. 鎺ュ彛璁块棶绾﹀畾

- 鐧诲綍鎺ュ彛锛歚POST /system/auth/login`
- 鐧诲綍鎴愬姛鍚庯紝鎶婅繑鍥炵殑 token 鏀惧埌璇锋眰澶达細`token: <jwt>`

## 9. 椤圭洰鐘舵€?
褰撳墠宸插畬鎴愶細
- system銆乻chedule銆乤ttendance銆乺ule-engine 涓诲姛鑳?- E2E 鑷姩鍖栬剼鏈笌鏂囨。

寰呭寮洪」瑙侊細`TASKS.md`

## 10. 璐＄尞涓庤鍙?
- 璐＄尞鎸囧崡锛歚CONTRIBUTING.md`
- 寮€婧愯鍙細`LICENSE`

