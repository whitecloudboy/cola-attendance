package com.cola.attendance.module.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cola.attendance.module.attendance.dao.AttendanceRuleParamDao;
import com.cola.attendance.module.attendance.dto.AttendanceDslConfigDTO;
import com.cola.attendance.module.attendance.entity.AttendanceRuleParamEntity;
import com.cola.attendance.module.attendance.service.AttendanceDslConfigService;
import com.cola.attendance.rule.dsl.AttendanceDslProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceDslConfigServiceImpl implements AttendanceDslConfigService {

    private static final String DSL_CONFIG_CODE = "dsl_config";

    private final AttendanceRuleParamDao ruleParamDao;
    private final AttendanceDslProperties dslProperties;
    private final ObjectMapper objectMapper;

    public AttendanceDslConfigServiceImpl(AttendanceRuleParamDao ruleParamDao, AttendanceDslProperties dslProperties, ObjectMapper objectMapper) {
        this.ruleParamDao = ruleParamDao;
        this.dslProperties = dslProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public AttendanceDslConfigDTO getEffectiveConfig() {
        AttendanceRuleParamEntity row = ruleParamDao.selectOne(
                new LambdaQueryWrapper<AttendanceRuleParamEntity>().eq(AttendanceRuleParamEntity::getCode, DSL_CONFIG_CODE));
        if (row != null && row.getValue() != null && !row.getValue().isBlank()) {
            try {
                return objectMapper.readValue(row.getValue(), AttendanceDslConfigDTO.class);
            } catch (Exception ignored) {
            }
        }
        return fromProperties(dslProperties);
    }

    @Override
    public void save(AttendanceDslConfigDTO config) {
        if (config == null) return;
        try {
            String json = objectMapper.writeValueAsString(config);
            AttendanceRuleParamEntity row = ruleParamDao.selectOne(
                    new LambdaQueryWrapper<AttendanceRuleParamEntity>().eq(AttendanceRuleParamEntity::getCode, DSL_CONFIG_CODE));
            if (row != null) {
                row.setValue(json);
                row.setName("DSL规则配置");
                ruleParamDao.updateById(row);
            } else {
                row = new AttendanceRuleParamEntity();
                row.setCode(DSL_CONFIG_CODE);
                row.setName("DSL规则配置");
                row.setValue(json);
                row.setDescription("考勤规则引擎 DSL 配置，JSON 格式");
                ruleParamDao.insert(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("保存 DSL 配置失败: " + e.getMessage(), e);
        }
    }

    private AttendanceDslConfigDTO fromProperties(AttendanceDslProperties p) {
        AttendanceDslConfigDTO dto = new AttendanceDslConfigDTO();
        dto.setEnabled(p.isEnabled());
        dto.setStartRules(p.getStartRules() == null ? Collections.emptyList() : p.getStartRules().stream().map(this::toRuleDto).collect(Collectors.toList()));
        dto.setEndRules(p.getEndRules() == null ? Collections.emptyList() : p.getEndRules().stream().map(this::toRuleDto).collect(Collectors.toList()));
        return dto;
    }

    private AttendanceDslConfigDTO.DslRuleDTO toRuleDto(AttendanceDslProperties.DslRule r) {
        AttendanceDslConfigDTO.DslRuleDTO d = new AttendanceDslConfigDTO.DslRuleDTO();
        d.setName(r.getName());
        d.setWhen(r.getWhen());
        d.setStartStatus(r.getStartStatus());
        d.setEndStatus(r.getEndStatus());
        d.setStop(r.isStop());
        return d;
    }
}
