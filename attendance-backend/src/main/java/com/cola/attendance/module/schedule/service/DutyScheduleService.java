package com.cola.attendance.module.schedule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cola.attendance.module.schedule.dto.DutyScheduleDTO;
import com.cola.attendance.module.schedule.entity.DutyScheduleEntity;

import java.time.LocalDate;
import java.util.List;

public interface DutyScheduleService extends IService<DutyScheduleEntity> {

    /**
     * 手工排班：为指定日期+班次设置人员（同一日期同一班次仅保留一人）
     */
    void setSchedule(LocalDate workDate, Long shiftId, Long userId);

    /**
     * 按日期范围查询排班（可选部门）
     */
    List<DutyScheduleDTO> listByDateRange(LocalDate startDate, LocalDate endDate, Long deptId);

    /**
     * 删除排班
     */
    void removeSchedule(Long id);
}
