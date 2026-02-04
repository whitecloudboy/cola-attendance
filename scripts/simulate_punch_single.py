#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
单条模拟打卡：调用 POST /attendance/record 写入一条打卡记录（需登录）。
也可用于设备回调模拟：调用 POST /attendance/record/callback（无需登录）。
"""
import argparse
import sys

from config import BASE_URL, DEFAULT_TIMEOUT
from http_client import curl_json
from login import login

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")


def punch_single(token: str, user_id: int, event_time: str, device_id: int | None, timeout: int) -> bool:
    """单条打卡（需 token）。"""
    url = BASE_URL + "/attendance/record"
    body = {"userId": user_id, "eventTime": event_time}
    if device_id is not None:
        body["deviceId"] = device_id
    status, data = curl_json("POST", url, json_body=body, headers={"token": token}, timeout=timeout)
    ok = status == 200 and data.get("code") == 0
    if ok:
        print(f"[OK] record id={data.get('data', {}).get('id')}")
    else:
        print(f"[FAIL] {status} {data}")
    return ok


def punch_callback(employee_no: str, event_time: str, device_code: str | None, device_ip: str | None, timeout: int) -> bool:
    """设备回调模拟（无需登录）。"""
    url = BASE_URL + "/attendance/record/callback"
    body = {"employeeNo": employee_no, "eventTime": event_time}
    if device_code:
        body["deviceCode"] = device_code
    if device_ip:
        body["deviceIp"] = device_ip
    status, data = curl_json("POST", url, json_body=body, timeout=timeout)
    ok = status == 200 and data.get("code") == 0
    if ok:
        print(f"[OK] record id={data.get('data', {}).get('id')}")
    else:
        print(f"[FAIL] {status} {data}")
    return ok


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", choices=["api", "callback"], default="api",
                        help="api=单条打卡(需登录), callback=设备回调(无需登录)")
    parser.add_argument("--user-id", type=int, help="用户ID（api 模式必填）")
    parser.add_argument("--employee-no", type=str, help="工号/用户名（callback 模式必填）")
    parser.add_argument("--event-time", type=str, required=True, help="打卡时间，如 2025-02-05 08:30:00")
    parser.add_argument("--device-id", type=int, default=None)
    parser.add_argument("--device-code", type=str, default=None)
    parser.add_argument("--device-ip", type=str, default=None)
    parser.add_argument("--timeout", type=int, default=DEFAULT_TIMEOUT)
    args = parser.parse_args()

    if args.mode == "api":
        if args.user_id is None:
            print("[ERROR] --user-id 必填")
            sys.exit(1)
        token = login(timeout=args.timeout)
        ok = punch_single(token, args.user_id, args.event_time, args.device_id, args.timeout)
    else:
        if not args.employee_no:
            print("[ERROR] --employee-no 必填")
            sys.exit(1)
        ok = punch_callback(args.employee_no, args.event_time, args.device_code, args.device_ip, args.timeout)
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
