package com.cola.attendance.module.attendance.service;

import com.cola.attendance.module.attendance.dto.AttendanceDslConfigDTO;

/**
 * DSL 规则配置服务：从 DB 读写，供规则引擎与界面使用。
 */
public interface AttendanceDslConfigService {

    AttendanceDslConfigDTO getEffectiveConfig();

    void save(AttendanceDslConfigDTO config);
}
