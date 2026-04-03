package com.finance.dashboard;

import com.finance.dto.response.DashboardSummaryResponse;
import com.finance.finance.FinancialRecordRepository;
import com.finance.finance.RecordType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getFullSummary(int recentCount) {
        BigDecimal totalIncome = recordRepository.sumByType(RecordType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(RecordType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);
        long totalRecords = recordRepository.countActive();

        Map<String, BigDecimal> categoryBreakdown = getCategoryBreakdown();
        List<DashboardSummaryResponse.MonthlyTrend> monthlyTrends = getMonthlyTrends(12);
        List<DashboardSummaryResponse.RecentActivity> recentActivity = getRecentActivity(recentCount);

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .totalRecords(totalRecords)
                .categoryBreakdown(categoryBreakdown)
                .monthlyTrends(monthlyTrends)
                .recentActivity(recentActivity)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCategoryBreakdown() {
        List<Object[]> results = recordRepository.getCategoryTotals();
        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        for (Object[] row : results) {
            String category = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            breakdown.put(category, total);
        }
        return breakdown;
    }

    @Transactional(readOnly = true)
    public List<DashboardSummaryResponse.MonthlyTrend> getMonthlyTrends(int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months).withDayOfMonth(1);
        List<Object[]> results = recordRepository.getMonthlyTrends(startDate);

        // Group by month → {income, expense}
        Map<String, DashboardSummaryResponse.MonthlyTrend> trendMap = new LinkedHashMap<>();
        for (Object[] row : results) {
            String month = (String) row[0];
            String type = row[1].toString();
            BigDecimal amount = (BigDecimal) row[2];

            trendMap.computeIfAbsent(month, m -> DashboardSummaryResponse.MonthlyTrend.builder()
                    .month(m)
                    .income(BigDecimal.ZERO)
                    .expenses(BigDecimal.ZERO)
                    .build());

            DashboardSummaryResponse.MonthlyTrend trend = trendMap.get(month);
            if ("INCOME".equals(type)) {
                trend.setIncome(amount);
            } else {
                trend.setExpenses(amount);
            }
        }

        return new ArrayList<>(trendMap.values());
    }

    @Transactional(readOnly = true)
    public List<DashboardSummaryResponse.RecentActivity> getRecentActivity(int count) {
        return recordRepository.findRecentActivity(PageRequest.of(0, count))
                .stream()
                .map(r -> DashboardSummaryResponse.RecentActivity.builder()
                        .id(r.getId().toString())
                        .type(r.getType().name())
                        .category(r.getCategory())
                        .amount(r.getAmount())
                        .date(r.getDate().toString())
                        .notes(r.getNotes())
                        .build())
                .toList();
    }
}
