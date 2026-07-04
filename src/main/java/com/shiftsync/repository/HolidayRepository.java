package com.shiftsync.repository;

import com.shiftsync.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByHolidayDateBetweenOrderByHolidayDate(LocalDate from, LocalDate to);

    List<Holiday> findByCountryCodeAndHolidayDateBetween(String countryCode, LocalDate from, LocalDate to);

    boolean existsByHolidayDateAndCountryCode(LocalDate date, String countryCode);
}