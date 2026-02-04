package com.cola.attendance.rule.dsl;

import com.cola.attendance.module.attendance.dto.AttendanceBanRecordDTO;
import com.cola.attendance.module.attendance.dto.AttendanceRecordDTO;
import com.cola.attendance.module.attendance.service.AttendanceBanRecordAdapter;
import com.cola.attendance.module.system.dto.SysDeptDTO;
import com.cola.attendance.module.system.service.SysDeptService;
import com.cola.attendance.rule.RuleContext;
import com.cola.attendance.rule.RuleDutyFacade;
import com.cola.attendance.rule.dto.RuleShiftDTO;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * DSL 表达式函数集合（作为 SpEL 根对象使用），复用旧架构逻辑。
 */
public class DslFunctionHelper {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RuleContext context;
    private final AttendanceBanRecordAdapter banRecordService;
    private final RuleDutyFacade dutyBanService;
    private final SysDeptService sysDeptService;

    public DslFunctionHelper(RuleContext context,
                             AttendanceBanRecordAdapter banRecordService,
                             RuleDutyFacade dutyBanService,
                             SysDeptService sysDeptService) {
        this.context = context;
        this.banRecordService = banRecordService;
        this.dutyBanService = dutyBanService;
        this.sysDeptService = sysDeptService;
    }

    public boolean isPerson(Long personId) {
        AttendanceBanRecordDTO ban = getBan();
        return ban != null && personId != null && personId.equals(ban.getPersonId());
    }

    public boolean inPerson(Collection<?> personIds) {
        AttendanceBanRecordDTO ban = getBan();
        if (ban == null || ban.getPersonId() == null || personIds == null) return false;
        return containsId(personIds, ban.getPersonId());
    }

    public boolean isDept(Long deptId) {
        AttendanceBanRecordDTO ban = getBan();
        return ban != null && deptId != null && deptId.equals(ban.getDeptId());
    }

    public boolean inDept(Collection<?> deptIds, boolean includeSub) {
        AttendanceBanRecordDTO ban = getBan();
        if (ban == null || ban.getDeptId() == null || deptIds == null) return false;
        if (containsId(deptIds, ban.getDeptId())) return true;
        if (!includeSub || sysDeptService == null) return false;
        for (Object deptIdObj : deptIds) {
            Long deptId = toLong(deptIdObj);
            if (deptId == null) continue;
            List<Long> subs = sysDeptService.getSubDeptIdList(deptId);
            if (subs != null && subs.contains(ban.getDeptId())) return true;
        }
        return false;
    }

    public int lateCountInWeek() {
        AttendanceBanRecordDTO ban = getBan();
        if (ban == null || ban.getPersonId() == null || ban.getAttendanceDate() == null) return 0;
        return lateCountInWeek(ban.getPersonId(), ban.getAttendanceDate());
    }

    public int lateCountInWeek(Long personId, String attendanceDate) {
        if (banRecordService == null || personId == null || attendanceDate == null) return 0;
        return banRecordService.countLateInWeek(personId, attendanceDate);
    }

    public int minutesLate() {
        AttendanceBanRecordDTO ban = getBan();
        if (ban == null || ban.getStartTime() == null) return Integer.MAX_VALUE;
        LocalDateTime checkIn = resolveCheckIn();
        if (checkIn == null) return Integer.MAX_VALUE;
        LocalDate date = LocalDate.parse(ban.getAttendanceDate());
        LocalDateTime base = date.atTime(ban.getStartTime());
        return (int) Duration.between(base, checkIn).toMinutes();
    }

    public int minutesEarlyLeave() {
        AttendanceBanRecordDTO ban = getBan();
        if (ban == null || ban.getEndTime() == null) return Integer.MAX_VALUE;
        LocalDateTime checkOut = resolveCheckOut();
        if (checkOut == null) return Integer.MAX_VALUE;
        LocalDate date = LocalDate.parse(ban.getAttendanceDate());
        LocalDateTime base = date.atTime(ban.getEndTime());
        if (ban.getStartTime() != null && ban.getStartTime().isAfter(ban.getEndTime())) base = base.plusDays(1);
        return (int) Duration.between(checkOut, base).toMinutes();
    }

    public Boolean handoverOk() {
        AttendanceBanRecordDTO ban = getBan();
        if (ban == null || ban.getSetBanId() == null) return null;
        LocalDateTime checkOut = resolveCheckOut();
        if (checkOut == null) return null;
        RuleShiftDTO dutyBan = dutyBanService != null ? dutyBanService.getBanByScheduleId(ban.getSetBanId()) : null;
        if (dutyBan == null) return null;
        List<RuleShiftDTO> afterBans = getAfterBans(dutyBan);
        if (afterBans.isEmpty()) return null;
        String workDay = ban.getAttendanceDate();
        if (ban.getStartTime() != null && ban.getEndTime() != null && ban.getStartTime().isAfter(ban.getEndTime())) {
            workDay = DATE_FORMAT.format(LocalDate.parse(workDay).plusDays(1));
        }
        LocalTime checkEndTime = checkOut.toLocalTime();
        for (RuleShiftDTO afterBan : afterBans) {
            List<AttendanceBanRecordDTO> recordList = banRecordService.getListByBanIdAndWorkDay(afterBan.getId(), workDay);
            if (recordList.isEmpty()) continue;
            for (AttendanceBanRecordDTO recordDTO : recordList) {
                if (recordDTO != null && recordDTO.getCheckStartTime() != null) {
                    if (checkEndTime.equals(recordDTO.getCheckStartTime()) || checkEndTime.isAfter(recordDTO.getCheckStartTime())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private AttendanceBanRecordDTO getBan() {
        return context != null ? context.getBanRecord() : null;
    }

    private boolean containsId(Collection<?> ids, Long target) {
        if (ids == null || target == null) return false;
        for (Object item : ids) {
            Long id = toLong(item);
            if (id != null && id.equals(target)) return true;
            if (item instanceof CharSequence && target.toString().contentEquals((CharSequence) item)) return true;
        }
        return false;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        if (value instanceof CharSequence s) {
            try { return Long.parseLong(s.toString().trim()); } catch (Exception ignored) { return null; }
        }
        return null;
    }

    private LocalDateTime resolveCheckIn() {
        AttendanceBanRecordDTO ban = getBan();
        if (ban != null && ban.getCheckStartTime() != null && ban.getAttendanceDate() != null) {
            LocalDate day = LocalDate.parse(ban.getAttendanceDate());
            return ban.getCheckStartTime().atDate(day);
        }
        List<AttendanceRecordDTO> records = context != null && context.getEvent() != null ? context.getEvent().getAttendanceRecords() : null;
        if (records == null || records.isEmpty()) return context != null && context.getEvent() != null ? context.getEvent().getOccurredAt() : null;
        LocalDate attendanceDate = ban != null && ban.getAttendanceDate() != null ? LocalDate.parse(ban.getAttendanceDate()) : (context.getEvent() != null ? context.getEvent().getAttendanceDate() : null);
        LocalTime startTime = ban != null ? ban.getStartTime() : null;
        if (attendanceDate == null || startTime == null) {
            return records.stream().filter(r -> r.getOpenTime() != null).map(this::parse).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
        }
        LocalDateTime base = attendanceDate.atTime(startTime);
        List<LocalDateTime> candidates = records.stream().filter(r -> r.getOpenTime() != null).map(this::parse).filter(Objects::nonNull).collect(Collectors.toList());
        if (context != null && context.getEvent() != null && context.getEvent().getOccurredAt() != null) {
            candidates.add(context.getEvent().getOccurredAt());
        }
        return candidates.stream().min(Comparator.comparingLong(dt -> Math.abs(Duration.between(base, dt).toMinutes()))).orElse(null);
    }

    private LocalDateTime resolveCheckOut() {
        List<AttendanceRecordDTO> records = context != null && context.getEvent() != null ? context.getEvent().getAttendanceRecords() : null;
        if (records == null || records.isEmpty()) return context != null && context.getEvent() != null ? context.getEvent().getOccurredAt() : null;
        AttendanceBanRecordDTO ban = getBan();
        LocalDate attendanceDate = ban != null && ban.getAttendanceDate() != null ? LocalDate.parse(ban.getAttendanceDate()) : (context.getEvent() != null ? context.getEvent().getAttendanceDate() : null);
        LocalTime endTime = ban != null ? ban.getEndTime() : null;
        if (attendanceDate == null || endTime == null) {
            return records.stream().filter(r -> r.getOpenTime() != null).map(this::parse).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        }
        boolean crossDay = ban != null && ban.getStartTime() != null && ban.getStartTime().isAfter(endTime);
        LocalDateTime base = attendanceDate.atTime(endTime);
        if (crossDay) base = base.plusDays(1);
        LocalTime startTime = ban != null ? ban.getStartTime() : null;
        LocalDateTime startBase = startTime != null ? attendanceDate.atTime(startTime) : null;
        LocalDateTime mid = (startBase != null) ? startBase.plusMinutes(Duration.between(startBase, base).toMinutes() / 2) : null;
        final LocalDateTime baseFinal = base;
        LocalDateTime occurredAt = context != null && context.getEvent() != null ? context.getEvent().getOccurredAt() : null;
        List<LocalDateTime> candidates = records.stream().filter(r -> r.getOpenTime() != null).map(this::parse).filter(Objects::nonNull).filter(dt -> mid == null || !dt.isBefore(mid)).collect(Collectors.toList());
        if (occurredAt != null) {
            if (crossDay && attendanceDate != null && occurredAt.toLocalDate().isBefore(attendanceDate)) occurredAt = occurredAt.plusDays(1);
            if (mid == null || !occurredAt.isBefore(mid)) candidates.add(occurredAt);
        }
        return candidates.stream().min(Comparator.comparingLong(dt -> Math.abs(Duration.between(baseFinal, dt).toMinutes()))).orElse(null);
    }

    private LocalDateTime parse(AttendanceRecordDTO record) {
        if (record == null || record.getOpenTime() == null) return null;
        try {
            return LocalDateTime.parse(record.getOpenTime(), DT_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前班次的“下一班”列表，用于交接班判断。
     * 优先按班次分组号（groupNo）同组 + 结束时间与下一班开始时间衔接；未设 groupNo 时按部门+颜色+时间衔接（兼容旧逻辑）。
     */
    private List<RuleShiftDTO> getAfterBans(RuleShiftDTO ban) {
        if (ban == null || dutyBanService == null) return Collections.emptyList();
        String groupNo = ban.getGroupNo();
        boolean useGroupNo = groupNo != null && !groupNo.isBlank();

        List<RuleShiftDTO> bans = useGroupNo
            ? dutyBanService.getListByDeptIdAndGroupNo(ban.getDeptId(), groupNo)
            : dutyBanService.getListByDeptIdAndColor(ban.getDeptId(), ban.getBanColor());
        if (bans.isEmpty()) return bans;
        bans = bans.stream()
            .filter(d -> ban.getEndTime() != null && ban.getEndTime().equals(d.getBeginTime()))
            .collect(Collectors.toList());

        if (bans.isEmpty() && sysDeptService != null) {
            SysDeptDTO deptDTO = sysDeptService.getById(ban.getDeptId()) != null ? toDeptDto(sysDeptService.getById(ban.getDeptId())) : null;
            if (deptDTO != null && deptDTO.getParentId() != null) {
                List<Long> deptIds = sysDeptService.getSubDeptIdList(deptDTO.getParentId());
                for (Long deptId : deptIds) {
                    List<RuleShiftDTO> banList = useGroupNo
                        ? dutyBanService.getListByDeptIdAndGroupNo(deptId, groupNo)
                        : dutyBanService.getListByDeptIdAndColor(deptId, ban.getBanColor());
                    for (RuleShiftDTO d : banList) {
                        if (ban.getEndTime() != null && ban.getEndTime().equals(d.getBeginTime())) bans.add(d);
                    }
                }
            }
        }
        return bans;
    }

    private static SysDeptDTO toDeptDto(com.cola.attendance.module.system.entity.SysDeptEntity e) {
        if (e == null) return null;
        SysDeptDTO d = new SysDeptDTO();
        d.setId(e.getId());
        d.setParentId(e.getParentId());
        d.setName(e.getName());
        return d;
    }
}
