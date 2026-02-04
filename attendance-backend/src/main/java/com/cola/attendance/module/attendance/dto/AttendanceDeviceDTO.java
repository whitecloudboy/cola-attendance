package com.cola.attendance.module.attendance.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttendanceDeviceDTO {
    private Long id;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private Integer port;
    private String location;
    private Long deptId;
    private String deptName;
    private Integer isAttendance;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
