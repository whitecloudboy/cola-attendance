package com.cola.attendance.rule.dsl;

import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
import com.cola.attendance.module.attendance.service.AttendanceBanRecordAdapter;
import com.cola.attendance.rule.IEventEvaluator;
import com.cola.attendance.rule.RuleDutyFacade;
import com.cola.attendance.rule.RuleContext;
import com.cola.attendance.rule.RuleOutcome;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * DSL rule evaluator for start/end decisions（复用旧架构逻辑）.
 */
@Component
@Order(10)
@RequiredArgsConstructor
public class DslRuleEvaluator implements IEventEvaluator {

    private static final Logger log = LoggerFactory.getLogger(DslRuleEvaluator.class);
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AttendanceDslProperties dslProperties;
    private final AttendanceBanRecordAdapter banRecordService;
    private final RuleDutyFacade dutyBanService;
    private final com.cola.attendance.module.system.service.SysDeptService sysDeptService;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void evaluate(RuleContext context, RuleOutcome outcome) {
        if (context == null || context.getEvent() == null || !dslProperties.isEnabled()) return;
        String eventType = context.getEvent().getEventType();
        if (!StringUtils.hasText(eventType)) return;
        // 初始版本无版本服务，仅用配置文件规则
        AttendanceDslRuleSet ruleSet = null;
        if (ruleSet != null && outcome != null && StringUtils.hasText(ruleSet.getVersionNo())) {
            outcome.setRuleVersion(ruleSet.getVersionNo());
        }
        if (eventType.contains("start") || "punch".equals(eventType) || "end-check".equals(eventType)) {
            List<AttendanceDslProperties.DslRule> rules = ruleSet != null ? ruleSet.getStartRules() : dslProperties.getStartRules();
            applyRules(context, outcome, rules, true);
        }
        if (eventType.contains("end") || "punch".equals(eventType) || "end-check".equals(eventType)) {
            outcome.setStop(false);
            List<AttendanceDslProperties.DslRule> rules = ruleSet != null ? ruleSet.getEndRules() : dslProperties.getEndRules();
            applyRules(context, outcome, rules, false);
        }
        // 无论规则是否匹配，有打卡记录时始终填充真实打卡时间（确保 checkInTime/checkOutTime 不为空）
        List<AttendanceRecordDTO> records = context.getEvent() != null ? context.getEvent().getAttendanceRecords() : null;
        if (records != null && !records.isEmpty()) {
            if (outcome.getCheckStartTime() == null) {
                LocalDateTime ci = resolveCheckIn(context);
                if (ci != null) outcome.setCheckStartTime(ci.toLocalTime());
            }
            if (outcome.getCheckEndTime() == null) {
                LocalDateTime co = resolveCheckOut(context);
                if (co != null) outcome.setCheckEndTime(co.toLocalTime());
            }
        }
    }

    private void applyRules(RuleContext context, RuleOutcome outcome,
                            List<AttendanceDslProperties.DslRule> rules, boolean isStart) {
        if (rules == null || rules.isEmpty()) return;
        for (AttendanceDslProperties.DslRule rule : rules) {
            if (!StringUtils.hasText(rule.getWhen())) continue;
            try {
                StandardEvaluationContext evalCtx = buildEvalContext(context);
                Expression expression = parser.parseExpression(rule.getWhen());
                Boolean matched = expression.getValue(evalCtx, Boolean.class);
                if (Boolean.TRUE.equals(matched)) {
                    if (isStart && outcome.getStartStatus() == null && rule.getStartStatus() != null) {
                        outcome.setStartStatus(rule.getStartStatus());
                        LocalDateTime checkTime = resolveCheckIn(context);
                        if (checkTime != null) outcome.setCheckStartTime(checkTime.toLocalTime());
                    }
                    if (!isStart && outcome.getEndStatus() == null && rule.getEndStatus() != null) {
                        outcome.setEndStatus(rule.getEndStatus());
                        LocalDateTime checkTime = resolveCheckOut(context);
                        if (checkTime != null) outcome.setCheckEndTime(checkTime.toLocalTime());
                    }
                    if (rule.isStop()) {
                        outcome.setStop(true);
                        return;
                    }
                }
            } catch (Exception ex) {
                log.warn("[rule-engine][dsl] evaluate failed, rule={}", rule.getName(), ex);
            }
        }
    }

    private StandardEvaluationContext buildEvalContext(RuleContext context) {
        StandardEvaluationContext evalCtx = new StandardEvaluationContext(
                new DslFunctionHelper(context, banRecordService, dutyBanService, sysDeptService)
        );
        evalCtx.setVariable("event", context.getEvent());
        evalCtx.setVariable("ban", context.getBanRecord());
        evalCtx.setVariable("records", context.getEvent() != null ? context.getEvent().getAttendanceRecords() : null);
        evalCtx.setVariable("params", context.getRuleParams());
        evalCtx.setVariable("occurredAt", context.getEvent() != null ? context.getEvent().getOccurredAt() : null);
        return evalCtx;
    }

    private LocalDateTime resolveCheckIn(RuleContext context) {
        if (context.getBanRecord() != null && context.getBanRecord().getCheckStartTime() != null && context.getBanRecord().getAttendanceDate() != null) {
            LocalDate day = LocalDate.parse(context.getBanRecord().getAttendanceDate());
            return context.getBanRecord().getCheckStartTime().atDate(day);
        }
        List<AttendanceRecordDTO> records = context.getEvent() != null ? context.getEvent().getAttendanceRecords() : null;
        if (records == null || records.isEmpty()) return null;
        LocalDate attendanceDate = context.getBanRecord() != null && context.getBanRecord().getAttendanceDate() != null ? LocalDate.parse(context.getBanRecord().getAttendanceDate()) : (context.getEvent() != null ? context.getEvent().getAttendanceDate() : null);
        LocalTime startTime = context.getBanRecord() != null ? context.getBanRecord().getStartTime() : null;
        if (attendanceDate == null || startTime == null) {
            return records.stream().filter(r -> r.getOpenTime() != null).map(r -> parse(r.getOpenTime())).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
        }
        LocalDateTime base = attendanceDate.atTime(startTime);
        return records.stream().filter(r -> r.getOpenTime() != null).map(r -> parse(r.getOpenTime())).filter(Objects::nonNull).min(Comparator.comparingLong(dt -> Math.abs(Duration.between(base, dt).toMinutes()))).orElse(null);
    }

    private LocalDateTime resolveCheckOut(RuleContext context) {
        List<AttendanceRecordDTO> records = context.getEvent() != null ? context.getEvent().getAttendanceRecords() : null;
        if (records == null || records.isEmpty()) return null;
        LocalDate attendanceDate = context.getBanRecord() != null && context.getBanRecord().getAttendanceDate() != null ? LocalDate.parse(context.getBanRecord().getAttendanceDate()) : (context.getEvent() != null ? context.getEvent().getAttendanceDate() : null);
        LocalTime endTime = context.getBanRecord() != null ? context.getBanRecord().getEndTime() : null;
        if (attendanceDate == null || endTime == null) {
            return records.stream().filter(r -> r.getOpenTime() != null).map(r -> parse(r.getOpenTime())).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        }
        boolean crossDay = context.getBanRecord() != null && context.getBanRecord().getStartTime() != null && context.getBanRecord().getStartTime().isAfter(endTime);
        LocalDateTime base = attendanceDate.atTime(endTime);
        if (crossDay) base = base.plusDays(1);
        LocalTime startTime = context.getBanRecord() != null ? context.getBanRecord().getStartTime() : null;
        LocalDateTime startBase = startTime != null ? attendanceDate.atTime(startTime) : null;
        LocalDateTime mid = (startBase != null) ? startBase.plusMinutes(Duration.between(startBase, base).toMinutes() / 2) : null;
        final LocalDateTime baseFinal = base;
        return records.stream().filter(r -> r.getOpenTime() != null).map(r -> parse(r.getOpenTime())).filter(Objects::nonNull).filter(dt -> mid == null || !dt.isBefore(mid)).min(Comparator.comparingLong(dt -> Math.abs(Duration.between(baseFinal, dt).toMinutes()))).orElse(null);
    }

    private LocalDateTime parse(String openTime) {
        try {
            return LocalDateTime.parse(openTime, DT_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }
}
