package com.cola.attendance.module.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.attendance.dto.AttendanceResultDTO;
import com.cola.attendance.module.attendance.entity.AttendanceResultEntity;
import com.cola.attendance.module.attendance.service.AttendanceResultService;
import com.cola.attendance.module.attendance.service.AttendanceTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "考勤结果")
@RestController
@RequestMapping("/attendance/result")
public class AttendanceResultController {

    private final AttendanceResultService attendanceResultService;
    private final AttendanceTaskService attendanceTaskService;

    public AttendanceResultController(AttendanceResultService attendanceResultService,
                                     AttendanceTaskService attendanceTaskService) {
        this.attendanceResultService = attendanceResultService;
        this.attendanceTaskService = attendanceTaskService;
    }

    @Operation(summary = "分页查询考勤结果")
    @GetMapping("/page")
    public Result<Page<AttendanceResultDTO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userKeyword) {
        return Result.ok(attendanceResultService.pageDto(
                Page.of(current, size), userId, deptId, startDate, endDate, userKeyword));
    }

    @Operation(summary = "按日期范围查询考勤结果")
    @GetMapping("/list")
    public Result<List<AttendanceResultDTO>> list(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long deptId) {
        return Result.ok(attendanceResultService.listByDateRange(
                java.time.LocalDate.parse(startDate),
                java.time.LocalDate.parse(endDate),
                deptId));
    }

    @Operation(summary = "根据ID查询考勤结果")
    @GetMapping("/{id}")
    public Result<AttendanceResultEntity> get(@PathVariable Long id) {
        return Result.ok(attendanceResultService.getById(id));
    }

    @Operation(summary = "手动生成指定日期空考勤记录（测试/补跑用）")
    @PostMapping("/generate-empty")
    public Result<Integer> generateEmpty(@RequestParam String date) {
        int count = attendanceResultService.generateEmptyForDate(LocalDate.parse(date));
        return Result.ok(count);
    }

    @Operation(summary = "手动触发布日终补录（E2E 测试用）")
    @PostMapping("/trigger-end-task")
    public Result<Integer> triggerEndTask(@RequestParam String date) {
        int processed = attendanceTaskService.runEndOfDaySupplement(LocalDate.parse(date));
        return Result.ok(processed);
    }

    @Operation(summary = "根据ID删除考勤结果（E2E 清理用）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        attendanceResultService.removeById(id);
        return Result.ok(null);
    }
}
