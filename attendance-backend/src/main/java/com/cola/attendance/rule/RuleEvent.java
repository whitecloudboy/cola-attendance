package com.cola.attendance.rule;

import com.cola.attendance.module.attendance.dto.AttendanceBanRecordDTO;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统一的规则入口事件模型，封装任务、设备回调等触发源的输入。
 */
@Data
@Builder
public class RuleEvent {

    private String eventType;
    private String branchCode;
    private String tenantId;
    private LocalDate attendanceDate;
    private AttendanceBanRecordDTO banRecord;
    private List<AttendanceRecordDTO> attendanceRecords;
    private Map<String, Object> deviceInfo;
    private Map<String, Object> extra;
    private LocalDateTime occurredAt;
}
