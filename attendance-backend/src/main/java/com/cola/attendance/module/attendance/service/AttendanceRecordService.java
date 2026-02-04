package com.cola.attendance.module.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
import com.cola.attendance.module.attendance.entity.AttendanceRecordEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttendanceRecordService extends IService<AttendanceRecordEntity> {

    Page<AttendanceRecordDTO> pageDto(Page<AttendanceRecordEntity> page, Long userId, Long deviceId, String startDate, String endDate, String userKeyword);

    /**
     * Excel 导入打卡记录。表头：姓名或工号、打卡时间、设备编码(可选)。
     * 第2行起为数据。
     */
    int importExcel(MultipartFile file);

    /**
     * 生成打卡记录导入用的 Excel 模板（含表头与示例行）。
     */
    byte[] downloadTemplate();
}
