package com.cola.attendance.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cola.attendance.module.system.dto.SysMenuDTO;
import com.cola.attendance.module.system.entity.SysMenuEntity;

import java.util.List;

public interface SysMenuService extends IService<SysMenuEntity> {
    List<SysMenuDTO> tree();
}
