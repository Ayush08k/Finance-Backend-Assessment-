package com.finance;

import com.finance.dto.request.FinancialRecordRequest;
import com.finance.dto.response.FinancialRecordResponse;
import com.finance.exception.ResourceNotFoundException;
import com.finance.finance.FinancialRecord;
import com.finance.finance.FinancialRecordRepository;
import com.finance.finance.FinancialRecordService;
import com.finance.finance.RecordType;
import com.finance.user.Role;
import com.finance.user.User;
import com.finance.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialRecordService Unit Tests")
class FinancialRecordServiceTest {

    @Mock private FinancialRecordRepository recordRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private FinancialRecordService recordService;

    private User adminUser;
    private FinancialRecord sampleRecord;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(UUID.randomUUID())
                .name("Admin")
                .email("admin@finance.com")
                .role(Role.ADMIN)
                .active(true)
                .build();

        sampleRecord = FinancialRecord.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("5000.00"))
                .type(RecordType.INCOME)
                .category("Salary")
                .date(LocalDate.now())
                .notes("Monthly salary")
                .createdBy(adminUser)
                .deleted(false)
                .build();
    }

    @Test
    @DisplayName("Create record - success")
    void createRecord_success() {
        FinancialRecordRequest request = new FinancialRecordRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setType(RecordType.INCOME);
        request.setCategory("Salary");
        request.setDate(LocalDate.now());
        request.setNotes("Monthly salary");

        when(userRepository.findByEmail("admin@finance.com")).thenReturn(Optional.of(adminUser));
        when(recordRepository.save(any())).thenReturn(sampleRecord);

        FinancialRecordResponse response = recordService.createRecord(request, "admin@finance.com");

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo("5000.00");
        assertThat(response.getType()).isEqualTo(RecordType.INCOME);
        assertThat(response.getCategory()).isEqualTo("Salary");
    }

    @Test
    @DisplayName("Create record - unknown user throws ResourceNotFoundException")
    void createRecord_unknownUser_throwsException() {
        FinancialRecordRequest request = new FinancialRecordRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setType(RecordType.EXPENSE);
        request.setCategory("Food");
        request.setDate(LocalDate.now());

        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.createRecord(request, "ghost@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Get record by ID - not found throws ResourceNotFoundException")
    void getRecordById_notFound_throwsException() {
        UUID randomId = UUID.randomUUID();
        when(recordRepository.findByIdAndDeletedFalse(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getRecordById(randomId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Soft delete - sets deleted=true and deletedAt timestamp")
    void softDelete_setsDeletedFlag() {
        when(recordRepository.findByIdAndDeletedFalse(sampleRecord.getId()))
                .thenReturn(Optional.of(sampleRecord));
        when(recordRepository.save(any())).thenReturn(sampleRecord);

        recordService.softDeleteRecord(sampleRecord.getId());

        verify(recordRepository).save(argThat(r -> r.isDeleted() && r.getDeletedAt() != null));
    }

    @Test
    @DisplayName("Soft delete - already-deleted record throws ResourceNotFoundException")
    void softDelete_alreadyDeleted_throwsException() {
        UUID id = UUID.randomUUID();
        when(recordRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.softDeleteRecord(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
