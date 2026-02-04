package com.cola.attendance.module.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备回调上报打卡数据。兼容门禁/刷脸等设备接口。
 * 设备通过 POST 上报，无需登录态。
 */
@Data
@Schema(description = "设备回调打卡请求")
public class DeviceCallbackDTO {

    @Schema(description = "人员标识：工号/卡号/用户名，用于解析 sys_user", example = "admin")
    private String employeeNo;

    @Schema(description = "打卡时间", example = "2025-02-05 08:30:00")
    private String eventTime;

    @Schema(description = "设备编码，与 deviceIp 二选一")
    private String deviceCode;

    @Schema(description = "设备 IP，与 deviceCode 二选一，用于匹配 attendance_device.ip_address")
    private String deviceIp;

    @Schema(description = "体温等扩展")
    private String temperature;

    @Schema(description = "事件类型，如 in/out/open")
    private String eventType;

    @Schema(description = "备注")
    private String remark;
}
