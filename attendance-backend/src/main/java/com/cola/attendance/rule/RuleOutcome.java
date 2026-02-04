package com.cola.attendance.rule;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule evaluation result.
 */
@Data
@Builder
public class RuleOutcome {

    private Integer startStatus;
    private Integer endStatus;
    private Integer banStatus;
    private LocalTime checkStartTime;
    private LocalTime checkEndTime;
    private String ruleVersion;
    @Builder.Default
    private boolean stop = false;
    @Builder.Default
    private List<RuleAction> actions = new ArrayList<>();

    public void addAction(RuleAction action) {
        if (actions == null) actions = new ArrayList<>();
        actions.add(action);
    }
}
