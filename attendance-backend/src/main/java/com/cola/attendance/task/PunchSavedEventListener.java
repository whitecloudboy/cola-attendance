package com.cola.attendance.task;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 监听打卡保存事件，触发规则引擎更新考勤结果。
 */
@Component
public class PunchSavedEventListener {

    private final AttendancePunchTriggerService punchTriggerService;

    public PunchSavedEventListener(AttendancePunchTriggerService punchTriggerService) {
        this.punchTriggerService = punchTriggerService;
    }

    @EventListener
    @Async
    public void onPunchSaved(PunchSavedEvent event) {
        punchTriggerService.onPunchSaved(event.getUserId(), event.getAttendanceDate());
    }
}
