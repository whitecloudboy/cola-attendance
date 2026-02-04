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

    /** 按部门 + 颜色查同组班次列表（兼容未设 groupNo 时按颜色匹配） */
    public List<RuleShiftDTO> getListByDeptIdAndColor(Long deptId, String banColor) {
        if (deptId == null || banColor == null) return Collections.emptyList();
        List<DutyShiftEntity> list = dutyShiftService.listByDeptIdAndColor(deptId, banColor);
        return list.stream().map(this::toRuleShift).collect(Collectors.toList());
    }

    /** 按部门 + 班次组号查同组班次列表，用于交接班按分组号匹配下一班 */
    public List<RuleShiftDTO> getListByDeptIdAndGroupNo(Long deptId, String groupNo) {
        if (deptId == null || groupNo == null || groupNo.isBlank()) return Collections.emptyList();
        List<DutyShiftEntity> list = dutyShiftService.listByDeptIdAndGroupNo(deptId, groupNo.trim());
        return list.stream().map(this::toRuleShift).collect(Collectors.toList());
    }

    private RuleShiftDTO toRuleShift(DutyShiftEntity e) {
        RuleShiftDTO dto = new RuleShiftDTO();
        dto.setId(e.getId());
        dto.setBeginTime(e.getStartTime());
        dto.setEndTime(e.getEndTime());
        dto.setBanColor(e.getColor());
        dto.setDeptId(e.getDeptId());
        dto.setGroupNo(e.getGroupNo());
        return dto;
    }
}
