#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""E2E 脚本配置。"""
import os

# 服务地址，可通过环境变量 QIANYI_BASE_URL 覆盖
BASE_URL = os.environ.get("QIANYI_BASE_URL", "http://localhost:8080").rstrip("/")

# 默认登录账号（需在 init-admin.sql 中已创建）
DEFAULT_LOGIN = {
    "username": "admin",
    "password": "123456",
}

# HTTP 超时（秒）
DEFAULT_TIMEOUT = 20
