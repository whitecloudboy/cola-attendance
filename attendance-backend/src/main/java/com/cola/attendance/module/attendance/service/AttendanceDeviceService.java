package com.cola.attendance.module.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cola.attendance.module.attendance.dto.AttendanceDeviceDTO;
import com.cola.attendance.module.attendance.entity.AttendanceDeviceEntity;

import java.util.List;

public interface AttendanceDeviceService extends IService<AttendanceDeviceEntity> {

    List<AttendanceDeviceDTO> listDto(Long deptId);

    Page<AttendanceDeviceDTO> pageDto(Page<AttendanceDeviceEntity> page, String deviceName, Long deptId);
}
