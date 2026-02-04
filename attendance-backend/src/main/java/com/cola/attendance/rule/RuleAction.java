package com.cola.attendance.rule;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 描述规则产生的动作，例如发送提醒或记录审计。
 */
@Data
@Builder
public class RuleAction {

    private String type;
    private Map<String, Object> payload;
}
