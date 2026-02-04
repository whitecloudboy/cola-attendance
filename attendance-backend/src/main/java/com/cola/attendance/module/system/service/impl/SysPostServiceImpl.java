package com.cola.attendance.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cola.attendance.module.system.dao.SysPostDao;
import com.cola.attendance.module.system.dto.SysPostDTO;
import com.cola.attendance.module.system.entity.SysPostEntity;
import com.cola.attendance.module.system.service.SysDeptService;
import com.cola.attendance.module.system.service.SysPostService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysPostServiceImpl extends ServiceImpl<SysPostDao, SysPostEntity> implements SysPostService {

    private final SysDeptService sysDeptService;

    public SysPostServiceImpl(SysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    @Override
    public List<SysPostDTO> listDto(Long deptId) {
        LambdaQueryWrapper<SysPostEntity> q = new LambdaQueryWrapper<>();
        if (deptId != null) q.eq(SysPostEntity::getDeptId, deptId);
        q.orderByAsc(SysPostEntity::getSort).orderByAsc(SysPostEntity::getId);
        return list(q).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<SysPostDTO> pageDto(Page<SysPostEntity> page, String name, Long deptId) {
        LambdaQueryWrapper<SysPostEntity> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) q.like(SysPostEntity::getName, name);
        if (deptId != null) q.eq(SysPostEntity::getDeptId, deptId);
        q.orderByAsc(SysPostEntity::getSort).orderByAsc(SysPostEntity::getId);
        Page<SysPostEntity> entityPage = this.page(page, q);
        List<SysPostDTO> list = entityPage.getRecords().stream().map(this::toDto).collect(Collectors.toList());
        Page<SysPostDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(list);
        return dtoPage;
    }

    private SysPostDTO toDto(SysPostEntity e) {
        SysPostDTO dto = new SysPostDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setCode(e.getCode());
        dto.setDeptId(e.getDeptId());
        if (e.getDeptId() != null) {
            var dept = sysDeptService.getById(e.getDeptId());
            if (dept != null) dto.setDeptName(dept.getName());
        }
        dto.setSort(e.getSort());
        dto.setStatus(e.getStatus());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
