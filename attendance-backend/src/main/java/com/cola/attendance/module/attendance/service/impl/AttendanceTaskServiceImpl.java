package com.cola.attendance.module.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cola.attendance.module.attendance.dao.AttendanceRecordDao;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
import com.cola.attendance.module.attendance.entity.AttendanceRecordEntity;
import com.cola.attendance.module.attendance.entity.AttendanceResultEntity;
import com.cola.attendance.module.attendance.service.AttendanceResultService;
import com.cola.attendance.module.attendance.service.AttendanceTaskService;
import com.cola.attendance.rule.AttendanceRuleEngineService;
import com.cola.attendance.rule.RuleEvent;
import com.cola.attendance.rule.RuleOutcome;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceTaskServiceImpl implements AttendanceTaskService {

    private final AttendanceResultService attendanceResultService;
    private final AttendanceRecordDao attendanceRecordDao;
    private final AttendanceRuleEngineService ruleEngineService;

    public AttendanceTaskServiceImpl(AttendanceResultService attendanceResultService,
                                    AttendanceRecordDao attendanceRecordDao,
                                    AttendanceRuleEngineService ruleEngineService) {
        this.attendanceResultService = attendanceResultService;
        this.attendanceRecordDao = attendanceRecordDao;
        this.ruleEngineService = ruleEngineService;
    }

    @Override
    public int runEndOfDaySupplement(LocalDate date) {
        if (date == null) return 0;
        List<AttendanceResultEntity> results = attendanceResultService.lambdaQuery()
                .eq(AttendanceResultEntity::getAttendanceDate, date)
                .list();
        int processed = 0;
        for (AttendanceResultEntity result : results) {
            var banRecord = attendanceResultService.toBanRecordDTO(result);
            var records = attendanceRecordDao.selectList(
                    new LambdaQueryWrapper<AttendanceRecordEntity>()
                            .eq(AttendanceRecordEntity::getUserId, result.getUserId())
                            .ge(AttendanceRecordEntity::getEventTime, date.atStartOfDay())
                            .le(AttendanceRecordEntity::getEventTime, date.plusDays(1).atStartOfDay().minusNanos(1))
                            .orderByAsc(AttendanceRecordEntity::getEventTime));
            List<AttendanceRecordDTO> dtos = records.stream().map(this::toRecordDto).collect(Collectors.toList());

            RuleEvent event = RuleEvent.builder()
                    .eventType("end-check")
                    .attendanceDate(date)
                    .banRecord(banRecord)
                    .attendanceRecords(dtos)
                    .occurredAt(java.time.LocalDateTime.now())
                    .build();
            RuleOutcome outcome = ruleEngineService.process(event);
            if (outcome != null) {
                attendanceResultService.updateFromOutcome(banRecord, outcome);
                processed++;
            }
        }
        return processed;
    }

    private AttendanceRecordDTO toRecordDto(AttendanceRecordEntity e) {
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
