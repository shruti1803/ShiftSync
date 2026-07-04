package com.shiftsync.entity;

import com.shiftsync.enums.Role;
import com.shiftsync.enums.ShiftType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_shift", nullable = false)
    private ShiftType defaultShift;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "manager_email")
    private String managerEmail;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // OAuth2 provider fields
    @Column(name = "provider")
    private String provider;       // e.g. "google"

    @Column(name = "provider_id")
    private String providerId;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LeaveBalance> leaveBalances = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WfhBalance wfhBalance;
}