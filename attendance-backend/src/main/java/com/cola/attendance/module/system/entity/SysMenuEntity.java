package com.cola.attendance.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenuEntity {
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private Integer type;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer visible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    @TableLogic
    private Integer deleted;
}
