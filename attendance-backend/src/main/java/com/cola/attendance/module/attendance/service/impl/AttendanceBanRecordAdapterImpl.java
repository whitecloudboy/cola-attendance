package com.cola.attendance.module.attendance.service.impl;

import com.cola.attendance.module.attendance.dto.AttendanceBanRecordDTO;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
import com.cola.attendance.module.attendance.service.AttendanceBanRecordAdapter;
import com.cola.attendance.module.attendance.service.AttendanceRecordService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 排班考勤适配实现：考勤结果表未就绪时仅落打卡记录，其余为占位。
 * 后续接入 attendance_result 后在此实现 update/countLateInWeek/getListByBanIdAndWorkDay。
 */
@Service
@RequiredArgsConstructor
public class AttendanceBanRecordAdapterImpl implements AttendanceBanRecordAdapter {

    private static final Logger log = LoggerFactory.getLogger(AttendanceBanRecordAdapterImpl.class);

    private final AttendanceRecordService attendanceRecordService;

    @Override
    public void update(AttendanceBanRecordDTO record) {
        if (record == null) return;
        log.debug("[rule-engine] BanRecord update placeholder: id={} personId={} date={}",
                record.getId(), record.getPersonId(), record.getAttendanceDate());
        // TODO: 写入 attendance_result
    }

    @Override
    public void saveAttendanceRecord(AttendanceRecordDTO record) {
        if (record == null) return;
        // 若 DTO 仅部分字段，需转换为 Entity 再 save；当前 AttendanceRecordService 无 save(DTO)，仅可扩展
        log.debug("[rule-engine] saveAttendanceRecord placeholder");
        // TODO: 如需在此落库，可调用 attendanceRecordService 的保存方法
    }

    @Override
    public int countLateInWeek(Long personId, String attendanceDate) {
        if (personId == null || attendanceDate == null) return 0;
        // TODO: 从 attendance_result 统计本周 startStatus=2
        return 0;
    }

    @Override
    public List<AttendanceBanRecordDTO> getListByBanIdAndWorkDay(Long banId, String workDay) {
        if (banId == null || workDay == null) return Collections.emptyList();
        // TODO: 从 attendance_result + duty_schedule 组装
        return Collections.emptyList();
    }
}
