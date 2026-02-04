#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
清理考勤与排班测试数据（E2E 用）。
顺序：attendance_record → attendance_result → duty_schedule
"""
import argparse
import datetime
import sys

from config import BASE_URL, DEFAULT_TIMEOUT
from http_client import curl_json
from login import login

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")


def api_get(path: str, params: dict | None, token: str, timeout: int = DEFAULT_TIMEOUT):
    headers = {"token": token}
    return curl_json("GET", BASE_URL + path, params=params or {}, headers=headers, timeout=timeout)


def api_delete(path: str, json_body: list | None, token: str, timeout: int = DEFAULT_TIMEOUT):
    headers = {"token": token}
    return curl_json("DELETE", BASE_URL + path, json_body=json_body or [], headers=headers, timeout=timeout)


def list_result_ids(token: str, date_str: str, timeout: int) -> list:
    """查询 attendance_result 分页获取 id 列表。"""
    ids = []
    page = 1
    while True:
        code, data = api_get(
            "/attendance/result/page",
            {"current": page, "size": 100, "startDate": date_str, "endDate": date_str},
            token,
            timeout,
        )
        if code != 200 or data.get("code") != 0:
            break
        records = data.get("data", {}).get("records") or []
        if not records:
            break
        for r in records:
            if r.get("id"):
                ids.append(r["id"])
        if len(records) < 100:
            break
        page += 1
    return ids


def list_record_ids(token: str, date_str: str, timeout: int) -> list:
    """查询 attendance_record 分页获取 id 列表。"""
    ids = []
    page = 1
    while True:
        code, data = api_get(
            "/attendance/record/page",
            {"current": page, "size": 100, "startDate": date_str, "endDate": date_str},
            token,
            timeout,
        )
        if code != 200 or data.get("code") != 0:
            break
        records = data.get("data", {}).get("records") or []
        if not records:
            break
        for r in records:
            if r.get("id"):
                ids.append(r["id"])
        if len(records) < 100:
            break
        page += 1
    return ids


def list_schedule_ids(token: str, date_str: str, timeout: int) -> list:
    """查询 duty_schedule 列表获取 id。"""
    code, resp = api_get(
        "/schedule/schedule/list",
        {"startDate": date_str, "endDate": date_str},
        token,
        timeout,
    )
    if code != 200 or resp.get("code") != 0:
        return []
    lst = resp.get("data") or []
    return [s["id"] for s in lst if s.get("id")]


def batch_delete_result(ids: list, token: str, timeout: int) -> tuple[int, dict]:
    """逐条删除 attendance_result。"""
    for rid in ids:
        code, data = curl_json(
            "DELETE",
            BASE_URL + f"/attendance/result/{rid}",
            headers={"token": token},
            timeout=timeout,
        )
        if code != 200 or (data.get("code") is not None and data.get("code") != 0):
            return code, data
    return 200, {"msg": f"deleted {len(ids)} results"}


def batch_delete_record(ids: list, token: str, timeout: int) -> tuple[int, dict]:
    """逐条删除 attendance_record。"""
    for rid in ids:
        code, data = curl_json(
            "DELETE",
            BASE_URL + f"/attendance/record/{rid}",
            headers={"token": token},
            timeout=timeout,
        )
        if code != 200 or (data.get("code") is not None and data.get("code") != 0):
            return code, data
    return 200, {"msg": f"deleted {len(ids)} records"}


def batch_delete_schedule(ids: list, token: str, timeout: int) -> tuple[int, dict]:
    """删除 duty_schedule。"""
    for sid in ids:
        code, data = curl_json(
            "DELETE",
            BASE_URL + f"/schedule/schedule/{sid}",
            headers={"token": token},
            timeout=timeout,
        )
        if code != 200:
            return code, data
        if data.get("code") != 0 and data.get("code") is not None:
            return code, data
    return 200, {"msg": f"deleted {len(ids)} schedules"}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--date", default=datetime.date.today().strftime("%Y-%m-%d"))
    parser.add_argument("--timeout", type=int, default=DEFAULT_TIMEOUT)
    args = parser.parse_args()

    token = login(timeout=args.timeout)
    print("[INFO] login ok")

    # 1. 先删 attendance_record（考勤结果可能依赖打卡记录逻辑，先清打卡）
    record_ids = list_record_ids(token, args.date, args.timeout)
    print(f"[INFO] date={args.date} record_ids={len(record_ids)}")
    if record_ids:
        code, data = batch_delete_record(record_ids, token, args.timeout)
        print(f"[INFO] delete records code={code} resp={data}")

    # 2. 再删 attendance_result
    result_ids = list_result_ids(token, args.date, args.timeout)
    print(f"[INFO] result_ids={len(result_ids)}")
    if result_ids:
        code, data = batch_delete_result(result_ids, token, args.timeout)
        print(f"[INFO] delete results code={code} resp={data}")

    # 3. 最后删 duty_schedule
    schedule_ids = list_schedule_ids(token, args.date, args.timeout)
    print(f"[INFO] schedule_ids={schedule_ids}")
    if schedule_ids:
        code, data = batch_delete_schedule(schedule_ids, token, args.timeout)
        print(f"[INFO] delete schedule code={code} resp={data}")

    print("[INFO] cleanup done")


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"[ERROR] {e}")
        sys.exit(1)
