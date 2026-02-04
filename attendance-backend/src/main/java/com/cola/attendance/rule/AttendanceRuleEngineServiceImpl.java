package com.cola.attendance.rule;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Rule engine core service（复用旧架构逻辑）。
 */
@Service
@RequiredArgsConstructor
public class AttendanceRuleEngineServiceImpl implements AttendanceRuleEngineService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceRuleEngineServiceImpl.class);

    private final AttendanceRuleEngineProperties properties;
    private final RuleConfigurationService configurationService;
    private final List<IEventEvaluator> evaluators;
    private final RuleExecutionLogger executionLogger;
    private final RuleOutcomeHandler outcomeHandler;

    @Override
    public RuleOutcome process(RuleEvent event) {
        if (!properties.isEnabled()) {
            log.debug("[rule-engine] disabled, skip event {}", event == null ? "null" : event.getEventType());
            return RuleOutcome.builder().build();
        }
        if (event == null) {
            return RuleOutcome.builder().build();
        }
        Map<String, Map<String, Object>> params =
                configurationService.getRuleParams(event.getBranchCode(), event.getTenantId());
        RuleContext context = RuleContext.builder()
                .event(event)
                .banRecord(event.getBanRecord())
                .ruleParams(params)
                .build();
        RuleOutcome outcome = RuleOutcome.builder().build();

        evaluators.stream()
                .sorted(Comparator.comparingInt(IEventEvaluator::getOrder))
                .forEach(evaluator -> {
                    if (!outcome.isStop()) {
                        safeEvaluate(evaluator, context, outcome);
                    }
                });

        if (properties.isParallel()) {
            executionLogger.log(RuleExecutionLog.builder()
                    .branchCode(event.getBranchCode())
                    .tenantId(event.getTenantId())
                    .eventType(event.getEventType())
                    .ruleVersion(outcome.getRuleVersion())
                    .snapshot(buildSnapshot(event))
                    .outcomeSummary(outcomeSummary(outcome))
                    .executedAt(event.getOccurredAt())
                    .build());
        } else {
            outcomeHandler.handle(context, outcome, properties);
        }
        return outcome;
    }

    private void safeEvaluate(IEventEvaluator evaluator, RuleContext context, RuleOutcome outcome) {
        try {
            evaluator.evaluate(context, outcome);
        } catch (Exception ex) {
            log.error("[rule-engine] evaluator {} failed", evaluator.getClass().getSimpleName(), ex);
        }
    }

    private String outcomeSummary(RuleOutcome outcome) {
        if (outcome == null) return "null";
        return String.format("start=%s,end=%s,ban=%s",
                outcome.getStartStatus(), outcome.getEndStatus(), outcome.getBanStatus());
    }

    private Map<String, Object> buildSnapshot(RuleEvent event) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        if (event == null) return snapshot;
        snapshot.put("attendanceDate", event.getAttendanceDate());
        if (event.getBanRecord() != null) {
            snapshot.put("personId", event.getBanRecord().getPersonId());
            snapshot.put("personName", event.getBanRecord().getRealName());
            snapshot.put("setBanId", event.getBanRecord().getSetBanId());
            snapshot.put("banType", event.getBanRecord().getBanType());
            snapshot.put("startTime", event.getBanRecord().getStartTime());
            snapshot.put("endTime", event.getBanRecord().getEndTime());
            snapshot.put("checkStartTime", event.getBanRecord().getCheckStartTime());
            snapshot.put("checkEndTime", event.getBanRecord().getCheckEndTime());
        }
        return snapshot;
    }
}
