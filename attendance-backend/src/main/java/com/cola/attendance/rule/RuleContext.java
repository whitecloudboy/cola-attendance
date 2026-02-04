package com.cola.attendance.rule;

import com.cola.attendance.module.attendance.dto.AttendanceBanRecordDTO;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 规则执行上下文，包含事件与预加载的规则配置。
 */
@Data
@Builder
public class RuleContext {

    private RuleEvent event;
    private Map<String, Map<String, Object>> ruleParams;
    private AttendanceBanRecordDTO banRecord;
}
