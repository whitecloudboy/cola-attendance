package com.cola.attendance.module.attendance.service;

import java.time.LocalDate;

/**
 * 考勤任务服务，供定时任务与 E2E 手动触发共用。
 */
public interface AttendanceTaskService {

    /**
     * 执行日终补录：对指定日期的 attendance_result 执行 end-check 规则更新。
     * @return 处理条数
     */
    int runEndOfDaySupplement(LocalDate date);
}
