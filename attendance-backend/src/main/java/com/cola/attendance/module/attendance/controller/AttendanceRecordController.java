package com.cola.attendance.module.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
import com.cola.attendance.module.attendance.dto.DeviceCallbackDTO;
import com.cola.attendance.module.attendance.dto.PunchCreateDTO;
import com.cola.attendance.module.attendance.entity.AttendanceRecordEntity;
import com.cola.attendance.module.attendance.service.AttendanceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Tag(name = "打卡记录")
@RestController
@RequestMapping("/attendance/record")
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;

    public AttendanceRecordController(AttendanceRecordService attendanceRecordService) {
        this.attendanceRecordService = attendanceRecordService;
    }

    @Operation(summary = "分页查询（按人、设备、日期范围；userKeyword 为姓名或工号模糊）")
    @GetMapping("/page")
    public Result<Page<AttendanceRecordDTO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userKeyword) {
        Long userIdLong = parseLongOrNull(userId);
        Long deviceIdLong = parseLongOrNull(deviceId);
        return Result.ok(attendanceRecordService.pageDto(
                Page.of(current, size), userIdLong, deviceIdLong, blankToNull(startDate), blankToNull(endDate), blankToNull(userKeyword)));
    }

    private static Long parseLongOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Long.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String blankToNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }

    @Operation(summary = "下载打卡记录导入模板")
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] body = attendanceRecordService.downloadTemplate();
        String filename = URLEncoder.encode("打卡记录导入模板.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .body(body);
    }

    @Operation(summary = "Excel 导入打卡记录")
    @PostMapping("/import")
    public Result<Integer> importExcel(@RequestParam("file") MultipartFile file) {
        int count = attendanceRecordService.importExcel(file);
        return Result.ok(count);
    }

    @Operation(summary = "设备回调上报打卡（兼容门禁/刷脸设备，无需登录）")
    @PostMapping("/callback")
    public Result<AttendanceRecordDTO> deviceCallback(@RequestBody DeviceCallbackDTO dto) {
        AttendanceRecordEntity entity = attendanceRecordService.saveFromDeviceCallback(dto);
        if (entity == null) {
            return Result.fail("人员或时间解析失败，请检查 employeeNo、eventTime");
        }
        AttendanceRecordDTO result = new AttendanceRecordDTO();
        result.setId(entity.getId());
        result.setUserId(entity.getUserId());
        result.setUserName(entity.getUserName());
        result.setEventTime(entity.getEventTime());
        result.setDeviceId(entity.getDeviceId());
        return Result.ok(result);
    }

    @Operation(summary = "单条打卡创建（模拟打卡/补录，需登录）")
    @PostMapping
    public Result<AttendanceRecordDTO> create(@RequestBody PunchCreateDTO dto) {
        AttendanceRecordEntity entity = attendanceRecordService.saveOne(dto);
        AttendanceRecordDTO result = new AttendanceRecordDTO();
        result.setId(entity.getId());
        result.setUserId(entity.getUserId());
        result.setUserName(entity.getUserName());
        result.setEventTime(entity.getEventTime());
        result.setDeviceId(entity.getDeviceId());
        return Result.ok(result);
    }

    @Operation(summary = "模拟打卡：生成当天约 10 条打卡记录（含正常/迟到/早退）并触发考勤计算")
    @PostMapping("/simulate")
    public Result<Integer> simulate(@RequestParam(required = false) String date) {
        LocalDate d = (date != null && !date.isBlank()) ? LocalDate.parse(date.trim()) : LocalDate.now();
        int count = attendanceRecordService.simulatePunchRecords(d);
        return Result.ok(count);
    }

    @Operation(summary = "根据ID删除打卡记录（E2E 清理用）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        attendanceRecordService.removeById(id);
        return Result.ok(null);
    }
}
