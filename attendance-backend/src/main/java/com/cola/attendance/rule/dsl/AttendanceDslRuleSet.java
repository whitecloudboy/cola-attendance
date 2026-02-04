package com.cola.attendance.rule.dsl;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * DSL rule set loaded from JSON content（版本化时使用）.
 */
@Data
public class AttendanceDslRuleSet {

    private Long versionId;
    private String versionNo;
    private List<AttendanceDslProperties.DslRule> startRules = Collections.emptyList();
    private List<AttendanceDslProperties.DslRule> endRules = Collections.emptyList();
}
