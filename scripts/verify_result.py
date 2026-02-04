#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
正确性校验：根据打卡记录 + 班次推算期望的 checkIn/checkOut 与状态，与 attendance_result 比对。
"""
import argparse
import datetime
import json
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


def parse_dt(s: str | None) -> datetime.datetime | None:
    if not s:
        return None
    for fmt in ("%Y-%m-%d %H:%M:%S", "%Y-%m-%dT%H:%M:%S"):
        try:
            return datetime.datetime.strptime(str(s).replace("T", " ")[:19], fmt)
        except ValueError:
            continue
    return None


def parse_time(s: str | None) -> datetime.time | None:
    if not s:
        return None
    for fmt in ("%H:%M:%S", "%H:%M"):
        try:
            return datetime.datetime.strptime(str(s).strip(), fmt).time()
        except ValueError:
            continue
    return None


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


def list_records_by_user(token: str, user_id: int, date_str: str, timeout: int) -> list:
    code, resp = api_get(
        "/attendance/record/page",
        {"current": 1, "size": 50, "userId": user_id, "startDate": date_str, "endDate": date_str},
        token,
        timeout,
    )
    if code != 200 or resp.get("code") != 0:
        return []
    data = resp.get("data") or {}
    return data.get("records") or []


def pick_checkin(day: datetime.date, start_time: datetime.time, records: list) -> datetime.datetime | None:
    """取最靠近计划上班时间的打卡。"""
    if not records:
        return None
    base = datetime.datetime.combine(day, start_time)
    candidates = []
    for r in records:
        dt = parse_dt(r.get("eventTime"))
        if dt:
            candidates.append(dt)
    if not candidates:
        return None
    return min(candidates, key=lambda x: abs((x - base).total_seconds()))


def pick_checkout(
    day: datetime.date,
    start_time: datetime.time,
    end_time: datetime.time,
    records: list,
    cross_day: bool,
) -> datetime.datetime | None:
    """取最靠近计划下班时间且在中点之后的打卡。"""
    if not records:
        return None
    end_base = datetime.datetime.combine(day, end_time)
    if cross_day:
        end_base += datetime.timedelta(days=1)
    start_base = datetime.datetime.combine(day, start_time)
    mid = start_base + datetime.timedelta(seconds=(end_base - start_base).total_seconds() / 2)
    candidates = []
    for r in records:
        dt = parse_dt(r.get("eventTime"))
        if not dt:
            continue
        if cross_day and dt.date() < day:
            dt = dt + datetime.timedelta(days=1)
        if dt >= mid:
            candidates.append(dt)
    if not candidates:
        return None
    return min(candidates, key=lambda x: abs((x - end_base).total_seconds()))


def verify_one(result: dict, records: list, date_str: str) -> tuple[bool, str, dict]:
    planned_start = parse_time(result.get("plannedStartTime")) or datetime.time(8, 30)
    planned_end = parse_time(result.get("plannedEndTime")) or datetime.time(17, 30)
    cross_day = planned_start > planned_end
    day = datetime.datetime.strptime(date_str, "%Y-%m-%d").date()

    exp_start = pick_checkin(day, planned_start, records)
    exp_end = pick_checkout(day, planned_start, planned_end, records, cross_day)

    actual_start = parse_time(result.get("checkInTime"))
    actual_end = parse_time(result.get("checkOutTime"))

    detail = {
        "userName": result.get("userName"),
        "userId": result.get("userId"),
        "expectedStart": exp_start.strftime("%H:%M") if exp_start else None,
        "expectedEnd": exp_end.strftime("%H:%M") if exp_end else None,
        "actualStart": actual_start.strftime("%H:%M") if actual_start else None,
        "actualEnd": actual_end.strftime("%H:%M") if actual_end else None,
        "startStatus": result.get("startStatus"),
        "endStatus": result.get("endStatus"),
    }

    if not records:
        if actual_start is None and actual_end is None and result.get("startStatus") in (3, None):
            return True, "no records, expected no card", detail
        return False, "no records but has result", detail

    start_ok = (exp_start is None and actual_start is None) or (
        exp_start and actual_start and exp_start.strftime("%H:%M") == actual_start.strftime("%H:%M")
    )
    end_ok = (exp_end is None and actual_end is None) or (
        exp_end and actual_end and exp_end.strftime("%H:%M") == actual_end.strftime("%H:%M")
    )
    ok = start_ok and end_ok
    msg = "ok" if ok else f"start_ok={start_ok} end_ok={end_ok}"
    return ok, msg, detail


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--date", default=datetime.date.today().strftime("%Y-%m-%d"))
    parser.add_argument("--samples", type=int, default=5, help="抽样数量，0=全部")
    parser.add_argument("--output", default="", help="输出 JSON 报告路径")
    parser.add_argument("--timeout", type=int, default=DEFAULT_TIMEOUT)
    args = parser.parse_args()

    token = login(timeout=args.timeout)
    results = list_results(token, args.date, args.timeout)
    if not results:
        print("[WARN] no attendance results")
        return

    if args.samples > 0:
        results = random.sample(results, min(args.samples, len(results)))

    ok_count = 0
    mismatch_count = 0
    skipped = 0
    details = []

    for r in results:
        uid = r.get("userId")
        records = list_records_by_user(token, uid, args.date, args.timeout)
        ok, msg, detail = verify_one(r, records, args.date)
        detail["ok"] = ok
        detail["msg"] = msg
        details.append(detail)
        if "skip" in msg.lower():
            skipped += 1
        elif ok:
            ok_count += 1
            print(f"[OK] {r.get('userName')} {msg}")
        else:
            mismatch_count += 1
            print(f"[MISMATCH] {r.get('userName')} {msg} {detail}")

    summary = {"date": args.date, "samples": len(results), "ok": ok_count, "mismatches": mismatch_count, "skipped": skipped}
    print(f"[SUMMARY] {json.dumps(summary, ensure_ascii=False)}")

    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            json.dump({"summary": summary, "details": details}, f, ensure_ascii=False, indent=2)


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"[ERROR] {e}")
        sys.exit(1)
