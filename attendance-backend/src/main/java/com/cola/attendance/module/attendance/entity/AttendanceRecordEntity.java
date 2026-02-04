package com.cola.attendance.module.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("attendance_record")
public class AttendanceRecordEntity {
    private Long id;
    private Long userId;
    private String userName;
    private Long deptId;
    private LocalDateTime eventTime;
    private Long deviceId;
    private String eventType;
    private String temperature;
    private Integer maskStatus;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    @TableLogic
    private Integer deleted;
}
