package com.cola.attendance.module.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cola.attendance.module.schedule.dao.DutyScheduleDao;
import com.cola.attendance.module.schedule.dto.DutyScheduleDTO;
import com.cola.attendance.module.schedule.entity.DutyScheduleEntity;
import com.cola.attendance.module.schedule.service.DutyScheduleService;
import com.cola.attendance.module.schedule.service.DutyShiftService;
import com.cola.attendance.module.system.entity.SysUserEntity;
import com.cola.attendance.module.system.service.SysDeptService;
import com.cola.attendance.module.system.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DutyScheduleServiceImpl extends ServiceImpl<DutyScheduleDao, DutyScheduleEntity> implements DutyScheduleService {

    private final DutyShiftService dutyShiftService;
    private final SysUserService sysUserService;
    private final SysDeptService sysDeptService;

    public DutyScheduleServiceImpl(DutyShiftService dutyShiftService, SysUserService sysUserService, SysDeptService sysDeptService) {
        this.dutyShiftService = dutyShiftService;
        this.sysUserService = sysUserService;
        this.sysDeptService = sysDeptService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setSchedule(LocalDate workDate, Long shiftId, Long userId) {
        if (workDate == null || shiftId == null || userId == null) {
            throw new IllegalArgumentException("workDate, shiftId, userId 不能为空");
        }
        var shift = dutyShiftService.getById(shiftId);
        if (shift == null) throw new IllegalArgumentException("班次不存在");
        var user = sysUserService.getById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");

        LambdaQueryWrapper<DutyScheduleEntity> q = new LambdaQueryWrapper<>();
        q.eq(DutyScheduleEntity::getWorkDate, workDate).eq(DutyScheduleEntity::getShiftId, shiftId);
        remove(q);

        DutyScheduleEntity e = new DutyScheduleEntity();
        e.setUserId(userId);
        e.setDeptId(user.getDeptId());
        e.setWorkDate(workDate);
        e.setShiftId(shiftId);
        e.setShiftType(shift.getShiftType());
        e.setStatus(1);
        save(e);
    }

    @Override
    public List<DutyScheduleDTO> listByDateRange(LocalDate startDate, LocalDate endDate, Long deptId) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate, endDate 不能为空");
        }
        LambdaQueryWrapper<DutyScheduleEntity> q = new LambdaQueryWrapper<>();
        q.ge(DutyScheduleEntity::getWorkDate, startDate).le(DutyScheduleEntity::getWorkDate, endDate);
        if (deptId != null) q.eq(DutyScheduleEntity::getDeptId, deptId);
        q.orderByAsc(DutyScheduleEntity::getWorkDate).orderByAsc(DutyScheduleEntity::getShiftId);
        return list(q).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void removeSchedule(Long id) {
        removeById(id);
    }

    private DutyScheduleDTO toDto(DutyScheduleEntity e) {
        DutyScheduleDTO dto = new DutyScheduleDTO();
        dto.setId(e.getId());
        dto.setUserId(e.getUserId());
        SysUserEntity user = sysUserService.getById(e.getUserId());
        dto.setUserName(user != null ? user.getDisplayName() : null);
        dto.setDeptId(e.getDeptId());
        if (e.getDeptId() != null) {
            var dept = sysDeptService.getById(e.getDeptId());
            dto.setDeptName(dept != null ? dept.getName() : null);
        }
        dto.setWorkDate(e.getWorkDate());
        dto.setShiftId(e.getShiftId());
        var shift = dutyShiftService.getById(e.getShiftId());
        dto.setShiftName(shift != null ? shift.getName() : null);
        dto.setShiftType(e.getShiftType());
        dto.setStatus(e.getStatus());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
