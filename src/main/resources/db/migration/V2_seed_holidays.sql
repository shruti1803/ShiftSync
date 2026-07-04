-- V2: Seed holidays for 2025 (mock data)

-- India Public Holidays 2025
INSERT INTO holidays (holiday_date, name, country_code, is_optional, description) VALUES
                                                                                      ('2025-01-14', 'Makar Sankranti', 'IN', false, 'Harvest festival'),
                                                                                      ('2025-01-26', 'Republic Day', 'IN', false, 'National holiday'),
                                                                                      ('2025-03-14', 'Holi', 'IN', false, 'Festival of colours'),
                                                                                      ('2025-04-14', 'Dr. Ambedkar Jayanti', 'IN', false, 'National holiday'),
                                                                                      ('2025-04-18', 'Good Friday', 'IN', false, 'Christian holiday'),
                                                                                      ('2025-05-12', 'Buddha Purnima', 'IN', false, 'Religious holiday'),
                                                                                      ('2025-08-15', 'Independence Day', 'IN', false, 'National holiday'),
                                                                                      ('2025-08-27', 'Ganesh Chaturthi', 'IN', false, 'Religious festival'),
                                                                                      ('2025-10-02', 'Gandhi Jayanti', 'IN', false, 'National holiday'),
                                                                                      ('2025-10-02', 'Dussehra', 'IN', false, 'Religious festival'),
                                                                                      ('2025-10-20', 'Diwali', 'IN', false, 'Festival of lights'),
                                                                                      ('2025-11-05', 'Diwali Padwa', 'IN', false, 'Day after Diwali'),
                                                                                      ('2025-11-15', 'Guru Nanak Jayanti', 'IN', false, 'Religious holiday'),
                                                                                      ('2025-12-25', 'Christmas', 'IN', false, 'Christian holiday')
    ON CONFLICT (holiday_date, country_code) DO NOTHING;

-- US Federal Holidays 2025
INSERT INTO holidays (holiday_date, name, country_code, is_optional, description) VALUES
                                                                                      ('2025-01-01', 'New Year''s Day', 'US', false, 'US Federal Holiday'),
                                                                                      ('2025-01-20', 'Martin Luther King Jr. Day', 'US', false, 'US Federal Holiday'),
                                                                                      ('2025-02-17', 'Presidents'' Day', 'US', false, 'US Federal Holiday'),
                                                                                      ('2025-05-26', 'Memorial Day', 'US', false, 'US Federal Holiday'),
                                                                                      ('2025-06-19', 'Juneteenth', 'US', false, 'US Federal Holiday'),
                                                                                      ('2025-07-04', 'Independence Day', 'US', false, 'US Federal Holiday'),
                                                                                      ('2025-09-01', 'Labor Day', 'US', false, 'US Federal Holiday'),
                                                                                      ('2025-10-13', 'Columbus Day', 'US', false, 'US Federal Holiday'),
                                                                                      ('2025-11-11', 'Veterans Day', 'US', false, 'US Federal Holiday'),
                                                                                      ('2025-11-27', 'Thanksgiving Day', 'US', false, 'US Federal Holiday'),
                                                                                      ('2025-12-25', 'Christmas Day', 'US', false, 'US Federal Holiday')
    ON CONFLICT (holiday_date, country_code) DO NOTHING;