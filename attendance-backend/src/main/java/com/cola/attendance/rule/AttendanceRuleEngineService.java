package com.cola.attendance.rule;

/**
 * 规则引擎核心服务。
 */
public interface AttendanceRuleEngineService {

    RuleOutcome process(RuleEvent event);
}
