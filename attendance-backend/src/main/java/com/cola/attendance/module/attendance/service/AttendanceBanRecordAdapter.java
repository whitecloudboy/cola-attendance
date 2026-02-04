package com.cola.attendance.module.attendance.service;

import com.cola.attendance.module.attendance.dto.AttendanceBanRecordDTO;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;

import java.util.List;

/**
 * 规则引擎用排班考勤适配：更新考勤结果、按班次+工作日查记录等。
 * 与旧架构 AttendanceBanRecordService 对齐，便于 DSL 复用。
 */
public interface AttendanceBanRecordAdapter {

    void update(AttendanceBanRecordDTO record);

    void saveAttendanceRecord(AttendanceRecordDTO record);

    /** 本周迟到次数（startStatus=2） */
    int countLateInWeek(Long personId, String attendanceDate);

    /** 按排班 id + 工作日查考勤记录列表 */
    List<AttendanceBanRecordDTO> getListByBanIdAndWorkDay(Long banId, String workDay);
}
