package com.cola.attendance.module.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cola.attendance.module.attendance.dao.AttendanceDeviceDao;
import com.cola.attendance.module.attendance.dao.AttendanceRecordDao;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
import com.cola.attendance.module.attendance.entity.AttendanceDeviceEntity;
import com.cola.attendance.module.attendance.entity.AttendanceRecordEntity;
import com.cola.attendance.module.attendance.service.AttendanceRecordService;
import com.cola.attendance.module.system.dao.SysUserDao;
import com.cola.attendance.module.system.entity.SysUserEntity;
import com.cola.attendance.module.system.service.SysDeptService;
import com.cola.attendance.task.PunchSavedEvent;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceRecordServiceImpl extends ServiceImpl<AttendanceRecordDao, AttendanceRecordEntity> implements AttendanceRecordService {

    private final AttendanceDeviceDao attendanceDeviceDao;
    private final SysUserDao sysUserDao;
    private final SysDeptService sysDeptService;
    private final ApplicationEventPublisher eventPublisher;

    public AttendanceRecordServiceImpl(AttendanceDeviceDao attendanceDeviceDao, SysUserDao sysUserDao, SysDeptService sysDeptService, ApplicationEventPublisher eventPublisher) {
        this.attendanceDeviceDao = attendanceDeviceDao;
        this.sysUserDao = sysUserDao;
        this.sysDeptService = sysDeptService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Page<AttendanceRecordDTO> pageDto(Page<AttendanceRecordEntity> page, Long userId, Long deviceId, String startDate, String endDate, String userKeyword) {
        LambdaQueryWrapper<AttendanceRecordEntity> q = new LambdaQueryWrapper<>();
        if (userId != null) {
            q.eq(AttendanceRecordEntity::getUserId, userId);
        } else if (userKeyword != null && !userKeyword.isBlank()) {
            List<Long> userIds = sysUserDao.selectList(
                    new LambdaQueryWrapper<SysUserEntity>()
                            .like(SysUserEntity::getUsername, userKeyword)
                            .or().like(SysUserEntity::getDisplayName, userKeyword))
                    .stream().map(SysUserEntity::getId).toList();
            if (userIds.isEmpty()) {
                q.eq(AttendanceRecordEntity::getUserId, -1L);
            } else {
                q.in(AttendanceRecordEntity::getUserId, userIds);
            }
        }
        if (deviceId != null) q.eq(AttendanceRecordEntity::getDeviceId, deviceId);
        if (startDate != null && !startDate.isBlank()) {
            q.ge(AttendanceRecordEntity::getEventTime, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isBlank()) {
            q.le(AttendanceRecordEntity::getEventTime, endDate + " 23:59:59");
        }
        q.orderByDesc(AttendanceRecordEntity::getEventTime);
        Page<AttendanceRecordEntity> entityPage = this.page(page, q);
        List<AttendanceRecordDTO> list = entityPage.getRecords().stream().map(this::toDto).collect(Collectors.toList());
        Page<AttendanceRecordDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(list);
        return dtoPage;
    }

    @Override
    public int importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传 Excel 文件");
        }
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".xlsx") && !name.endsWith(".xls"))) {
            throw new IllegalArgumentException("仅支持 .xlsx 或 .xls 文件");
        }
        int count = 0;
        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) return 0;
            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String userStr = getCellString(row.getCell(0));
                String timeStr = getCellString(row.getCell(1));
                String deviceCodeStr = getCellString(row.getCell(2));
                if (userStr == null || userStr.isBlank() || timeStr == null || timeStr.isBlank()) continue;
                SysUserEntity user = resolveUser(userStr.trim());
                if (user == null) continue;
                LocalDateTime eventTime = parseEventTime(row.getCell(1), timeStr.trim());
                if (eventTime == null) continue;
                Long deviceId = null;
                if (deviceCodeStr != null && !deviceCodeStr.isBlank()) {
                    AttendanceDeviceEntity dev = attendanceDeviceDao.selectOne(
                            new LambdaQueryWrapper<AttendanceDeviceEntity>().eq(AttendanceDeviceEntity::getDeviceCode, deviceCodeStr.trim()));
                    if (dev != null) deviceId = dev.getId();
                }
                AttendanceRecordEntity rec = new AttendanceRecordEntity();
                rec.setUserId(user.getId());
                rec.setUserName(user.getDisplayName());
                rec.setDeptId(user.getDeptId());
                rec.setEventTime(eventTime);
                rec.setDeviceId(deviceId);
                save(rec);
                eventPublisher.publishEvent(new PunchSavedEvent(this, user.getId(), eventTime.toLocalDate()));
                count++;
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException) e;
            throw new RuntimeException("导入失败: " + e.getMessage(), e);
        }
        return count;
    }

    @Override
    public byte[] downloadTemplate() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("打卡记录");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("姓名或工号");
            header.createCell(1).setCellValue("打卡时间");
            header.createCell(2).setCellValue("设备编码(可选)");
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("张三");
            sample.createCell(1).setCellValue("2025-02-01 08:30:00");
            sample.createCell(2).setCellValue("");
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 5500);
            sheet.setColumnWidth(2, 4500);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成模板失败: " + e.getMessage(), e);
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date d = cell.getDateCellValue();
                    yield d != null ? LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()).toString().replace("T", " ") : null;
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    private LocalDateTime parseEventTime(Cell cell, String timeStr) {
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date d = cell.getDateCellValue();
            if (d != null) return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
        }
        try {
            if (timeStr.length() <= 10) {
                return LocalDate.parse(timeStr).atStartOfDay();
            }
            return LocalDateTime.parse(timeStr.replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
    }

    private SysUserEntity resolveUser(String userStr) {
        SysUserEntity u = sysUserDao.selectOne(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUsername, userStr));
        if (u != null) return u;
        return sysUserDao.selectOne(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getDisplayName, userStr));
    }

    private AttendanceRecordDTO toDto(AttendanceRecordEntity e) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(e.getId());
        dto.setUserId(e.getUserId());
        dto.setUserName(e.getUserName());
        dto.setDeptId(e.getDeptId());
        if (e.getDeptId() != null) {
            var dept = sysDeptService.getById(e.getDeptId());
            dto.setDeptName(dept != null ? dept.getName() : null);
        }
        dto.setEventTime(e.getEventTime());
        dto.setDeviceId(e.getDeviceId());
        if (e.getDeviceId() != null) {
            var dev = attendanceDeviceDao.selectById(e.getDeviceId());
            dto.setDeviceName(dev != null ? dev.getDeviceName() : null);
        }
        dto.setEventType(e.getEventType());
        dto.setRemark(e.getRemark());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
