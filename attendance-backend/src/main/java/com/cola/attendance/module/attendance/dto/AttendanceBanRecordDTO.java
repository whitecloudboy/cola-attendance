package com.cola.attendance.module.attendance.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * 排班考勤记录 DTO，供规则引擎使用（与旧架构字段对齐便于 DSL 复用）。
 */
@Data
public class AttendanceBanRecordDTO {

    private Long id;
    private String attendanceDate;
    private Long setBanId;
    private Long personId;
    private String realName;
    private Long deptId;
    private String deptName;

    private LocalTime startTime;
    private LocalTime checkStartTime;
    private Integer startStatus;

    private LocalTime endTime;
    private LocalTime checkEndTime;
    private Integer endStatus;
    private Integer banStatus;
    private String remark;
    private String banType;
}
