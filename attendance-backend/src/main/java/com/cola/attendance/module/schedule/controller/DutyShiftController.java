package com.cola.attendance.module.schedule.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.schedule.dto.DutyShiftDTO;
import com.cola.attendance.module.schedule.entity.DutyShiftEntity;
import com.cola.attendance.module.schedule.service.DutyShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "班次")
@RestController
@RequestMapping("/schedule/shift")
public class DutyShiftController {

    private final DutyShiftService dutyShiftService;

    public DutyShiftController(DutyShiftService dutyShiftService) {
        this.dutyShiftService = dutyShiftService;
    }

    @Operation(summary = "列表（按部门、排序）")
    @GetMapping("/list")
    public Result<List<DutyShiftDTO>> list(@RequestParam(required = false) Long deptId) {
        return Result.ok(dutyShiftService.listDto(deptId));
    }

    @Operation(summary = "分页")
    @GetMapping("/page")
    public Result<Page<DutyShiftDTO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long deptId) {
        return Result.ok(dutyShiftService.pageDto(Page.of(current, size), name, deptId));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<DutyShiftEntity> get(@PathVariable Long id) {
        return Result.ok(dutyShiftService.getById(id));
    }

    @Operation(summary = "保存")
    @PostMapping
    public Result<Void> save(@RequestBody DutyShiftEntity entity) {
        dutyShiftService.save(entity);
        return Result.ok(null);
    }

    @Operation(summary = "更新")
    @PutMapping
    public Result<Void> update(@RequestBody DutyShiftEntity entity) {
        dutyShiftService.updateById(entity);
        return Result.ok(null);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dutyShiftService.removeById(id);
        return Result.ok(null);
    }
}
