package com.finance.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, UUID> {

    // Single record - exclude soft-deleted
    Optional<FinancialRecord> findByIdAndDeletedFalse(UUID id);

    // Filterable list with pagination
    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE r.deleted = false
              AND (:type IS NULL OR r.type = :type)
              AND (:category IS NULL OR LOWER(r.category) = LOWER(:category))
              AND (:startDate IS NULL OR r.date >= :startDate)
              AND (:endDate IS NULL OR r.date <= :endDate)
            """)
    Page<FinancialRecord> findWithFilters(
            @Param("type") RecordType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // Dashboard aggregations
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.deleted = false AND r.type = :type")
    BigDecimal sumByType(@Param("type") RecordType type);

    @Query("SELECT COUNT(r) FROM FinancialRecord r WHERE r.deleted = false")
    long countActive();

    @Query("""
            SELECT r.category, COALESCE(SUM(r.amount), 0)
            FROM FinancialRecord r
            WHERE r.deleted = false
            GROUP BY r.category
            ORDER BY SUM(r.amount) DESC
            """)
    List<Object[]> getCategoryTotals();

    @Query("""
            SELECT FUNCTION('FORMATDATETIME', r.date, 'yyyy-MM') as month,
                   r.type,
                   COALESCE(SUM(r.amount), 0)
            FROM FinancialRecord r
            WHERE r.deleted = false
              AND r.date >= :startDate
            GROUP BY FUNCTION('FORMATDATETIME', r.date, 'yyyy-MM'), r.type
            ORDER BY month DESC
            """)
    List<Object[]> getMonthlyTrends(@Param("startDate") LocalDate startDate);

    // Recent activity
    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false ORDER BY r.date DESC, r.createdAt DESC")
    List<FinancialRecord> findRecentActivity(Pageable pageable);
}
