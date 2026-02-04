package com.cola.attendance.module.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("attendance_rule_param")
public class AttendanceRuleParamEntity {
    private Long id;
    private String code;
    private String name;
    private String value;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    @TableLogic
    private Integer deleted;
}
