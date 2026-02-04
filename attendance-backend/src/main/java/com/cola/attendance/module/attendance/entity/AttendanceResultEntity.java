package com.cola.attendance.module.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 考勤结果实体，对应 attendance_result 表。
 * 按人+日期+班次聚合的上下班状态（attendance-optimal-design 中的 AttendanceBanRecord）。
 */
@Data
@TableName("attendance_result")
public class AttendanceResultEntity {
    @TableId
    private Long id;
    private LocalDate attendanceDate;
    private Long userId;
    private String userName;
    private Long deptId;
    private String deptName;
    private Long scheduleId;
    private Long shiftId;
    private String shiftType;

    private LocalTime plannedStartTime;
    private LocalTime checkInTime;
    private Integer startStatus;

    private LocalTime plannedEndTime;
    private LocalTime checkOutTime;
    private Integer endStatus;

    private Integer absentStatus;
    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    @TableLogic
    private Integer deleted;
}
