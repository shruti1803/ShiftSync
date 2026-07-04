package com.shiftsync.repository;

import com.shiftsync.entity.User;
import com.shiftsync.enums.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByTeamName(String teamName);

    List<User> findByDefaultShift(ShiftType shiftType);

    List<User> findByTeamNameAndIsActiveTrue(String teamName);

    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.name")
    List<User> findAllActiveUsers();
}