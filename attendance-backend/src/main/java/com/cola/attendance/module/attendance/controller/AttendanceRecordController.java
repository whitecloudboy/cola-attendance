package com.cola.attendance.module.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cola.attendance.common.result.Result;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
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
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userKeyword) {
        return Result.ok(attendanceRecordService.pageDto(
                Page.of(current, size), userId, deviceId, startDate, endDate, userKeyword));
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

    @Operation(summary = "根据ID删除打卡记录（E2E 清理用）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        attendanceRecordService.removeById(id);
        return Result.ok(null);
    }
}
