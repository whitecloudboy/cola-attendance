package com.cola.attendance.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cola.attendance.module.system.dto.SysDeptDTO;
import com.cola.attendance.module.system.entity.SysDeptEntity;

import java.util.List;

public interface SysDeptService extends IService<SysDeptEntity> {
    List<SysDeptDTO> tree();

    /**
     * 获取某部门下所有子部门 ID（含自身），供规则引擎 DSL inDept 等使用。
     */
    List<Long> getSubDeptIdList(Long deptId);
}
