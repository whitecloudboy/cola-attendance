package com.cola.attendance.rule;

import com.cola.attendance.module.schedule.entity.DutyScheduleEntity;
import com.cola.attendance.module.schedule.entity.DutyShiftEntity;
import com.cola.attendance.module.schedule.service.DutyScheduleService;
import com.cola.attendance.module.schedule.service.DutyShiftService;
import com.cola.attendance.rule.dto.RuleShiftDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则引擎用班次/排班门面，与旧架构 DutyBanService 对齐便于 DSL 复用。
 */
@Component
@RequiredArgsConstructor
public class RuleDutyFacade {

    private final DutyScheduleService dutyScheduleService;
    private final DutyShiftService dutyShiftService;

    /** 按排班记录 id 查班次（setBanId = schedule.id） */
    public RuleShiftDTO getBanByScheduleId(Long setBanId) {
        if (setBanId == null) return null;
        DutyScheduleEntity schedule = dutyScheduleService.getById(setBanId);
        if (schedule == null || schedule.getShiftId() == null) return null;
        DutyShiftEntity shift = dutyShiftService.getById(schedule.getShiftId());
        if (shift == null) return null;
        return toRuleShift(shift);
    }

    /** 按部门 + 颜色查同组班次列表 */
    public List<RuleShiftDTO> getListByDeptIdAndColor(Long deptId, String banColor) {
        if (deptId == null || banColor == null) return Collections.emptyList();
        List<DutyShiftEntity> list = dutyShiftService.listByDeptIdAndColor(deptId, banColor);
        return list.stream().map(this::toRuleShift).collect(Collectors.toList());
    }

    private RuleShiftDTO toRuleShift(DutyShiftEntity e) {
        RuleShiftDTO dto = new RuleShiftDTO();
        dto.setId(e.getId());
        dto.setBeginTime(e.getStartTime());
        dto.setEndTime(e.getEndTime());
        dto.setBanColor(e.getColor());
        dto.setDeptId(e.getDeptId());
        return dto;
    }
}
