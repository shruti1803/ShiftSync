package com.shiftsync.repository;

import com.shiftsync.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, Long> {

    Optional<ShiftSchedule> findByUserIdAndShiftDate(Long userId, LocalDate shiftDate);

    List<ShiftSchedule> findByUserIdAndShiftDateBetweenOrderByShiftDate(Long userId, LocalDate from, LocalDate to);

    // Get all team shifts in a range — for the team calendar
    @Query("""
            SELECT ss FROM ShiftSchedule ss
            WHERE ss.user.teamName = :teamName
            AND ss.shiftDate BETWEEN :fromDate AND :toDate
            ORDER BY ss.shiftDate, ss.user.name
            """)
    List<ShiftSchedule> findTeamShiftsInRange(@Param("teamName") String teamName,
                                              @Param("fromDate") LocalDate fromDate,
                                              @Param("toDate") LocalDate toDate);
}