package com.shiftsync.repository;

import com.shiftsync.entity.CompOffCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompOffCreditRepository extends JpaRepository<CompOffCredit, Long> {

    // All active (unused, not expired) comp-off credits for a user
    @Query("""
            SELECT c FROM CompOffCredit c
            WHERE c.user.id = :userId
            AND c.isUsed = false
            AND c.expiryDate >= :today
            ORDER BY c.expiryDate ASC
            """)
    List<CompOffCredit> findActiveCredits(@Param("userId") Long userId, @Param("today") LocalDate today);

    // Count active comp-off credits
    @Query("""
            SELECT COUNT(c) FROM CompOffCredit c
            WHERE c.user.id = :userId
            AND c.isUsed = false
            AND c.expiryDate >= :today
            """)
    long countActiveCredits(@Param("userId") Long userId, @Param("today") LocalDate today);

    // Check if comp-off already credited for a specific worked holiday
    boolean existsByUserIdAndWorkedOnDate(Long userId, LocalDate workedOnDate);

    // For the scheduler — find expiring credits
    List<CompOffCredit> findByIsUsedFalseAndExpiryDateBefore(LocalDate date);
}