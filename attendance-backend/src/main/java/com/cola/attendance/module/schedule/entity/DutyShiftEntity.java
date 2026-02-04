package com.cola.attendance.module.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("duty_shift")
public class DutyShiftEntity {
    private Long id;
    private String name;
    private String code;
    private Long deptId;
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
    private Long createdBy;
    private Long updatedBy;
    @TableLogic
    private Integer deleted;
}
