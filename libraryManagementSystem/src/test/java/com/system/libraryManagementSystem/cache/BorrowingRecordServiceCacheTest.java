package com.system.libraryManagementSystem.cache;

import com.system.libraryManagementSystem.exception.BorrowingRecordNotFound;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.BorrowingRecord;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.BorrowingRecordRepository;
import com.system.libraryManagementSystem.service.BorrowingRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@EnableCaching
@ActiveProfiles("test")
@SpringBootTest
class BorrowingRecordServiceCacheTest {

    @Autowired
    private BorrowingRecordService borrowingRecordService;

    @MockitoBean
    private BorrowingRecordRepository borrowingRecordRepository;

    @Autowired
    private CacheManager cacheManager;

    private Book book;
    private Author author;
    private BorrowingRecord borrowingRecord;
    private Member member;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("borrowing_records").clear();
        author = Author.builder()
                .id(1L)
                .name("J.K. Rowling")
                .biography("Famous author")
                .build();
        book = Book.builder()
                .id(1L)
                .title("Harry Potter")
                .genre("Fantasy")
                .publicationYear(1997)
                .author(author)
                .build();
        member = Member.builder()
                .id(1L)
                .name("Member")
                .email("member@gmail.com")
                .password("12345member")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        borrowingRecord = BorrowingRecord.builder()
                .id(1L)
                .book(book)
                .member(member)
                .borrowDate(LocalDateTime.now())
                .isApproved(false)
                .build();

        // Mock repository behavior
        when(borrowingRecordRepository.findById(1L)).thenReturn(Optional.of(borrowingRecord));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void getBorrowingRecordById_ShouldUseCache_AfterFirstCall() {
        assertNull(cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));

        BorrowingRecord firstCall = borrowingRecordService.getBorrowingRecordById(borrowingRecord.getId());
        assertNotNull(cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        assertEquals(firstCall, cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        verify(borrowingRecordRepository, times(1)).findById(borrowingRecord.getId());

        BorrowingRecord secondCall = borrowingRecordService.getBorrowingRecordById(borrowingRecord.getId());
        assertNotNull(cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        assertEquals(secondCall, cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        assertEquals(secondCall, firstCall);
        verify(borrowingRecordRepository, times(1)).findById(borrowingRecord.getId());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void updateBorrowingRecord_ShouldUpdateCache() {
        assertNull(cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));

        BorrowingRecord firstCall = borrowingRecordService.getBorrowingRecordById(borrowingRecord.getId());
        assertNotNull(cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        assertEquals(firstCall, cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        verify(borrowingRecordRepository, times(1)).findById(borrowingRecord.getId());

        //update record, must update cache too
        BorrowingRecord newBorrowingRecord = BorrowingRecord.builder()
                .id(borrowingRecord.getId())
                .book(book)
                .member(member)
                .borrowDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().plusMonths(2))
                .isApproved(true)
                .build();
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(newBorrowingRecord);

        BorrowingRecord updatedRecord = borrowingRecordService.updateBorrowingRecord(newBorrowingRecord.getId(), newBorrowingRecord);
        assertNotNull(cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        assertEquals(updatedRecord, cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        assertNotEquals(firstCall, cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));     //first call must be different
        verify(borrowingRecordRepository, times(2)).findById(borrowingRecord.getId());

        //second call, check if it is getting from the cache and is updated
        when(borrowingRecordRepository.findById(updatedRecord.getId())).thenReturn(Optional.of(updatedRecord));

        BorrowingRecord secondCall = borrowingRecordService.getBorrowingRecordById(borrowingRecord.getId());
        assertNotNull(cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        assertEquals(secondCall, cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        assertEquals(updatedRecord, secondCall);
        assertNotEquals(firstCall, secondCall);
        verify(borrowingRecordRepository, times(2)).findById(borrowingRecord.getId());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void deleteBorrowingRecordById_ShouldEvictCache() {
        assertNull(cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));

        BorrowingRecord firstCall = borrowingRecordService.getBorrowingRecordById(borrowingRecord.getId());
        assertNotNull(cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        assertEquals(firstCall, cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        verify(borrowingRecordRepository, times(1)).findById(borrowingRecord.getId());

        //delete data must delete the cache too
        doNothing().when(borrowingRecordRepository).deleteById(borrowingRecord.getId());
        borrowingRecordService.deleteBorrowingRecordById(borrowingRecord.getId());

        assertNull(cacheManager.getCache("borrowing_records").get(borrowingRecord.getId(), BorrowingRecord.class));
        verify(borrowingRecordRepository,times(1)).findById(borrowingRecord.getId());
        verify(borrowingRecordRepository,times(1)).deleteById(borrowingRecord.getId());

        //repository must be activated because cache is deleted
        when(borrowingRecordRepository.findById(borrowingRecord.getId())).thenReturn(Optional.empty());
        assertThrows(BorrowingRecordNotFound.class, () -> borrowingRecordService.getBorrowingRecordById(borrowingRecord.getId()));
        verify(borrowingRecordRepository,times(2)).findById(borrowingRecord.getId());
    }
}