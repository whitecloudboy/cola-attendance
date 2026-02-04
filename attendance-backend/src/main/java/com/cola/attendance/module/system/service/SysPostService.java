package com.cola.attendance.module.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cola.attendance.module.system.dto.SysPostDTO;
import com.cola.attendance.module.system.entity.SysPostEntity;

import java.util.List;

public interface SysPostService extends IService<SysPostEntity> {

    List<SysPostDTO> listDto(Long deptId);

    Page<SysPostDTO> pageDto(Page<SysPostEntity> page, String name, Long deptId);
}
