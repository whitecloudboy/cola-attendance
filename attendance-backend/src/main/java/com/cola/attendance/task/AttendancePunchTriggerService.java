package com.cola.attendance.task;

import com.cola.attendance.module.attendance.dao.AttendanceRecordDao;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
import com.cola.attendance.module.attendance.entity.AttendanceRecordEntity;
import com.cola.attendance.module.attendance.service.AttendanceResultService;
import com.cola.attendance.rule.AttendanceRuleEngineService;
import com.cola.attendance.rule.RuleEvent;
import com.cola.attendance.rule.RuleOutcome;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 打卡事件触发规则引擎。按 attendance-optimal-design：统一入口，打卡写入后触发 RuleEngine。
 */
@Service
@RequiredArgsConstructor
public class AttendancePunchTriggerService {

    private static final Logger log = LoggerFactory.getLogger(AttendancePunchTriggerService.class);

    private final AttendanceResultService attendanceResultService;
    private final AttendanceRecordDao attendanceRecordDao;
    private final AttendanceRuleEngineService ruleEngineService;

    /**
     * 打卡记录写入后调用，触发规则引擎更新考勤结果。
     * 按用户+日期查找 attendance_result，加载当日打卡记录，构建事件并执行。
     */
    public void onPunchSaved(Long userId, LocalDate attendanceDate) {
        if (userId == null || attendanceDate == null) return;
        List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRecordEntity>()
                        .eq(AttendanceRecordEntity::getUserId, userId)
                        .ge(AttendanceRecordEntity::getEventTime, attendanceDate.atStartOfDay())
                        .le(AttendanceRecordEntity::getEventTime, attendanceDate.plusDays(1).atStartOfDay().minusNanos(1))
                        .orderByAsc(AttendanceRecordEntity::getEventTime));
        List<AttendanceRecordDTO> recordDtos = records.stream().map(this::toDto).collect(Collectors.toList());

        var results = attendanceResultService.lambdaQuery()
                .eq(com.cola.attendance.module.attendance.entity.AttendanceResultEntity::getUserId, userId)
                .eq(com.cola.attendance.module.attendance.entity.AttendanceResultEntity::getAttendanceDate, attendanceDate)
                .list();

        for (var result : results) {
            var banRecord = attendanceResultService.toBanRecordDTO(result);
            RuleEvent event = RuleEvent.builder()
                    .eventType("punch")
                    .attendanceDate(attendanceDate)
                    .banRecord(banRecord)
                    .attendanceRecords(recordDtos)
                    .occurredAt(java.time.LocalDateTime.now())
                    .build();
            RuleOutcome outcome = ruleEngineService.process(event);
            if (outcome != null) {
                attendanceResultService.updateFromOutcome(banRecord, outcome);
            }
        }
    }

    private AttendanceRecordDTO toDto(AttendanceRecordEntity e) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(e.getId());
        dto.setUserId(e.getUserId());
        dto.setUserName(e.getUserName());
        dto.setDeptId(e.getDeptId());
        dto.setEventTime(e.getEventTime());
        dto.setDeviceId(e.getDeviceId());
        dto.setEventType(e.getEventType());
        dto.setRemark(e.getRemark());
        return dto;
    }
}
