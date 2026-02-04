package com.cola.attendance.module.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cola.attendance.module.attendance.dao.AttendanceDeviceDao;
import com.cola.attendance.module.attendance.dto.AttendanceDeviceDTO;
import com.cola.attendance.module.attendance.entity.AttendanceDeviceEntity;
import com.cola.attendance.module.attendance.service.AttendanceDeviceService;
import com.cola.attendance.module.system.service.SysDeptService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceDeviceServiceImpl extends ServiceImpl<AttendanceDeviceDao, AttendanceDeviceEntity> implements AttendanceDeviceService {

    private final SysDeptService sysDeptService;

    public AttendanceDeviceServiceImpl(SysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    @Override
    public List<AttendanceDeviceDTO> listDto(Long deptId) {
        LambdaQueryWrapper<AttendanceDeviceEntity> q = new LambdaQueryWrapper<>();
        if (deptId != null) q.eq(AttendanceDeviceEntity::getDeptId, deptId);
        q.eq(AttendanceDeviceEntity::getStatus, 1).orderByAsc(AttendanceDeviceEntity::getId);
        return list(q).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<AttendanceDeviceDTO> pageDto(Page<AttendanceDeviceEntity> page, String deviceName, Long deptId) {
        LambdaQueryWrapper<AttendanceDeviceEntity> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(deviceName)) q.like(AttendanceDeviceEntity::getDeviceName, deviceName);
        if (deptId != null) q.eq(AttendanceDeviceEntity::getDeptId, deptId);
        q.orderByAsc(AttendanceDeviceEntity::getId);
        Page<AttendanceDeviceEntity> entityPage = this.page(page, q);
        List<AttendanceDeviceDTO> list = entityPage.getRecords().stream().map(this::toDto).collect(Collectors.toList());
        Page<AttendanceDeviceDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(list);
        return dtoPage;
    }

    private AttendanceDeviceDTO toDto(AttendanceDeviceEntity e) {
        AttendanceDeviceDTO dto = new AttendanceDeviceDTO();
        dto.setId(e.getId());
        dto.setDeviceCode(e.getDeviceCode());
        dto.setDeviceName(e.getDeviceName());
        dto.setDeviceType(e.getDeviceType());
        dto.setIpAddress(e.getIpAddress());
        dto.setPort(e.getPort());
        dto.setLocation(e.getLocation());
        dto.setDeptId(e.getDeptId());
        if (e.getDeptId() != null) {
            var dept = sysDeptService.getById(e.getDeptId());
            if (dept != null) dto.setDeptName(dept.getName());
        }
        dto.setIsAttendance(e.getIsAttendance());
        dto.setStatus(e.getStatus());
        dto.setRemark(e.getRemark());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
