package com.shiftsync.dto.response;

import com.shiftsync.enums.Role;
import com.shiftsync.enums.ShiftType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String profilePicture;
    private Role role;
    private ShiftType defaultShift;
    private LocalDate joiningDate;
    private String teamName;
    private String managerEmail;
}