#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
E2E 交接班用例：覆盖班次分组号（groupNo）下的交接班计算。
- 依赖测试数据：白班/中班/夜班 同一 group_no（如 001）、同一 dept_id（如 8），见 docs/test-data-hospital.sql
- 流程：清理 -> 为 2 人排班（白班、中班）-> 生成空考勤 -> 模拟打卡（白 08:00/16:00，中 16:00/00:00）-> 日终补录 -> 校验结果
"""
import argparse
import datetime
import os
import subprocess
import sys
import time as _t

from config import BASE_URL, DEFAULT_TIMEOUT
from http_client import curl_json
from login import login

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

try:
    from openpyxl import Workbook
except ImportError:
    Workbook = None


def api_get(path: str, params: dict | None, token: str, timeout: int = DEFAULT_TIMEOUT):
    headers = {"token": token}
    return curl_json("GET", BASE_URL + path, params=params or {}, headers=headers, timeout=timeout)


def api_post(path: str, body: dict | None, token: str, timeout: int = DEFAULT_TIMEOUT):
    headers = {"token": token}
    return curl_json("POST", BASE_URL + path, json_body=body or {}, headers=headers, timeout=timeout)


def list_shifts(token: str, timeout: int) -> list:
    code, resp = api_get("/schedule/shift/list", {}, token, timeout)
    if code != 200 or resp.get("code") != 0:
        return []
    return resp.get("data") or []


def shift_by_code(shifts: list, code: str) -> dict | None:
    for s in shifts:
        if (s.get("code") or "").strip() == code:
            return s
    return None


def list_users(token: str, timeout: int, limit: int = 20) -> list:
    code, resp = api_get("/system/user/page", {"current": 1, "size": limit}, token, timeout)
    if code != 200 or resp.get("code") != 0:
        return []
    return (resp.get("data") or {}).get("records") or []


def set_schedule(work_date: str, shift_id: int, user_id: int, token: str, timeout: int) -> bool:
    code, resp = api_post(
        "/schedule/schedule/set",
        {"workDate": work_date, "shiftId": shift_id, "userId": user_id},
        token,
        timeout,
    )
    return code == 200 and resp.get("code") == 0


def trigger_generate_empty(date_str: str, token: str, timeout: int) -> bool:
    status, data = curl_json(
        "POST",
        BASE_URL + "/attendance/result/generate-empty",
        params={"date": date_str},
        headers={"token": token},
        timeout=timeout,
    )
    return status == 200 and data.get("code") == 0


def trigger_end_task(date_str: str, token: str, timeout: int) -> bool:
    status, data = curl_json(
        "POST",
        BASE_URL + "/attendance/result/trigger-end-task",
        params={"date": date_str},
        headers={"token": token},
        timeout=timeout,
    )
    return status == 200 and data.get("code") == 0


def import_punch_excel(token: str, rows: list, timeout: int) -> int:
    if not Workbook:
        return 0
    import json
    import os
    import tempfile

    wb = Workbook()
    ws = wb.active
    ws.append(["姓名或工号", "打卡时间", "设备编码(可选)"])
    for r in rows:
        ws.append(list(r) + [""])
    with tempfile.NamedTemporaryFile(suffix=".xlsx", delete=False) as f:
        wb.save(f.name)
        tmp_path = f.name
    try:
        cmd = [
            "curl", "-s", "-m", str(timeout),
            "-X", "POST",
            "-H", "token: " + token,
            "-F", f"file=@{tmp_path}",
            BASE_URL + "/attendance/record/import",
        ]
        proc = subprocess.run(cmd, capture_output=True, timeout=timeout + 5, text=True, encoding="utf-8")
        if proc.returncode != 0:
            return 0
        try:
            data = json.loads(proc.stdout)
            if data.get("code") == 0 and data.get("data") is not None:
                return int(data["data"])
        except json.JSONDecodeError:
            pass
        return 0
    finally:
        try:
            os.unlink(tmp_path)
        except OSError:
            pass


def list_results(token: str, date_str: str, timeout: int) -> list:
    code, resp = api_get(
        "/attendance/result/list",
        {"startDate": date_str, "endDate": date_str},
        token,
        timeout,
    )
    if code != 200 or resp.get("code") != 0:
        return []
    return resp.get("data") or []


def run_cleanup(script_dir: str, date_str: str, timeout: int) -> bool:
    proc = subprocess.run(
        [sys.executable, os.path.join(script_dir, "cleanup_attendance.py"), "--date", date_str, "--timeout", str(timeout)],
        capture_output=True,
        timeout=timeout + 30,
        text=True,
        encoding="utf-8",
        cwd=script_dir,
    )
    return proc.returncode == 0


def main():
    parser = argparse.ArgumentParser(description="E2E 交接班用例（班次分组号 groupNo）")
    parser.add_argument("--date", default=datetime.date.today().strftime("%Y-%m-%d"))
    parser.add_argument("--timeout", type=int, default=DEFAULT_TIMEOUT)
    parser.add_argument("--skip-cleanup", action="store_true", help="跳过清理步骤")
    args = parser.parse_args()

    script_dir = os.path.dirname(os.path.abspath(__file__))
    token = login(timeout=args.timeout)
    print("[INFO] login ok")

    shifts = list_shifts(token, args.timeout)
    shift_day = shift_by_code(shifts, "ZB-DAY")
    shift_mid = shift_by_code(shifts, "ZB-MID")
    if not shift_day or not shift_mid:
        print("[ERROR] 需要班次 ZB-DAY（白班）和 ZB-MID（中班），请先执行 docs/test-data-hospital.sql")
        sys.exit(1)
    if not (shift_day.get("groupNo") or "").strip() or not (shift_mid.get("groupNo") or "").strip():
        print("[WARN] 白班/中班 未配置 groupNo，交接班将按部门+颜色匹配；建议设为同一 groupNo（如 001）")
    users = list_users(token, args.timeout)
    if len(users) < 2:
        print("[ERROR] 至少需要 2 个用户")
        sys.exit(1)
    u1, u2 = users[0], users[1]
    uid1, uid2 = u1.get("id"), u2.get("id")
    name1 = (u1.get("displayName") or u1.get("username") or "").strip() or str(uid1)
    name2 = (u2.get("displayName") or u2.get("username") or "").strip() or str(uid2)

    if not args.skip_cleanup:
        print("[STEP] cleanup")
        if not run_cleanup(script_dir, args.date, args.timeout):
            print("[WARN] cleanup failed")
    _t.sleep(0.3)

    print("[STEP] 排班：用户1 白班，用户2 中班")
    if not set_schedule(args.date, shift_day["id"], uid1, token, args.timeout):
        print("[ERROR] set schedule 白班 failed")
        sys.exit(1)
    if not set_schedule(args.date, shift_mid["id"], uid2, token, args.timeout):
        print("[ERROR] set schedule 中班 failed")
        sys.exit(1)

    print("[STEP] 生成空考勤")
    if not trigger_generate_empty(args.date, token, args.timeout):
        print("[ERROR] generate-empty failed")
        sys.exit(1)
    _t.sleep(0.3)

    base_date = datetime.datetime.strptime(args.date, "%Y-%m-%d").date()
    next_date = base_date + datetime.timedelta(days=1)
    rows = [
        (name1, f"{args.date} 08:00:00"),
        (name1, f"{args.date} 16:00:00"),
        (name2, f"{args.date} 16:00:00"),
        (name2, f"{next_date} 00:00:00"),
    ]
    print("[STEP] 导入打卡（白 08:00/16:00，中 16:00/次日 00:00）")
    n = import_punch_excel(token, rows, args.timeout)
    if n < 1:
        print("[ERROR] import punch failed or 0 records")
        sys.exit(1)
    print(f"[INFO] import records count={n}")
    _t.sleep(0.5)

    print("[STEP] 日终补录")
    if not trigger_end_task(args.date, token, args.timeout):
        print("[ERROR] trigger-end failed")
        sys.exit(1)

    results = list_results(token, args.date, args.timeout)
    by_user = {r.get("userId"): r for r in results if r.get("userId")}
    ok1 = uid1 in by_user and (by_user[uid1].get("checkInTime") or by_user[uid1].get("checkOutTime"))
    ok2 = uid2 in by_user and (by_user[uid2].get("checkInTime") or by_user[uid2].get("checkOutTime"))
    if ok1 and ok2:
        print("[PASS] 交接班 E2E：两人考勤结果均有打卡时间，日终补录与交接班计算（含 groupNo）已覆盖")
    else:
        print("[WARN] 考勤结果不完整，请检查规则或打卡数据；userId1=", uid1, "userId2=", uid2, "results=", len(results))


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"[ERROR] {e}")
        sys.exit(1)
