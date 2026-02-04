#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""通用 HTTP 客户端，供 E2E 脚本使用。"""
import json
import subprocess
import urllib.parse
from typing import Any, Optional

if hasattr(__import__("sys").stdout, "reconfigure"):
    __import__("sys").stdout.reconfigure(encoding="utf-8")


def build_url(url: str, params: Optional[dict] = None) -> str:
    if not params:
        return url
    query = urllib.parse.urlencode(params, doseq=True)
    sep = "&" if "?" in url else "?"
    return f"{url}{sep}{query}"


def curl_json(
    method: str,
    url: str,
    params: Optional[dict] = None,
    json_body: Optional[dict] = None,
    headers: Optional[dict] = None,
    timeout: int = 20,
) -> tuple[int, dict[str, Any]]:
    """执行 HTTP 请求，返回 (http_status, parsed_json_or_error_dict)。"""
    final_url = build_url(url, params)
    cmd = ["curl", "-s", "-m", str(timeout), "-X", method.upper(), final_url]
    if headers:
        for k, v in headers.items():
            cmd.extend(["-H", f"{k}: {v}"])
    if json_body is not None:
        cmd.extend(["-H", "Content-Type: application/json"])
        cmd.extend(["--data", json.dumps(json_body, ensure_ascii=False)])
    cmd.extend(["-w", "\nHTTPSTATUS:%{http_code}"])
    try:
        proc = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
            timeout=timeout + 5,
        )
    except subprocess.TimeoutExpired as exc:
        return 0, {"error": "curl timeout", "text": str(exc)}
    stdout = proc.stdout or ""
    stderr = proc.stderr or ""
    if proc.returncode != 0:
        return 0, {"error": stderr.strip(), "text": stdout.strip()}
    output = stdout.strip()
    http_status = 0
    body = output
    if "HTTPSTATUS:" in output:
        body, status_text = output.rsplit("HTTPSTATUS:", 1)
        body = body.strip()
        try:
            http_status = int(status_text.strip())
        except ValueError:
            http_status = 0
    if not body:
        return http_status, {}
    try:
        return http_status, json.loads(body)
    except json.JSONDecodeError:
        return http_status, {"text": body}
