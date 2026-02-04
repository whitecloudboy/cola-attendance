package com.cola.attendance.rule;

import java.util.Map;

/**
 * 规则配置服务，从数据库或缓存获取参数。
 */
public interface RuleConfigurationService {

    Map<String, Map<String, Object>> getRuleParams(String branchCode, String tenantId);
}
