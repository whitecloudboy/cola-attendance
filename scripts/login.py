#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""登录获取 JWT token。"""
import time
from typing import Optional

from config import BASE_URL, DEFAULT_LOGIN, DEFAULT_TIMEOUT
from http_client import curl_json


def login(
    base_url: Optional[str] = None,
    username: Optional[str] = None,
    password: Optional[str] = None,
    timeout: int = DEFAULT_TIMEOUT,
    retries: int = 5,
    sleep_sec: float = 2.0,
) -> str:
    url = (base_url or BASE_URL) + "/system/auth/login"
    payload = {
        "username": username or DEFAULT_LOGIN["username"],
        "password": password or DEFAULT_LOGIN["password"],
    }
    last_err = None
    for _ in range(retries):
        status, data = curl_json("POST", url, json_body=payload, timeout=timeout)
        if status == 200 and data.get("code") == 0 and data.get("data"):
            return data["data"]["token"]
        last_err = (status, data)
        time.sleep(sleep_sec)
    raise RuntimeError(f"login failed: {last_err}")
