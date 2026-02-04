package com.cola.attendance.module.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cola.attendance.module.attendance.dao.AttendanceResultDao;
import com.cola.attendance.module.attendance.dto.AttendanceBanRecordDTO;
import com.cola.attendance.module.attendance.dto.AttendanceResultDTO;
import com.cola.attendance.module.attendance.entity.AttendanceResultEntity;
import com.cola.attendance.module.attendance.service.AttendanceResultService;
import com.cola.attendance.module.schedule.entity.DutyScheduleEntity;
import com.cola.attendance.module.schedule.entity.DutyShiftEntity;
import com.cola.attendance.module.schedule.service.DutyScheduleService;
import com.cola.attendance.module.schedule.service.DutyShiftService;
import com.cola.attendance.module.system.entity.SysDeptEntity;
import com.cola.attendance.module.system.entity.SysUserEntity;
import com.cola.attendance.module.system.dao.SysUserDao;
import com.cola.attendance.module.system.entity.SysUserEntity;
import com.cola.attendance.module.system.service.SysDeptService;
import com.cola.attendance.module.system.service.SysUserService;
import com.cola.attendance.rule.RuleOutcome;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考勤结果服务实现。按 attendance-optimal-design：规则引擎统一入口，输出落 attendance_result。
 */
@Service
public class AttendanceResultServiceImpl extends ServiceImpl<AttendanceResultDao, AttendanceResultEntity>
        implements AttendanceResultService {

    private final DutyScheduleService dutyScheduleService;
    private final DutyShiftService dutyShiftService;
    private final SysUserService sysUserService;
    private final SysDeptService sysDeptService;
    private final SysUserDao sysUserDao;

    public AttendanceResultServiceImpl(DutyScheduleService dutyScheduleService, DutyShiftService dutyShiftService,
                                      SysUserService sysUserService, SysDeptService sysDeptService, SysUserDao sysUserDao) {
        this.dutyScheduleService = dutyScheduleService;
        this.dutyShiftService = dutyShiftService;
        this.sysUserService = sysUserService;
        this.sysDeptService = sysDeptService;
        this.sysUserDao = sysUserDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateEmptyForDate(LocalDate date) {
        if (date == null) return 0;
        var schedules = dutyScheduleService.lambdaQuery()
                .eq(DutyScheduleEntity::getWorkDate, date)
                .eq(DutyScheduleEntity::getStatus, 1)
                .list();
        int count = 0;
        for (DutyScheduleEntity s : schedules) {
            if (getByUserDateShift(s.getUserId(), date, s.getShiftId()) != null) continue;
            AttendanceResultEntity e = new AttendanceResultEntity();
            e.setAttendanceDate(date);
            e.setUserId(s.getUserId());
            SysUserEntity user = sysUserService.getById(s.getUserId());
            e.setUserName(user != null ? user.getDisplayName() : null);
            e.setDeptId(s.getDeptId());
            if (s.getDeptId() != null) {
                SysDeptEntity dept = sysDeptService.getById(s.getDeptId());
                e.setDeptName(dept != null ? dept.getName() : null);
            }
            e.setScheduleId(s.getId());
            e.setShiftId(s.getShiftId());
            e.setShiftType(s.getShiftType());
            DutyShiftEntity shift = dutyShiftService.getById(s.getShiftId());
            if (shift != null) {
                e.setPlannedStartTime(shift.getStartTime());
                e.setPlannedEndTime(shift.getEndTime());
            }
            e.setStartStatus(null);
            e.setEndStatus(null);
            e.setAbsentStatus(0);
            save(e);
            count++;
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFromOutcome(AttendanceBanRecordDTO banRecord, RuleOutcome outcome) {
        if (banRecord == null || banRecord.getId() == null) return;
        AttendanceResultEntity e = getById(banRecord.getId());
        if (e == null) return;
        boolean changed = false;
        if (outcome.getStartStatus() != null) { e.setStartStatus(outcome.getStartStatus()); changed = true; }
        if (outcome.getEndStatus() != null) { e.setEndStatus(outcome.getEndStatus()); changed = true; }
        if (outcome.getCheckStartTime() != null) { e.setCheckInTime(outcome.getCheckStartTime()); changed = true; }
        if (outcome.getCheckEndTime() != null) { e.setCheckOutTime(outcome.getCheckEndTime()); changed = true; }
        if (outcome.getBanStatus() != null) { e.setAbsentStatus(outcome.getBanStatus()); changed = true; }
        if (changed) updateById(e);
    }

    @Override
    public AttendanceResultEntity getByUserDateShift(Long userId, LocalDate date, Long shiftId) {
        LambdaQueryWrapper<AttendanceResultEntity> q = new LambdaQueryWrapper<>();
        q.eq(AttendanceResultEntity::getUserId, userId).eq(AttendanceResultEntity::getAttendanceDate, date);
        if (shiftId != null) q.eq(AttendanceResultEntity::getShiftId, shiftId);
        else q.isNull(AttendanceResultEntity::getShiftId);
        return getOne(q);
    }

    @Override
    public AttendanceBanRecordDTO toBanRecordDTO(AttendanceResultEntity entity) {
        if (entity == null) return null;
        AttendanceBanRecordDTO dto = new AttendanceBanRecordDTO();
        dto.setId(entity.getId());
        dto.setAttendanceDate(entity.getAttendanceDate() != null ? entity.getAttendanceDate().toString() : null);
        dto.setSetBanId(entity.getScheduleId());
        dto.setPersonId(entity.getUserId());
        dto.setRealName(entity.getUserName());
        dto.setDeptId(entity.getDeptId());
        dto.setDeptName(entity.getDeptName());
        dto.setStartTime(entity.getPlannedStartTime());
        dto.setCheckStartTime(entity.getCheckInTime());
        dto.setStartStatus(entity.getStartStatus());
        dto.setEndTime(entity.getPlannedEndTime());
        dto.setCheckEndTime(entity.getCheckOutTime());
        dto.setEndStatus(entity.getEndStatus());
        dto.setBanStatus(entity.getAbsentStatus());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    @Override
    public Page<AttendanceResultDTO> pageDto(Page<AttendanceResultEntity> page, Long userId, Long deptId,
                                             String startDate, String endDate, String userKeyword) {
        LambdaQueryWrapper<AttendanceResultEntity> q = new LambdaQueryWrapper<>();
        if (userId != null) {
            q.eq(AttendanceResultEntity::getUserId, userId);
        } else if (StringUtils.hasText(userKeyword)) {
            List<Long> userIds = sysUserDao.selectList(
                    new LambdaQueryWrapper<SysUserEntity>()
                            .like(SysUserEntity::getUsername, userKeyword)
                            .or().like(SysUserEntity::getDisplayName, userKeyword))
                    .stream().map(SysUserEntity::getId).toList();
            if (userIds.isEmpty()) q.eq(AttendanceResultEntity::getUserId, -1L);
            else q.in(AttendanceResultEntity::getUserId, userIds);
        }
        if (deptId != null) q.eq(AttendanceResultEntity::getDeptId, deptId);
        if (StringUtils.hasText(startDate)) q.ge(AttendanceResultEntity::getAttendanceDate, LocalDate.parse(startDate));
        if (StringUtils.hasText(endDate)) q.le(AttendanceResultEntity::getAttendanceDate, LocalDate.parse(endDate));
        q.orderByDesc(AttendanceResultEntity::getAttendanceDate).orderByAsc(AttendanceResultEntity::getUserId);
        Page<AttendanceResultEntity> entityPage = this.page(page, q);
        List<AttendanceResultDTO> list = entityPage.getRecords().stream().map(this::toDto).collect(Collectors.toList());
        Page<AttendanceResultDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(list);
        return dtoPage;
    }

    @Override
    public List<AttendanceResultDTO> listByDateRange(LocalDate startDate, LocalDate endDate, Long deptId) {
        LambdaQueryWrapper<AttendanceResultEntity> q = new LambdaQueryWrapper<>();
        q.ge(AttendanceResultEntity::getAttendanceDate, startDate).le(AttendanceResultEntity::getAttendanceDate, endDate);
        if (deptId != null) q.eq(AttendanceResultEntity::getDeptId, deptId);
        q.orderByAsc(AttendanceResultEntity::getAttendanceDate).orderByAsc(AttendanceResultEntity::getUserId);
        return list(q).stream().map(this::toDto).collect(Collectors.toList());
    }

    private AttendanceResultDTO toDto(AttendanceResultEntity e) {
        AttendanceResultDTO dto = new AttendanceResultDTO();
        dto.setId(e.getId());
        dto.setAttendanceDate(e.getAttendanceDate());
        dto.setUserId(e.getUserId());
        dto.setUserName(e.getUserName());
        dto.setDeptId(e.getDeptId());
        dto.setDeptName(e.getDeptName());
        dto.setScheduleId(e.getScheduleId());
        dto.setShiftId(e.getShiftId());
        dto.setShiftType(e.getShiftType());
        dto.setPlannedStartTime(e.getPlannedStartTime());
        dto.setCheckInTime(e.getCheckInTime());
        dto.setStartStatus(e.getStartStatus());
        dto.setPlannedEndTime(e.getPlannedEndTime());
        dto.setCheckOutTime(e.getCheckOutTime());
        dto.setEndStatus(e.getEndStatus());
        dto.setAbsentStatus(e.getAbsentStatus());
        dto.setRemark(e.getRemark());
        return dto;
    }
}
