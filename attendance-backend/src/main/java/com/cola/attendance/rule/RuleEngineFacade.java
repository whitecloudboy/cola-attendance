package com.cola.attendance.rule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 规则引擎调用门面，统一检查开关，避免业务侧重复判断。
 */
@Component
@RequiredArgsConstructor
public class RuleEngineFacade implements RuleEngine {

    private final AttendanceRuleEngineProperties properties;
    private final AttendanceRuleEngineService ruleEngineService;

    @Override
    public void publishEvent(RuleEvent event) {
        if (!properties.isEnabled()) return;
        ruleEngineService.process(event);
    }

    @Override
    public RuleOutcome process(RuleEvent event) {
        if (!properties.isEnabled()) return RuleOutcome.builder().build();
        return ruleEngineService.process(event);
    }
}
