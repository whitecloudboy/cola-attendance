package com.cola.attendance.rule;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 灰度/并行开关配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "attendance.rule-engine")
public class AttendanceRuleEngineProperties {

    private boolean enabled = false;
    private boolean parallel = true;
    private boolean legacyDisabled = false;
    private boolean handoverEnabled = true;
    private boolean emitAllEndEvents = false;
    private List<Long> autoNormalPersonIds = Collections.emptyList();
    private List<Long> autoNormalDeptIds = Collections.emptyList();
    private boolean autoNormalIncludeSubDept = false;
}
