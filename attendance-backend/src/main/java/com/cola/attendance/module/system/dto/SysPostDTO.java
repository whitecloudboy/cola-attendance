package com.cola.attendance.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysPostDTO {
    private Long id;
    private String name;
    private String code;
    private Long deptId;
    private String deptName;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
