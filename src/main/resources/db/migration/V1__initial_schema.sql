-- ShiftSync Database Schema
-- V1: Initial schema creation

-- Users table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       profile_picture VARCHAR(500),
                       role VARCHAR(50) NOT NULL DEFAULT 'EMPLOYEE',
                       default_shift VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
                       joining_date DATE NOT NULL,
                       team_name VARCHAR(255),
                       manager_email VARCHAR(255),
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       provider VARCHAR(50),
                       provider_id VARCHAR(255),
                       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMP
);

-- Leave balances
CREATE TABLE leave_balances (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                leave_type VARCHAR(50) NOT NULL,
                                balance DOUBLE PRECISION NOT NULL DEFAULT 0,
                                total_allocated DOUBLE PRECISION NOT NULL DEFAULT 0,
                                total_used DOUBLE PRECISION NOT NULL DEFAULT 0,
                                created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMP,
                                UNIQUE (user_id, leave_type)
);

-- Leave requests
CREATE TABLE leave_requests (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                leave_type VARCHAR(50) NOT NULL,
                                from_date DATE NOT NULL,
                                to_date DATE NOT NULL,
                                number_of_days DOUBLE PRECISION NOT NULL,
                                reason VARCHAR(500) NOT NULL,
                                status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                manager_comment VARCHAR(500),
                                applied_on_holiday BOOLEAN DEFAULT FALSE,
                                holiday_name VARCHAR(255),
                                created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMP
);

-- WFH balances
CREATE TABLE wfh_balances (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE UNIQUE,
                              current_month_balance INTEGER NOT NULL DEFAULT 2,
                              balance_month VARCHAR(7) NOT NULL,  -- e.g. "2025-01"
                              total_wfh_used_this_year INTEGER NOT NULL DEFAULT 0,
                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMP
);

-- WFH requests
CREATE TABLE wfh_requests (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              wfh_date DATE NOT NULL,
                              reason VARCHAR(500) NOT NULL,
                              status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                              manager_comment VARCHAR(500),
                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMP
);

-- Shift schedules
CREATE TABLE shift_schedules (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                 shift_date DATE NOT NULL,
                                 shift_type VARCHAR(50) NOT NULL,
                                 is_swapped BOOLEAN NOT NULL DEFAULT FALSE,
                                 created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMP,
                                 UNIQUE (user_id, shift_date)
);

-- Shift swap requests
CREATE TABLE shift_swap_requests (
                                     id BIGSERIAL PRIMARY KEY,
                                     requester_id BIGINT NOT NULL REFERENCES users(id),
                                     target_id BIGINT NOT NULL REFERENCES users(id),
                                     requester_shift_date DATE NOT NULL,
                                     target_shift_date DATE NOT NULL,
                                     reason VARCHAR(500) NOT NULL,
                                     status VARCHAR(50) NOT NULL DEFAULT 'PENDING_TARGET_APPROVAL',
                                     target_comment VARCHAR(500),
                                     manager_comment VARCHAR(500),
                                     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                     updated_at TIMESTAMP
);

-- Comp-off credits
CREATE TABLE comp_off_credits (
                                  id BIGSERIAL PRIMARY KEY,
                                  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                  worked_on_date DATE NOT NULL,
                                  holiday_name VARCHAR(255) NOT NULL,
                                  expiry_date DATE NOT NULL,
                                  is_used BOOLEAN NOT NULL DEFAULT FALSE,
                                  redeemed_via_leave_request_id BIGINT,
                                  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMP
);

-- Holidays
CREATE TABLE holidays (
                          id BIGSERIAL PRIMARY KEY,
                          holiday_date DATE NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          country_code VARCHAR(2) NOT NULL,
                          is_optional BOOLEAN DEFAULT FALSE,
                          description VARCHAR(500),
                          created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMP,
                          UNIQUE (holiday_date, country_code)
);

-- On-call schedules
CREATE TABLE on_call_schedules (
                                   id BIGSERIAL PRIMARY KEY,
                                   primary_user_id BIGINT NOT NULL REFERENCES users(id),
                                   secondary_user_id BIGINT REFERENCES users(id),
                                   on_call_date DATE NOT NULL,
                                   primary_acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
                                   primary_acknowledged_at TIMESTAMP,
                                   secondary_acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
                                   secondary_acknowledged_at TIMESTAMP,
                                   comp_off_credited BOOLEAN NOT NULL DEFAULT FALSE,
                                   created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                   updated_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_leave_requests_user_id ON leave_requests(user_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_leave_requests_dates ON leave_requests(from_date, to_date);
CREATE INDEX idx_wfh_requests_user_id ON wfh_requests(user_id);
CREATE INDEX idx_wfh_requests_date ON wfh_requests(wfh_date);
CREATE INDEX idx_shift_schedules_user_date ON shift_schedules(user_id, shift_date);
CREATE INDEX idx_shift_schedules_date ON shift_schedules(shift_date);
CREATE INDEX idx_holidays_date ON holidays(holiday_date);
CREATE INDEX idx_holidays_country ON holidays(country_code);
CREATE INDEX idx_on_call_date ON on_call_schedules(on_call_date);
CREATE INDEX idx_comp_off_user ON comp_off_credits(user_id);
CREATE INDEX idx_comp_off_expiry ON comp_off_credits(expiry_date);