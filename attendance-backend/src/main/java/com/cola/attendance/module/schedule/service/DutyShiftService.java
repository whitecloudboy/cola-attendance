package com.cola.attendance.module.schedule.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cola.attendance.module.schedule.dto.DutyShiftDTO;
import com.cola.attendance.module.schedule.entity.DutyShiftEntity;

import java.util.List;

public interface DutyShiftService extends IService<DutyShiftEntity> {

    List<DutyShiftDTO> listDto(Long deptId);

    /** 按部门与颜色查询班次，供规则引擎交接班等使用 */
    List<DutyShiftEntity> listByDeptIdAndColor(Long deptId, String color);

    /** 按部门与班次组号查询班次，供交接班按分组号匹配下一班 */
    List<DutyShiftEntity> listByDeptIdAndGroupNo(Long deptId, String groupNo);

    Page<DutyShiftDTO> pageDto(Page<DutyShiftEntity> page, String name, Long deptId);
}
