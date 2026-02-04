package com.cola.attendance.rule.dsl;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * DSL rule configuration（与旧架构一致，从 YAML 加载）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "attendance.rule-engine.dsl")
public class AttendanceDslProperties {

    private boolean enabled = false;
    private boolean versioningEnabled = false;
    private boolean grayEnabled = false;
    private List<DslRule> startRules = Collections.emptyList();
    private List<DslRule> endRules = Collections.emptyList();

    @Data
    public static class DslRule {
        private String name;
        private String when;
        private Integer startStatus;
        private Integer endStatus;
        private boolean stop = true;
    }
}
