package com.cola.attendance.task;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

@Getter
public class PunchSavedEvent extends ApplicationEvent {
    private final Long userId;
    private final LocalDate attendanceDate;

    public PunchSavedEvent(Object source, Long userId, LocalDate attendanceDate) {
        super(source);
        this.userId = userId;
        this.attendanceDate = attendanceDate;
    }
}
