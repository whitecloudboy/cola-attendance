package com.cola.attendance.module.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("duty_schedule")
public class DutyScheduleEntity {
    private Long id;
    private Long userId;
    private Long deptId;
    private LocalDate workDate;
    private Long shiftId;
    private String shiftType;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    @TableLogic
    private Integer deleted;
}
