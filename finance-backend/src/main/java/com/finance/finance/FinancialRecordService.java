package com.finance.finance;

import com.finance.dto.request.FinancialRecordRequest;
import com.finance.dto.response.FinancialRecordResponse;
import com.finance.exception.ResourceNotFoundException;
import com.finance.user.User;
import com.finance.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    @Transactional
    public FinancialRecordResponse createRecord(FinancialRecordRequest request, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + creatorEmail));

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(creator)
                .build();

        recordRepository.save(record);
        log.info("Record created: {} {} by {}", record.getType(), record.getAmount(), creatorEmail);
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> getRecords(
            RecordType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        return recordRepository.findWithFilters(type, category, startDate, endDate, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecordById(UUID id) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));
        return toResponse(record);
    }

    @Transactional
    public FinancialRecordResponse updateRecord(UUID id, FinancialRecordRequest request) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());

        recordRepository.save(record);
        log.info("Record updated: {}", id);
        return toResponse(record);
    }

    @Transactional
    public void softDeleteRecord(UUID id) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));

        record.setDeleted(true);
        record.setDeletedAt(LocalDateTime.now());
        recordRepository.save(record);
        log.info("Record soft-deleted: {}", id);
    }

    private FinancialRecordResponse toResponse(FinancialRecord record) {
        return FinancialRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .notes(record.getNotes())
                .createdById(record.getCreatedBy().getId())
                .createdByEmail(record.getCreatedBy().getEmail())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
