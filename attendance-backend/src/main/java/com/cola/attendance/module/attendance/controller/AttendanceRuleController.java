package com.cola.attendance.module.attendance.controller;

import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.attendance.dto.AttendanceDslConfigDTO;
import com.cola.attendance.module.attendance.service.AttendanceDslConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "考勤规则")
@RestController
@RequestMapping("/attendance/rule")
public class AttendanceRuleController {

    private final AttendanceDslConfigService dslConfigService;

    public AttendanceRuleController(AttendanceDslConfigService dslConfigService) {
        this.dslConfigService = dslConfigService;
    }

    @Operation(summary = "获取 DSL 规则配置（DB 优先，无则取 YAML 默认）")
    @GetMapping("/dsl")
    public Result<AttendanceDslConfigDTO> getDsl() {
        return Result.ok(dslConfigService.getEffectiveConfig());
    }

    @Operation(summary = "保存 DSL 规则配置")
    @PutMapping("/dsl")
    public Result<Void> saveDsl(@RequestBody AttendanceDslConfigDTO config) {
        dslConfigService.save(config);
        return Result.ok(null);
    }
}
