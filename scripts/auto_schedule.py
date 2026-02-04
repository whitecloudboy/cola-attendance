#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
自动排班（E2E 用）。
- 获取 sys_user（is_duty_person=1 或全部）与 duty_shift
- 为指定日期随机分配班次
"""
import argparse
import datetime
import random
import sys

from config import BASE_URL, DEFAULT_TIMEOUT
from http_client import curl_json
from login import login

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")


def api_get(path: str, params: dict | None, token: str, timeout: int = DEFAULT_TIMEOUT):
    headers = {"token": token}
    return curl_json("GET", BASE_URL + path, params=params or {}, headers=headers, timeout=timeout)


def api_post(path: str, body: dict, token: str, timeout: int = DEFAULT_TIMEOUT):
    headers = {"token": token}
    return curl_json("POST", BASE_URL + path, json_body=body, headers=headers, timeout=timeout)


def list_users(token: str, timeout: int) -> list:
    """分页获取用户，优先 is_duty_person=1。"""
    users = []
    page = 1
    while True:
        code, resp = api_get("/system/user/page", {"current": page, "size": 100}, token, timeout)
        if code != 200 or resp.get("code") != 0:
            break
        data = resp.get("data") or {}
        records = data.get("records") or []
        if not records:
            break
        users.extend(records)
        if len(records) < 100:
            break
        page += 1
    duty = [u for u in users if u.get("isDutyPerson") == 1]
    return duty if duty else users


def list_shifts(token: str, timeout: int) -> list:
    """获取班次列表。"""
    code, resp = api_get("/schedule/shift/list", {"deptId": None}, token, timeout)
    if code != 200 or resp.get("code") != 0:
        return []
    return resp.get("data") or []


def set_schedule(work_date: str, shift_id: int, user_id: int, token: str, timeout: int) -> bool:
    """设置排班。"""
    code, resp = api_post(
        "/schedule/schedule/set",
        {"workDate": work_date, "shiftId": shift_id, "userId": user_id},
        token,
        timeout,
    )
    return code == 200 and resp.get("code") == 0


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--date", default=datetime.date.today().strftime("%Y-%m-%d"))
    parser.add_argument("--ban-count", type=int, default=0, help="每个用户排几个班次，0=每人1个随机班次")
    parser.add_argument("--timeout", type=int, default=DEFAULT_TIMEOUT)
    args = parser.parse_args()

    token = login(timeout=args.timeout)
    print("[INFO] login ok")

    users = list_users(token, args.timeout)
    shifts = list_shifts(token, args.timeout)
    if not users:
        print("[WARN] no users")
        return
    if not shifts:
        print("[WARN] no shifts, create shifts first")
        return

    count = 0
    ban_count = max(1, args.ban_count) if args.ban_count else 1
    for u in users:
        uid = u.get("id")
        if not uid:
            continue
        for _ in range(ban_count):
            shift = random.choice(shifts)
            sid = shift.get("id")
            if not sid:
                continue
            if set_schedule(args.date, sid, uid, token, args.timeout):
                count += 1
    print(f"[INFO] auto schedule date={args.date} count={count}")


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"[ERROR] {e}")
        sys.exit(1)
