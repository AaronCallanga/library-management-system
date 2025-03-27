package com.system.libraryManagementSystem.service;

import com.system.libraryManagementSystem.exception.BorrowingRecordNotFound;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.BorrowingRecord;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.BorrowingRecordRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingRecordServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BorrowingRecordRepository borrowingRecordRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BorrowingRecordService borrowingRecordService;  // The class containing isMemberOwnerOfTheRecord

    private BorrowingRecord record1;
    private BorrowingRecord record2;
    private Member member;
    private PageRequest  pageRequest;
    private List<BorrowingRecord> records;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        record1  = BorrowingRecord.builder()
                .id(1L)
                .member(member)
                .build();

        record2  = BorrowingRecord.builder()
                .id(2L)
                .member(member)
                .build();
        pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.fromString("ASC"), "id"));
        records = List.of(record1, record2);
    }

    @Test
    void getAllBorrowingRecords_ShouldReturnPagedOfBorrowingRecords() {
        Page<BorrowingRecord> expectedPage = new PageImpl<>(records, pageRequest, records.size());

        when(borrowingRecordRepository.findAll(pageRequest)).thenReturn(expectedPage);

        // Act
        Page<BorrowingRecord> result = borrowingRecordService.getAllBorrowingRecords(0, 10, "ASC", "id");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(2L, result.getContent().get(1).getId());
        assertEquals("id: ASC", result.getPageable().getSort().toString());

        verify(borrowingRecordRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getAllBorrowingRecords_WhenRecordsIsEmpty_ShouldReturnPagedOfBorrowingRecords() {
        List<BorrowingRecord> emptyPage = new ArrayList<>();
        Page<BorrowingRecord> expectedPage = new PageImpl<>(emptyPage, pageRequest, emptyPage.size());

        when(borrowingRecordRepository.findAll(pageRequest)).thenReturn(expectedPage);

        // Act
        Page<BorrowingRecord> result = borrowingRecordService.getAllBorrowingRecords(0, 10, "ASC", "id");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals("id: ASC", result.getPageable().getSort().toString());

        verify(borrowingRecordRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getBorrowingRecordById_WhenIdExist_ShouldReturnBorrowingRecord() {
        when(borrowingRecordRepository.findById(record1.getId())).thenReturn(Optional.of(record1));

        BorrowingRecord result = borrowingRecordService.getBorrowingRecordById(record1.getId());

        assertNotNull(result);
        assertEquals(record1.getId(), result.getId());
        verify(borrowingRecordRepository, times(1)).findById(1L);
    }

    @Test
    void getBorrowingRecordById_WhenIdDoesNotExist_ShouldThrowException() {
        when(borrowingRecordRepository.findById(99L)).thenReturn(Optional.empty());

        BorrowingRecordNotFound result = assertThrows(BorrowingRecordNotFound.class, () -> borrowingRecordService.getBorrowingRecordById(99L));

        assertNotNull(result);
        assertEquals("Record not found with the id: 99", result.getMessage());
        verify(borrowingRecordRepository, times(1)).findById(99L);
    }

    @Test
    void saveNewBorrowingRecord() {
        BorrowingRecord newBorrowingRecord = BorrowingRecord.builder()
                .id(3L)
                .member(member)
                .build();

        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(newBorrowingRecord);

        // Act
        BorrowingRecord result = borrowingRecordService.saveNewBorrowingRecord(newBorrowingRecord);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        verify(borrowingRecordRepository, times(1)).save(newBorrowingRecord);
    }

    @Test
    void updateBorrowingRecord_WhenRecordIsChanged_ShouldUpdateSuccessfully() {
        Book book = Book.builder()
                .title("New Book")
                .build();

        BorrowingRecord updatedRecord1  = BorrowingRecord.builder()
                .id(record1.getId())
                .member(record1.getMember())
                .book(book)
                .borrowDate(LocalDate.of(2024, 3, 5).atStartOfDay())
                .build();

        when(borrowingRecordRepository.findById(record1.getId())).thenReturn(Optional.of(record1));
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(updatedRecord1);

        BorrowingRecord result = borrowingRecordService.updateBorrowingRecord(record1.getId(), updatedRecord1);

        assertNotNull(result);
        assertEquals("New Book", result.getBook().getTitle());
        assertEquals("test@example.com", result.getMember().getEmail());
        assertEquals(LocalDate.of(2024, 3, 5).atStartOfDay(), result.getBorrowDate());

        verify(borrowingRecordRepository, times(1)).findById(record1.getId());
        verify(borrowingRecordRepository, times(1)).save(record1);
    }

    @Test
    void updateBook_WithTheSameData_ShouldAvoidUnnecessaryUpdates() {
        BorrowingRecord updatedRecord1  = BorrowingRecord.builder()
                .id(record1.getId())
                .member(record1.getMember())
                .build();

        when(borrowingRecordRepository.findById(record1.getId())).thenReturn(Optional.of(record1));

        BorrowingRecord result = borrowingRecordService.updateBorrowingRecord(record1.getId(), updatedRecord1);

        assertNotNull(result);
        assertEquals(result, updatedRecord1);
        assertEquals(record1, updatedRecord1);
        verify(borrowingRecordRepository, times(1)).findById(record1.getId());
        verify(borrowingRecordRepository, never()).save(updatedRecord1);
    }

    @Test
    void updateBorrowingRecord_WhenRecordNotFound_ShouldThrowException() {
        BorrowingRecord updatedRecord = new BorrowingRecord();

        when(borrowingRecordRepository.findById(99L)).thenReturn(Optional.empty());

        BorrowingRecordNotFound result = assertThrows(BorrowingRecordNotFound.class, () -> borrowingRecordService.getBorrowingRecordById(99L));

        assertNotNull(result);
        assertEquals("Record not found with the id: 99", result.getMessage());
        verify(borrowingRecordRepository, times(1)).findById(99L);
        verify(borrowingRecordRepository, never()).save(any(BorrowingRecord.class));
    }

    @Test
    void deleteBorrowingRecordById() {
        doNothing().when(borrowingRecordRepository).deleteById(record1.getId());

        borrowingRecordService.deleteBorrowingRecordById(record1.getId());

        verify(borrowingRecordRepository, times(1)).deleteById(record1.getId());
    }

    @Test
    void deleteBorrowingRecordById_ShouldNotThrowException_WhenRecordDoesNotExist() {
        doNothing().when(borrowingRecordRepository).deleteById(99L);

        assertDoesNotThrow(() -> borrowingRecordService.deleteBorrowingRecordById(99L));

        verify(borrowingRecordRepository, times(1)).deleteById(99L);
    }

    @Test
    void approveBorrowRequest_WhenIdExist_ShouldSetApproveToTrue() {
        when(borrowingRecordRepository.findById(record1.getId())).thenReturn(Optional.of(record1));
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(record1);

        borrowingRecordService.approveBorrowRequest(record1.getId());

        BorrowingRecord result = borrowingRecordService.getBorrowingRecordById(record1.getId());

        assertNotNull(result);
        assertTrue(record1.isApproved());
        verify(borrowingRecordRepository, times(2)).findById(record1.getId());
        verify(borrowingRecordRepository, times(1)).save(record1);
    }

    @Test
    void approveBorrowRequest_WhenIdDoesNotExist_ShouldSetApproveToTrue() {
        when(borrowingRecordRepository.findById(99L)).thenReturn(Optional.empty());

        BorrowingRecordNotFound result = assertThrows(BorrowingRecordNotFound.class, () -> borrowingRecordService.approveBorrowRequest(99L));

        assertNotNull(result);
        assertEquals("Record not found with the id: 99", result.getMessage());
        verify(borrowingRecordRepository, times(1)).findById(99L);
        verify(borrowingRecordRepository, never()).save(record1);
    }

    @Test
    void testIsMemberOwnerOfTheRecord_ReturnsTrue_WhenEmailMatches() {
        when(borrowingRecordRepository.findById(1L)).thenReturn(Optional.of(record1));
        when(authentication.getName()).thenReturn("test@example.com");

        boolean result = borrowingRecordService.isMemberOwnerOfTheRecord(1L, authentication);

        assertTrue(result);
    }

    @Test
    void testIsMemberOwnerOfTheRecord_ReturnsFalse_WhenEmailDoesNotMatch() {
        when(borrowingRecordRepository.findById(1L)).thenReturn(Optional.of(record1));
        when(authentication.getName()).thenReturn("wrong@example.com");

        boolean result = borrowingRecordService.isMemberOwnerOfTheRecord(1L, authentication);

        assertFalse(result);
    }

    @Test
    void testIsMemberOwnerOfTheRecord_ReturnsFalse_WhenRecordNotFound() {
        when(borrowingRecordRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = borrowingRecordService.isMemberOwnerOfTheRecord(1L, authentication);

        assertFalse(result);
    }
}