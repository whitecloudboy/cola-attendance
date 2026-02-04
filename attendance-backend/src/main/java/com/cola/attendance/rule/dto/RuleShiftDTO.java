package com.cola.attendance.rule.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * 规则引擎用班次视图，与旧架构 DutyBanDTO 字段对齐（beginTime/endTime/banColor）。
 */
@Data
public class RuleShiftDTO {

    private Long id;
    private LocalTime beginTime;
    private LocalTime endTime;
    private String banColor;
    private Long deptId;
    /** 班次组号，用于交接班匹配同一组内的下一班 */
    private String groupNo;
}
