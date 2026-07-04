package com.shiftsync.repository;

import com.shiftsync.entity.OnCallSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OnCallScheduleRepository extends JpaRepository<OnCallSchedule, Long> {

    Optional<OnCallSchedule> findByOnCallDate(LocalDate date);

    // Upcoming on-call duties for a user
    @Query("""
            SELECT o FROM OnCallSchedule o
            WHERE (o.primaryUser.id = :userId OR o.secondaryUser.id = :userId)
            AND o.onCallDate >= :fromDate
            ORDER BY o.onCallDate ASC
            """)
    List<OnCallSchedule> findUpcomingOnCallForUser(@Param("userId") Long userId,
                                                   @Param("fromDate") LocalDate fromDate);

    List<OnCallSchedule> findByOnCallDateBetweenOrderByOnCallDate(LocalDate from, LocalDate to);

    // Find on-call records on holidays where comp-off hasn't been credited yet
    @Query("""
            SELECT o FROM OnCallSchedule o
            WHERE o.onCallDate = :date
            AND o.compOffCredited = false
            """)
    List<OnCallSchedule> findUnCreditedOnCallOnDate(@Param("date") LocalDate date);
}