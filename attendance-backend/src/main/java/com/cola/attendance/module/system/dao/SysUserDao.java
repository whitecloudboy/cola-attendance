package com.cola.attendance.module.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cola.attendance.module.system.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserDao extends BaseMapper<SysUserEntity> {
}
