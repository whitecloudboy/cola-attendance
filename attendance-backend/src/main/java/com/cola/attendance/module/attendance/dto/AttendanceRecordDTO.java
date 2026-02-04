package com.cola.attendance.module.attendance.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttendanceRecordDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long deptId;
    private String deptName;
    private LocalDateTime eventTime;
    /** 规则引擎用：打卡时间字符串 yyyy-MM-dd HH:mm:ss，与 eventTime 一致 */
    public String getOpenTime() {
        return eventTime != null ? eventTime.toString().replace("T", " ").substring(0, 19) : null;
    }
    private Long deviceId;
    private String deviceName;
    private String eventType;
    private String remark;
    private LocalDateTime createdAt;
}
