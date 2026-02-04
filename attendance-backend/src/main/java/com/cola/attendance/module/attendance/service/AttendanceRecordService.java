package com.cola.attendance.module.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
import com.cola.attendance.module.attendance.dto.DeviceCallbackDTO;
import com.cola.attendance.module.attendance.dto.PunchCreateDTO;
import com.cola.attendance.module.attendance.entity.AttendanceRecordEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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

    /**
     * 设备回调上报一条打卡。根据 employeeNo 解析用户，根据 deviceCode/deviceIp 解析设备。
     * 无需登录态，供门禁/刷脸等设备对接。
     * @return 新建记录，解析失败返回 null
     */
    AttendanceRecordEntity saveFromDeviceCallback(DeviceCallbackDTO dto);

    /**
     * 单条打卡创建（模拟打卡或管理端补录）。需登录。
     */
    AttendanceRecordEntity saveOne(PunchCreateDTO dto);

    /**
     * 模拟打卡：按指定日期排班生成约 10 条打卡记录（含正常、迟到、早退等），并触发当天考勤计算。
     * @return 生成的打卡条数
     */
    int simulatePunchRecords(LocalDate date);
}
