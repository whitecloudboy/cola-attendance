package com.cola.attendance.module.attendance.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AttendanceDslConfigDTO {
    private boolean enabled = true;
    private List<DslRuleDTO> startRules = new ArrayList<>();
    private List<DslRuleDTO> endRules = new ArrayList<>();

    @Data
    public static class DslRuleDTO {
        private String name;
        private String when;
        private Integer startStatus;
        private Integer endStatus;
        private boolean stop = true;
    }
}
