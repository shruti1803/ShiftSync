package com.shiftsync.repository;

import com.shiftsync.entity.WfhBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WfhBalanceRepository extends JpaRepository<WfhBalance, Long> {
    Optional<WfhBalance> findByUserId(Long userId);
}