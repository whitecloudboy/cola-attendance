package com.cola.attendance.rule;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 规则执行日志记录结构（暂存内存或写日志）。
 */
@Data
@Builder
public class RuleExecutionLog {

    private String branchCode;
    private String tenantId;
    private String eventType;
    private String evaluator;
    private String ruleCode;
    private String ruleVersion;
    private Map<String, Object> snapshot;
    private LocalDateTime executedAt;
    private String outcomeSummary;
}
