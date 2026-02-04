package com.cola.attendance.module.schedule.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DutyScheduleDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long deptId;
    private String deptName;
    private LocalDate workDate;
    private Long shiftId;
    private String shiftName;
    private String shiftType;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
