package com.shiftsync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "holidays",
        uniqueConstraints = @UniqueConstraint(columnNames = {"holiday_date", "country_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holiday extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(nullable = false)
    private String name;

    // "IN" for India, "US" for United States
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "is_optional")
    @Builder.Default
    private Boolean isOptional = false;

    @Column(length = 500)
    private String description;
}