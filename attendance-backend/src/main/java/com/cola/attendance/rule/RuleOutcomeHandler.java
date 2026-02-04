package com.cola.attendance.rule;

import com.cola.attendance.module.attendance.dto.AttendanceBanRecordDTO;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
import com.cola.attendance.module.attendance.service.AttendanceBanRecordAdapter;
import com.cola.attendance.module.attendance.service.AttendanceResultService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 规则结果落库：仅在 parallel=false 时执行。
 * 按 attendance-optimal-design 统一入口，输出落 attendance_result。
 */
@Component
@RequiredArgsConstructor
public class RuleOutcomeHandler {

    private static final Logger log = LoggerFactory.getLogger(RuleOutcomeHandler.class);

    private final AttendanceResultService attendanceResultService;
    private final AttendanceBanRecordAdapter banRecordAdapter;

    public void handle(RuleContext context, RuleOutcome outcome, AttendanceRuleEngineProperties props) {
        if (props.isParallel()) return;
        if (context == null || context.getBanRecord() == null || outcome == null) return;
        AttendanceBanRecordDTO record = context.getBanRecord();
        if (outcome.getStartStatus() != null) record.setStartStatus(outcome.getStartStatus());
        if (outcome.getEndStatus() != null) record.setEndStatus(outcome.getEndStatus());
        if (outcome.getBanStatus() != null) record.setBanStatus(outcome.getBanStatus());
        if (outcome.getCheckStartTime() != null) record.setCheckStartTime(outcome.getCheckStartTime());
        if (outcome.getCheckEndTime() != null) record.setCheckEndTime(outcome.getCheckEndTime());
        attendanceResultService.updateFromOutcome(record, outcome);

        for (RuleAction action : outcome.getActions()) {
            if ("record-open".equals(action.getType()) && action.getPayload() != null) {
                Object rec = action.getPayload().get("record");
                if (rec instanceof AttendanceRecordDTO dto) {
                    banRecordAdapter.saveAttendanceRecord(dto);
                }
            }
        }
    }
}
