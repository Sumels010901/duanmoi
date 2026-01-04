package com.worktime.service;

import com.worktime.dto.scheduleoverride.ScheduleOverrideRequest;
import com.worktime.dto.scheduleoverride.ScheduleOverrideResponse;
import com.worktime.dto.workingschedule.WorkingScheduleRequest;
import com.worktime.dto.workingschedule.WorkingScheduleResponse;
import com.worktime.mapper.DtoMapper;
import com.worktime.model.ScheduleOverride;
import com.worktime.model.WorkingSchedule;
import com.worktime.repository.ScheduleOverrideRepository;
import com.worktime.repository.WorkingScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing working schedules and schedule overrides.
 *
 * @author Thang
 * @since 2026-01-02
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleManagementService {

    private final WorkingScheduleRepository scheduleRepository;
    private final ScheduleOverrideRepository overrideRepository;

    // ==================== Working Schedules ====================

    @Transactional
    public WorkingScheduleResponse createWorkingSchedule(WorkingScheduleRequest request) {
        log.info("Creating working schedule for user: {}, day: {}",
            request.userId(), request.dayOfWeek());

        WorkingSchedule schedule = DtoMapper.toEntity(request);
        WorkingSchedule saved = scheduleRepository.save(schedule);

        log.info("Created working schedule: {}", saved.getId());
        return DtoMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkingScheduleResponse> getActiveWorkingSchedules(String userId) {
        log.info("Fetching active working schedules for user: {}", userId);

        return scheduleRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(userId)
            .stream()
            .map(DtoMapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public WorkingScheduleResponse updateWorkingSchedule(UUID id, WorkingScheduleRequest request) {
        log.info("Updating working schedule: {}", id);

        WorkingSchedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Working schedule not found: " + id));

        // Update fields
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setTimezone(request.timezone());
        schedule.setIsActive(request.isActive());
        schedule.setEffectiveFrom(request.effectiveFrom());
        schedule.setEffectiveTo(request.effectiveTo());

        WorkingSchedule saved = scheduleRepository.save(schedule);

        log.info("Updated working schedule: {}", id);
        return DtoMapper.toDto(saved);
    }

    @Transactional
    public void deleteWorkingSchedule(UUID id) {
        log.info("Deleting working schedule: {}", id);

        WorkingSchedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Working schedule not found: " + id));

        schedule.softDelete();
        scheduleRepository.save(schedule);

        log.info("Soft deleted working schedule: {}", id);
    }

    // ==================== Schedule Overrides ====================

    @Transactional
    public ScheduleOverrideResponse createScheduleOverride(ScheduleOverrideRequest request) {
        log.info("Creating schedule override for date: {}", request.date());

        ScheduleOverride override = DtoMapper.toEntity(request);
        ScheduleOverride saved = overrideRepository.save(override);

        log.info("Created schedule override: {}", saved.getId());
        return DtoMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduleOverrideResponse> getScheduleOverrides(Instant startDate, Instant endDate) {
        log.info("Fetching schedule overrides from {} to {}", startDate, endDate);

        // Convert Instant to LocalDate (using system default timezone)
        LocalDate startLocalDate = startDate.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.atZone(ZoneId.systemDefault()).toLocalDate();

        return overrideRepository.findByDateBetweenAndIsDeletedFalse(startLocalDate, endLocalDate)
            .stream()
            .map(DtoMapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteScheduleOverride(UUID id) {
        log.info("Deleting schedule override: {}", id);

        ScheduleOverride override = overrideRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Schedule override not found: " + id));

        override.softDelete();
        overrideRepository.save(override);

        log.info("Soft deleted schedule override: {}", id);
    }
}
