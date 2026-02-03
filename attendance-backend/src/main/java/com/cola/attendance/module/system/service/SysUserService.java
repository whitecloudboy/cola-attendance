package com.cola.attendance.module.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cola.attendance.module.system.dto.SysUserDTO;
import com.cola.attendance.module.system.entity.SysUserEntity;

public interface SysUserService extends IService<SysUserEntity> {
    Page<SysUserDTO> pageDto(Page<SysUserEntity> page, String username, Long deptId);
    SysUserDTO getByIdDto(Long id);
    void saveUser(SysUserDTO dto);
    void updateUser(SysUserDTO dto);
}
