package com.finance.config;

import com.finance.finance.FinancialRecord;
import com.finance.finance.FinancialRecordRepository;
import com.finance.finance.RecordType;
import com.finance.user.Role;
import com.finance.user.User;
import com.finance.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedUsers();
        seedFinancialRecords();
    }

    private void seedUsers() {
        if (userRepository.existsByEmail("admin@finance.com")) {
            return; // already seeded
        }

        User admin = User.builder()
                .name("System Admin")
                .email("admin@finance.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .active(true)
                .build();

        User analyst = User.builder()
                .name("Finance Analyst")
                .email("analyst@finance.com")
                .passwordHash(passwordEncoder.encode("Analyst@123"))
                .role(Role.ANALYST)
                .active(true)
                .build();

        User viewer = User.builder()
                .name("Dashboard Viewer")
                .email("viewer@finance.com")
                .passwordHash(passwordEncoder.encode("Viewer@123"))
                .role(Role.VIEWER)
                .active(true)
                .build();

        userRepository.save(admin);
        userRepository.save(analyst);
        userRepository.save(viewer);

        log.info("=== SEEDED USERS ===");
        log.info("ADMIN   -> admin@finance.com   / Admin@123");
        log.info("ANALYST -> analyst@finance.com / Analyst@123");
        log.info("VIEWER  -> viewer@finance.com  / Viewer@123");

        seedRecordsForAdmin(admin);
    }

    private void seedFinancialRecords() {
        if (recordRepository.count() > 0) {
            return; // already seeded
        }
    }

    private void seedRecordsForAdmin(User admin) {
        LocalDate now = LocalDate.now();

        FinancialRecord[] records = {
            makeRecord(admin, new BigDecimal("85000.00"), RecordType.INCOME, "Salary", now.minusDays(5), "Monthly salary - April"),
            makeRecord(admin, new BigDecimal("12000.00"), RecordType.INCOME, "Freelance", now.minusDays(10), "Web project payment"),
            makeRecord(admin, new BigDecimal("5000.00"), RecordType.INCOME, "Investments", now.minusDays(15), "Dividend income"),
            makeRecord(admin, new BigDecimal("25000.00"), RecordType.EXPENSE, "Rent", now.minusDays(3), "Monthly apartment rent"),
            makeRecord(admin, new BigDecimal("8500.00"), RecordType.EXPENSE, "Groceries", now.minusDays(7), "Monthly groceries"),
            makeRecord(admin, new BigDecimal("3200.00"), RecordType.EXPENSE, "Utilities", now.minusDays(12), "Electricity and water"),
            makeRecord(admin, new BigDecimal("2100.00"), RecordType.EXPENSE, "Transport", now.minusDays(8), "Fuel and commute"),
            makeRecord(admin, new BigDecimal("1500.00"), RecordType.EXPENSE, "Entertainment", now.minusDays(20), "Dining and movies"),
            // Previous month
            makeRecord(admin, new BigDecimal("85000.00"), RecordType.INCOME, "Salary", now.minusMonths(1).minusDays(5), "Monthly salary - March"),
            makeRecord(admin, new BigDecimal("25000.00"), RecordType.EXPENSE, "Rent", now.minusMonths(1).minusDays(3), "Monthly apartment rent"),
            makeRecord(admin, new BigDecimal("7800.00"), RecordType.EXPENSE, "Groceries", now.minusMonths(1).minusDays(7), "Monthly groceries"),
            makeRecord(admin, new BigDecimal("15000.00"), RecordType.INCOME, "Bonus", now.minusMonths(1).minusDays(10), "Q1 performance bonus"),
        };

        for (FinancialRecord record : records) {
            recordRepository.save(record);
        }

        log.info("Seeded {} sample financial records", records.length);
    }

    private FinancialRecord makeRecord(User admin, BigDecimal amount, RecordType type,
                                        String category, LocalDate date, String notes) {
        return FinancialRecord.builder()
                .amount(amount)
                .type(type)
                .category(category)
                .date(date)
                .notes(notes)
                .createdBy(admin)
                .build();
    }
}
