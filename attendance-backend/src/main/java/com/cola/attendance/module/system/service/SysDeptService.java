package com.cola.attendance.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cola.attendance.module.system.dto.SysDeptDTO;
import com.cola.attendance.module.system.entity.SysDeptEntity;

import java.util.List;

public interface SysDeptService extends IService<SysDeptEntity> {
    List<SysDeptDTO> tree();
}
