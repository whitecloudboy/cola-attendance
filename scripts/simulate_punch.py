#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
模拟打卡并触发考勤流程（E2E 用）。
- 根据 duty_schedule 获取排班
- 为每人生成上下班打卡时间（可配置迟到/早退等边界）
- 通过 Excel 导入写入 attendance_record
- 触发生成空考勤、日终补录
"""
import argparse
import datetime
import io
import sys
import time

from config import BASE_URL, DEFAULT_TIMEOUT
from http_client import curl_json
from login import login

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

try:
    from openpyxl import Workbook
except ImportError:
    print("[WARN] openpyxl not installed, use: pip install openpyxl")
    Workbook = None


def api_get(path: str, params: dict | None, token: str, timeout: int = DEFAULT_TIMEOUT):
    headers = {"token": token}
    return curl_json("GET", BASE_URL + path, params=params or {}, headers=headers, timeout=timeout)


def api_post(path: str, body: dict | None, token: str, timeout: int = DEFAULT_TIMEOUT):
    headers = {"token": token}
    return curl_json("POST", BASE_URL + path, json_body=body, headers=headers, timeout=timeout)


def list_schedules(token: str, date_str: str, timeout: int) -> list:
    code, resp = api_get("/schedule/schedule/list", {"startDate": date_str, "endDate": date_str}, token, timeout)
    if code != 200 or resp.get("code") != 0:
        return []
    return resp.get("data") or []


def list_shifts(token: str, timeout: int) -> dict:
    """返回 { shiftId: { startTime, endTime } }"""
    code, resp = api_get("/schedule/shift/list", {}, token, timeout)
    if code != 200 or resp.get("code") != 0:
        return {}
    shifts = resp.get("data") or []
    return {s["id"]: {"startTime": s.get("startTime"), "endTime": s.get("endTime")} for s in shifts if s.get("id")}


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


def parse_time(t: str | None) -> datetime.time | None:
    if not t:
        return None
    for fmt in ("%H:%M:%S", "%H:%M"):
        try:
            return datetime.datetime.strptime(str(t).strip(), fmt).time()
        except ValueError:
            continue
    return None


def build_excel_rows(schedules: list, shift_info: dict, date_str: str, start_offset_min: int, end_offset_min: int) -> list:
    """生成 Excel 行 [(userName, eventTime), ...]。"""
    rows = []
    base_date = datetime.datetime.strptime(date_str, "%Y-%m-%d").date()
    for s in schedules:
        uid = s.get("userId")
        uname = s.get("userName") or str(uid)
        sid = s.get("shiftId")
        info = shift_info.get(sid) or {}
        st = parse_time(info.get("startTime")) or datetime.time(8, 30)
        et = parse_time(info.get("endTime")) or datetime.time(17, 30)
        cross_day = st > et if st and et else False
        start_dt = datetime.datetime.combine(base_date, st) + datetime.timedelta(minutes=start_offset_min)
        end_date = base_date + datetime.timedelta(days=1) if cross_day else base_date
        end_dt = datetime.datetime.combine(end_date, et) + datetime.timedelta(minutes=end_offset_min)
        rows.append((uname, start_dt.strftime("%Y-%m-%d %H:%M:%S")))
        rows.append((uname, end_dt.strftime("%Y-%m-%d %H:%M:%S")))
    return rows


def import_excel(token: str, rows: list, timeout: int) -> int:
    """通过 Excel 导入打卡记录。"""
    if not Workbook:
        print("[ERROR] openpyxl required: pip install openpyxl")
        return 0
    import tempfile
    import json
    import subprocess

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
        import os
        try:
            os.unlink(tmp_path)
        except OSError:
            pass


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--date", default=datetime.date.today().strftime("%Y-%m-%d"))
    parser.add_argument("--start-offset", type=int, default=0, help="上班打卡偏移分钟，0=准时")
    parser.add_argument("--end-offset", type=int, default=0, help="下班打卡偏移分钟，0=准时")
    parser.add_argument("--trigger-generate", action="store_true", help="触发生成空考勤")
    parser.add_argument("--trigger-end", action="store_true", help="触发日终补录")
    parser.add_argument("--timeout", type=int, default=DEFAULT_TIMEOUT)
    args = parser.parse_args()

    token = login(timeout=args.timeout)
    print("[INFO] login ok")

    if args.trigger_generate:
        if trigger_generate_empty(args.date, token, args.timeout):
            print("[INFO] trigger generate-empty ok")
        else:
            print("[WARN] trigger generate-empty failed")
        time.sleep(0.3)

    schedules = list_schedules(token, args.date, args.timeout)
    if not schedules:
        print("[WARN] no schedule for date")
        return

    shift_info = list_shifts(token, args.timeout)
    rows = build_excel_rows(
        schedules,
        shift_info,
        args.date,
        args.start_offset,
        args.end_offset,
    )
    if not rows:
        print("[WARN] no rows to import")
        return

    count = import_excel(token, rows, args.timeout)
    print(f"[INFO] import records count={count}")

    time.sleep(0.5)

    if args.trigger_end:
        if trigger_end_task(args.date, token, args.timeout):
            print("[INFO] trigger end-task ok")
        else:
            print("[WARN] trigger end-task failed")


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"[ERROR] {e}")
        sys.exit(1)
