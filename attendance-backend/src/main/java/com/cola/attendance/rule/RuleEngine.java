package com.cola.attendance.rule;

/**
 * 规则引擎统一入口，所有考勤判定仅走此入口。
 * 业务侧使用 RuleEngineFacade.publishEvent(event) 或 process(event)。
 */
public interface RuleEngine {

    void publishEvent(RuleEvent event);

    RuleOutcome process(RuleEvent event);
}
