package com.cola.attendance.module.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("attendance_device")
public class AttendanceDeviceEntity {
    private Long id;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private Integer port;
    private String location;
    private Long deptId;
    private Integer isAttendance;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    @TableLogic
    private Integer deleted;
}
