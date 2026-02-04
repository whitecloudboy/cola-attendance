package com.cola.attendance.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 执行日志适配器，当前写入应用日志，后续可接入 DB/ES。
 */
@Component
public class RuleExecutionLogger {

    private static final Logger log = LoggerFactory.getLogger(RuleExecutionLogger.class);

    public void log(RuleExecutionLog record) {
        if (record == null) return;
        log.info("[rule-engine] branch={} tenant={} event={} evaluator={} rule={} version={} outcome={} snapshot={}",
                record.getBranchCode(), record.getTenantId(), record.getEventType(), record.getEvaluator(),
                record.getRuleCode(), record.getRuleVersion(), record.getOutcomeSummary(), record.getSnapshot());
    }
}
