package com.cola.attendance.module.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 单条打卡记录创建（模拟打卡 / 管理端补录）。
 */
@Data
@Schema(description = "单条打卡创建请求")
public class PunchCreateDTO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "打卡时间", example = "2025-02-05 08:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventTime;

    @Schema(description = "考勤设备ID，可选")
    private Long deviceId;

    @Schema(description = "事件类型，如 in/out")
    private String eventType;

    @Schema(description = "备注")
    private String remark;
}
