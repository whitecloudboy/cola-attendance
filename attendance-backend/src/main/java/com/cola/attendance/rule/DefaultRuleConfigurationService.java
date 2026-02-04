package com.cola.attendance.rule;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * 默认规则配置：DSL 从 YAML 加载；参数可后续从 attendance_rule_param 表读取。
 */
@Service
public class DefaultRuleConfigurationService implements RuleConfigurationService {

    @Override
    public Map<String, Map<String, Object>> getRuleParams(String branchCode, String tenantId) {
        return Collections.emptyMap();
    }
}
