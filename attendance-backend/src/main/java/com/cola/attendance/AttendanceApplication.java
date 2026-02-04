package com.cola.attendance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 考勤与排班系统启动类。
 * <ul>
 *   <li>{@code @EnableScheduling}：启用定时任务（如排班、打卡触发等）</li>
 *   <li>{@code @EnableAsync}：启用异步执行</li>
 *   <li>{@code @MapperScan}：扫描 system / schedule / attendance 模块的 Mapper 接口</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan({"com.cola.attendance.module.system.dao", "com.cola.attendance.module.schedule.dao", "com.cola.attendance.module.attendance.dao"})
public class AttendanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceApplication.class, args);
    }
}
