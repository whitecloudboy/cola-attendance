package com.cola.attendance.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysMenuDTO {
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
    private List<SysMenuDTO> children;
}
