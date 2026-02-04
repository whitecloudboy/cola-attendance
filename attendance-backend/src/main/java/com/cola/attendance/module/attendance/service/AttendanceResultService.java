package com.cola.attendance.module.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cola.attendance.module.attendance.dto.AttendanceBanRecordDTO;
import com.cola.attendance.module.attendance.dto.AttendanceResultDTO;
import com.cola.attendance.module.attendance.entity.AttendanceResultEntity;
import com.cola.attendance.rule.RuleOutcome;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤结果服务。按 attendance-optimal-design 统一入口：规则引擎输出落 attendance_result。
 */
public interface AttendanceResultService extends IService<AttendanceResultEntity> {

    /**
     * 按 duty_schedule 生成当日空考勤记录（定时任务调用）。
     * @return 生成条数
     */
    int generateEmptyForDate(LocalDate date);

    /**
     * 按规则引擎输出更新考勤结果。
     */
    void updateFromOutcome(AttendanceBanRecordDTO banRecord, RuleOutcome outcome);

    /**
     * 按用户+日期+班次查找一条（shiftId 可为 null）。
     */
    AttendanceResultEntity getByUserDateShift(Long userId, LocalDate date, Long shiftId);

    /**
     * 将 AttendanceResultEntity 转为规则引擎用的 AttendanceBanRecordDTO。
     */
    AttendanceBanRecordDTO toBanRecordDTO(AttendanceResultEntity entity);

    Page<AttendanceResultDTO> pageDto(Page<AttendanceResultEntity> page,
                                     Long userId, Long deptId, String startDate, String endDate, String userKeyword);

    List<AttendanceResultDTO> listByDateRange(LocalDate startDate, LocalDate endDate, Long deptId);
}
