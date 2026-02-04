package com.cola.attendance.task;

import com.cola.attendance.module.attendance.service.AttendanceResultService;
import com.cola.attendance.module.attendance.service.AttendanceTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 考勤定时任务。按 attendance-optimal-design：每日生成空考勤、日终补录。
 */
@Component
public class AttendanceScheduleTask {

    private static final Logger log = LoggerFactory.getLogger(AttendanceScheduleTask.class);

    private final AttendanceResultService attendanceResultService;
    private final AttendanceTaskService attendanceTaskService;

    public AttendanceScheduleTask(AttendanceResultService attendanceResultService,
                                 AttendanceTaskService attendanceTaskService) {
        this.attendanceResultService = attendanceResultService;
        this.attendanceTaskService = attendanceTaskService;
    }

    /**
     * 每日凌晨 1 点生成当日空考勤记录。
     */
    @Scheduled(cron = "${attendance.schedule.generate-empty-cron:0 0 1 * * ?}")
    public void generateEmptyAttendanceResult() {
        LocalDate today = LocalDate.now();
        int count = attendanceResultService.generateEmptyForDate(today);
        log.info("[task] 生成当日空考勤记录: date={}, count={}", today, count);
    }

    /**
     * 日终补录：每日 23:55 对当日考勤结果执行 end-check，补录未打卡、异常等。
     */
    @Scheduled(cron = "${attendance.schedule.end-task-cron:0 55 23 * * ?}")
    public void endOfDaySupplement() {
        LocalDate today = LocalDate.now();
        int processed = attendanceTaskService.runEndOfDaySupplement(today);
        log.info("[task] 日终补录: date={}, processed={}", today, processed);
    }
}
