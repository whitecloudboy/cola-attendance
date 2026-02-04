package com.cola.attendance.module.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cola.attendance.module.schedule.dao.DutyShiftDao;
import com.cola.attendance.module.schedule.dto.DutyShiftDTO;
import com.cola.attendance.module.schedule.entity.DutyShiftEntity;
import com.cola.attendance.module.schedule.service.DutyShiftService;
import com.cola.attendance.module.system.service.SysDeptService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DutyShiftServiceImpl extends ServiceImpl<DutyShiftDao, DutyShiftEntity> implements DutyShiftService {

    private final SysDeptService sysDeptService;

    public DutyShiftServiceImpl(SysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    @Override
    public List<DutyShiftDTO> listDto(Long deptId) {
        LambdaQueryWrapper<DutyShiftEntity> q = new LambdaQueryWrapper<>();
        if (deptId != null) q.eq(DutyShiftEntity::getDeptId, deptId);
        q.orderByAsc(DutyShiftEntity::getSort).orderByAsc(DutyShiftEntity::getId);
        return list(q).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<DutyShiftEntity> listByDeptIdAndColor(Long deptId, String color) {
        LambdaQueryWrapper<DutyShiftEntity> q = new LambdaQueryWrapper<>();
        q.eq(DutyShiftEntity::getDeptId, deptId).eq(DutyShiftEntity::getColor, color);
        q.orderByAsc(DutyShiftEntity::getSort).orderByAsc(DutyShiftEntity::getId);
        return list(q);
    }

    @Override
    public Page<DutyShiftDTO> pageDto(Page<DutyShiftEntity> page, String name, Long deptId) {
        LambdaQueryWrapper<DutyShiftEntity> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) q.like(DutyShiftEntity::getName, name);
        if (deptId != null) q.eq(DutyShiftEntity::getDeptId, deptId);
        q.orderByAsc(DutyShiftEntity::getSort).orderByAsc(DutyShiftEntity::getId);
        Page<DutyShiftEntity> entityPage = this.page(page, q);
        List<DutyShiftDTO> list = entityPage.getRecords().stream().map(this::toDto).collect(Collectors.toList());
        Page<DutyShiftDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(list);
        return dtoPage;
    }

    private DutyShiftDTO toDto(DutyShiftEntity e) {
        DutyShiftDTO dto = new DutyShiftDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setCode(e.getCode());
        dto.setDeptId(e.getDeptId());
        if (e.getDeptId() != null) {
            var dept = sysDeptService.getById(e.getDeptId());
            if (dept != null) dto.setDeptName(dept.getName());
        }
        dto.setSort(e.getSort());
        dto.setGroupNo(e.getGroupNo());
        dto.setColor(e.getColor());
        dto.setStartTime(e.getStartTime());
        dto.setEndTime(e.getEndTime());
        dto.setIsCrossDay(e.getIsCrossDay());
        dto.setShiftType(e.getShiftType());
        dto.setDescription(e.getDescription());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
