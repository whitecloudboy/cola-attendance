package com.cola.attendance.module.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cola.attendance.module.attendance.entity.AttendanceRecordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttendanceRecordDao extends BaseMapper<AttendanceRecordEntity> {
}
