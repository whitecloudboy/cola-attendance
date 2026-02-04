package com.cola.attendance.module.schedule.controller;

import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.schedule.dto.DutyScheduleDTO;
import com.cola.attendance.module.schedule.service.DutyScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "排班")
@RestController
@RequestMapping("/schedule/schedule")
public class DutyScheduleController {

    private final DutyScheduleService dutyScheduleService;

    public DutyScheduleController(DutyScheduleService dutyScheduleService) {
        this.dutyScheduleService = dutyScheduleService;
    }

    @Operation(summary = "手工排班：设置某日某班次的人员")
    @PostMapping("/set")
    public Result<Void> setSchedule(@RequestBody Map<String, Object> body) {
        LocalDate workDate = body.get("workDate") != null ? LocalDate.parse(body.get("workDate").toString()) : null;
        Long shiftId = body.get("shiftId") != null ? Long.valueOf(body.get("shiftId").toString()) : null;
        Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
        dutyScheduleService.setSchedule(workDate, shiftId, userId);
        return Result.ok(null);
    }

    @Operation(summary = "按日期范围查询排班")
    @GetMapping("/list")
    public Result<List<DutyScheduleDTO>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long deptId) {
        return Result.ok(dutyScheduleService.listByDateRange(startDate, endDate, deptId));
    }

    @Operation(summary = "删除排班")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dutyScheduleService.removeSchedule(id);
        return Result.ok(null);
    }
}
