package com.cola.attendance.module.attendance.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 考勤结果 DTO，供查询与导出使用。
 */
@Data
public class AttendanceResultDTO {
    private Long id;
    private LocalDate attendanceDate;
    private Long userId;
    private String userName;
    private Long deptId;
    private String deptName;
    private Long scheduleId;
    private Long shiftId;
    private String shiftType;

    private LocalTime plannedStartTime;
    private LocalTime checkInTime;
    private Integer startStatus;

    private LocalTime plannedEndTime;
    private LocalTime checkOutTime;
    private Integer endStatus;

    private Integer absentStatus;
    private String remark;
}
