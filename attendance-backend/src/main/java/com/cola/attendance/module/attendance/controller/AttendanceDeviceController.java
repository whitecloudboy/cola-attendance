package com.cola.attendance.module.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.attendance.dto.AttendanceDeviceDTO;
import com.cola.attendance.module.attendance.entity.AttendanceDeviceEntity;
import com.cola.attendance.module.attendance.service.AttendanceDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "考勤设备")
@RestController
@RequestMapping("/attendance/device")
public class AttendanceDeviceController {

    private final AttendanceDeviceService attendanceDeviceService;

    public AttendanceDeviceController(AttendanceDeviceService attendanceDeviceService) {
        this.attendanceDeviceService = attendanceDeviceService;
    }

    @Operation(summary = "列表")
    @GetMapping("/list")
    public Result<List<AttendanceDeviceDTO>> list(@RequestParam(required = false) Long deptId) {
        return Result.ok(attendanceDeviceService.listDto(deptId));
    }

    @Operation(summary = "分页")
    @GetMapping("/page")
    public Result<Page<AttendanceDeviceDTO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deptId) {
        return Result.ok(attendanceDeviceService.pageDto(Page.of(current, size), deviceName, deptId));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public Result<AttendanceDeviceEntity> get(@PathVariable Long id) {
        return Result.ok(attendanceDeviceService.getById(id));
    }

    @Operation(summary = "保存")
    @PostMapping
    public Result<Void> save(@RequestBody AttendanceDeviceEntity entity) {
        if (entity.getIsAttendance() == null) entity.setIsAttendance(1);
        if (entity.getStatus() == null) entity.setStatus(1);
        attendanceDeviceService.save(entity);
        return Result.ok(null);
    }

    @Operation(summary = "更新")
    @PutMapping
    public Result<Void> update(@RequestBody AttendanceDeviceEntity entity) {
        attendanceDeviceService.updateById(entity);
        return Result.ok(null);
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        attendanceDeviceService.removeById(id);
        return Result.ok(null);
    }
}
