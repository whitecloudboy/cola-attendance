package com.cola.attendance.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cola.attendance.module.system.dao.SysUserDao;
import com.cola.attendance.module.system.dao.SysUserPostDao;
import com.cola.attendance.module.system.dao.SysUserRoleDao;
import com.cola.attendance.module.system.dto.SysUserDTO;
import com.cola.attendance.module.system.entity.SysUserEntity;
import com.cola.attendance.module.system.entity.SysUserPostEntity;
import com.cola.attendance.module.system.entity.SysUserRoleEntity;
import com.cola.attendance.module.system.service.SysDeptService;
import com.cola.attendance.module.system.service.SysUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserDao, SysUserEntity> implements SysUserService {

    private final SysUserRoleDao sysUserRoleDao;
    private final SysUserPostDao sysUserPostDao;
    private final SysDeptService sysDeptService;
    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(SysUserRoleDao sysUserRoleDao, SysUserPostDao sysUserPostDao, SysDeptService sysDeptService, PasswordEncoder passwordEncoder) {
        this.sysUserRoleDao = sysUserRoleDao;
        this.sysUserPostDao = sysUserPostDao;
        this.sysDeptService = sysDeptService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<SysUserDTO> pageDto(Page<SysUserEntity> page, String username, Long deptId) {
        LambdaQueryWrapper<SysUserEntity> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) q.like(SysUserEntity::getUsername, username);
        if (deptId != null) q.eq(SysUserEntity::getDeptId, deptId);
        q.orderByDesc(SysUserEntity::getId);
        Page<SysUserEntity> entityPage = this.page(page, q);
        List<SysUserDTO> list = entityPage.getRecords().stream().map(this::toDto).collect(Collectors.toList());
        Page<SysUserDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(list);
        return dtoPage;
    }

    @Override
    @Transactional
    public void saveUser(SysUserDTO dto) {
        SysUserEntity e = new SysUserEntity();
        e.setUsername(dto.getUsername());
        e.setDisplayName(dto.getDisplayName());
        e.setPasswordHash(passwordEncoder.encode(StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "123456"));
        e.setDeptId(dto.getDeptId());
        e.setEmail(dto.getEmail());
        e.setPhone(dto.getPhone());
        e.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        e.setIsDutyPerson(dto.getIsDutyPerson() != null ? dto.getIsDutyPerson() : 0);
        save(e);
        if (dto.getRoleIds() != null) {
            for (Long roleId : dto.getRoleIds()) {
                SysUserRoleEntity ur = new SysUserRoleEntity();
                ur.setUserId(e.getId());
                ur.setRoleId(roleId);
                sysUserRoleDao.insert(ur);
            }
        }
        if (dto.getPostIds() != null) {
            for (Long postId : dto.getPostIds()) {
                SysUserPostEntity up = new SysUserPostEntity();
                up.setUserId(e.getId());
                up.setPostId(postId);
                sysUserPostDao.insert(up);
            }
        }
    }

    @Override
    @Transactional
    public void updateUser(SysUserDTO dto) {
        if (dto.getId() == null) return;
        SysUserEntity e = getById(dto.getId());
        if (e == null) return;
        e.setDisplayName(dto.getDisplayName());
        e.setDeptId(dto.getDeptId());
        e.setEmail(dto.getEmail());
        e.setPhone(dto.getPhone());
        e.setStatus(dto.getStatus());
        e.setIsDutyPerson(dto.getIsDutyPerson());
        updateById(e);
        sysUserRoleDao.delete(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, e.getId()));
        if (dto.getRoleIds() != null) {
            for (Long roleId : dto.getRoleIds()) {
                SysUserRoleEntity ur = new SysUserRoleEntity();
                ur.setUserId(e.getId());
                ur.setRoleId(roleId);
                sysUserRoleDao.insert(ur);
            }
        }
        sysUserPostDao.delete(new LambdaQueryWrapper<SysUserPostEntity>().eq(SysUserPostEntity::getUserId, e.getId()));
        if (dto.getPostIds() != null) {
            for (Long postId : dto.getPostIds()) {
                SysUserPostEntity up = new SysUserPostEntity();
                up.setUserId(e.getId());
                up.setPostId(postId);
                sysUserPostDao.insert(up);
            }
        }
    }

    @Override
    public SysUserDTO getByIdDto(Long id) {
        SysUserEntity e = getById(id);
        return e != null ? toDto(e) : null;
    }

    private SysUserDTO toDto(SysUserEntity e) {
        SysUserDTO dto = new SysUserDTO();
        dto.setId(e.getId());
        dto.setUsername(e.getUsername());
        dto.setDisplayName(e.getDisplayName());
        dto.setDeptId(e.getDeptId());
        if (e.getDeptId() != null) {
            var dept = sysDeptService.getById(e.getDeptId());
            if (dept != null) dto.setDeptName(dept.getName());
        }
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setStatus(e.getStatus());
        dto.setIsDutyPerson(e.getIsDutyPerson());
        dto.setLastLoginAt(e.getLastLoginAt());
        dto.setCreatedAt(e.getCreatedAt());
        var roleList = sysUserRoleDao.selectList(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, e.getId()));
        dto.setRoleIds(roleList.stream().map(SysUserRoleEntity::getRoleId).collect(Collectors.toList()));
        var postList = sysUserPostDao.selectList(new LambdaQueryWrapper<SysUserPostEntity>().eq(SysUserPostEntity::getUserId, e.getId()));
        dto.setPostIds(postList.stream().map(SysUserPostEntity::getPostId).collect(Collectors.toList()));
        return dto;
    }
}
