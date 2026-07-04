package com.shiftsync.service;

import com.shiftsync.dto.request.MockLoginRequest;
import com.shiftsync.dto.response.AuthResponse;
import com.shiftsync.dto.response.UserResponse;
import com.shiftsync.entity.LeaveBalance;
import com.shiftsync.entity.User;
import com.shiftsync.entity.WfhBalance;
import com.shiftsync.enums.LeaveType;
import com.shiftsync.enums.Role;
import com.shiftsync.enums.ShiftType;
import com.shiftsync.repository.LeaveBalanceRepository;
import com.shiftsync.repository.UserRepository;
import com.shiftsync.repository.WfhBalanceRepository;
import com.shiftsync.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final WfhBalanceRepository wfhBalanceRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.wfh.monthly-allowance}")
    private int monthlyAllowance;

    // Default leave credits on joining
    private static final Map<LeaveType, Double> DEFAULT_LEAVE_CREDITS = Map.of(
            LeaveType.ANNUAL, 18.0,
            LeaveType.MEDICAL, 12.0,
            LeaveType.MATERNITY, 180.0,
            LeaveType.PATERNITY, 5.0,
            LeaveType.COMP_OFF, 0.0,
            LeaveType.LOP, 0.0
    );

    /**
     * Mock login — auto-creates user if not found.
     * In prod, this is replaced by Google OAuth2 flow.
     */
    @Transactional
    public AuthResponse mockLogin(MockLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> provisionNewEmployee(request));

        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        log.info("Mock login successful for: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String newAccessToken = jwtTokenProvider.generateToken(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    /**
     * Auto-provisions a new employee with default leave and WFH balances.
     * This simulates what would happen when HR onboards someone via Admin panel.
     */
    private User provisionNewEmployee(MockLoginRequest request) {
        log.info("Auto-provisioning new employee: {}", request.getEmail());

        User user = User.builder()
                .name(request.getName() != null ? request.getName() : request.getEmail().split("@")[0])
                .email(request.getEmail())
                .role(Role.EMPLOYEE)
                .defaultShift(ShiftType.GENERAL)
                .joiningDate(LocalDate.now())
                .teamName("Default Team")
                .provider("mock")
                .build();

        user = userRepository.save(user);

        // Auto-create leave balances for all leave types
        User finalUser = user;
        Arrays.stream(LeaveType.values()).forEach(type -> {
            double credit = DEFAULT_LEAVE_CREDITS.getOrDefault(type, 0.0);
            LeaveBalance balance = LeaveBalance.builder()
                    .user(finalUser)
                    .leaveType(type)
                    .balance(credit)
                    .totalAllocated(credit)
                    .totalUsed(0.0)
                    .build();
            leaveBalanceRepository.save(balance);
        });

        // Auto-create WFH balance
        WfhBalance wfhBalance = WfhBalance.builder()
                .user(finalUser)
                .currentMonthBalance(monthlyAllowance)
                .balanceMonth(YearMonth.now().toString())
                .totalWfhUsedThisYear(0)
                .build();
        wfhBalanceRepository.save(wfhBalance);

        log.info("New employee provisioned with ID: {}", user.getId());
        return user;
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .role(user.getRole())
                .defaultShift(user.getDefaultShift())
                .joiningDate(user.getJoiningDate())
                .teamName(user.getTeamName())
                .managerEmail(user.getManagerEmail())
                .build();
    }
}