package com.cola.attendance.module.attendance.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class AttendanceRecordDTO {
    private static final DateTimeFormatter OPEN_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;
    private Long userId;
    private String userName;
    private Long deptId;
    private String deptName;
    private LocalDateTime eventTime;
    /** 规则引擎用：打卡时间字符串 yyyy-MM-dd HH:mm:ss，与 eventTime 一致 */
    public String getOpenTime() {
        return eventTime != null ? eventTime.format(OPEN_TIME_FMT) : null;
    }
    private Long deviceId;
    private String deviceName;
    private String eventType;
    private String remark;
    private LocalDateTime createdAt;
}
