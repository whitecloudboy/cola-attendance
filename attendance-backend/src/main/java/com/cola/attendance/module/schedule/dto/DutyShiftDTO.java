package com.cola.attendance.module.schedule.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class DutyShiftDTO {
    private Long id;
    private String name;
    private String code;
    private Long deptId;
    private String deptName;
    private Integer sort;
    private String groupNo;
    private String color;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer isCrossDay;
    private String shiftType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
