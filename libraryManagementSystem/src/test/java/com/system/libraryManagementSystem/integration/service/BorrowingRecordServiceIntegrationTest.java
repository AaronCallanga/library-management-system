package com.system.libraryManagementSystem.integration.service;

import com.system.libraryManagementSystem.exception.BorrowingRecordNotFound;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.BorrowingRecord;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.repository.BorrowingRecordRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import com.system.libraryManagementSystem.service.BorrowingRecordService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class BorrowingRecordServiceIntegrationTest {       //integration tests focus only on database queries and retrieval operations

    @Autowired
    private BorrowingRecordService borrowingRecordService;

    @Autowired
    private BorrowingRecordRepository borrowingRecordRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member member;
    private Author author;
    private Book book;
    private BorrowingRecord borrowingRecord1;
    private BorrowingRecord borrowingRecord2;

    @BeforeEach
    void setUp() {
        author = Author.builder()
                .name("J.K. Rowling")
                .biography("Famous author")
                .build();
        authorRepository.save(author);

        book = Book.builder()
                .title("Harry Potter")
                .genre("Fantasy")
                .publicationYear(1997)
                .author(author)
                .build();
        bookRepository.save(book);

        member = Member.builder()
                .name("Member")
                .email("member@gmail.com")
                .password(passwordEncoder.encode("12345member"))
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        memberRepository.save(member);

        borrowingRecord1 = BorrowingRecord.builder()
                .member(member)
                .book(book)
                .borrowDate(LocalDate.of(2025, 2, 13).atTime(12, 0, 0))
                .returnDate(LocalDate.of(2025, 2, 15).atTime(12, 0, 0))
                .isApproved(true)
                .build();
        borrowingRecord2 = BorrowingRecord.builder()
                .member(member)
                .book(book)
                .borrowDate(LocalDate.of(2025, 2, 13).atTime(18, 30, 0))  // "13-Feb-2025 18:30:00"
                .returnDate(LocalDate.of(2025, 2, 15).atTime(18, 30, 0))
                .isApproved(true)
                .build();
        borrowingRecordRepository.saveAll(List.of(borrowingRecord1, borrowingRecord2));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetAllBorrowingRecords() {
        Page<BorrowingRecord> result = borrowingRecordService.getAllBorrowingRecords(0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }


    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"member@gmail.com", "mEmBeR@GmAiL.cOm", "MEMBER@GMAIL.COM"})
    void testGetBorrowingRecordByMemberEmail_WhenEmailFullyMatchIgnoringCase_ShouldReturnPageOfRecords(String email) {
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByMemberEmail(email, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(br -> br.getMember().getEmail().equalsIgnoreCase(email)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"memb@gmail", "member"})
    void testGetBorrowingRecordByMemberEmail_WhenEmailDoesNotMatch_ShouldReturnEmptyPage(String email) {
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByMemberEmail(email, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"Harry Potter", "HaRrY PoTteR", "HARRY potter"})
    void testGetBorrowingRecordByBookTitle_WhenTitleFullyMatchIgnoringCase_ShouldReturnPageOfRecords(String title) {
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByBookTitle(title, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(br -> br.getBook().getTitle().equalsIgnoreCase(title)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"Potter", "RrY PoTt", "HARRY"})
    void testGetBorrowingRecordByBookTitle_WhenTitlePartiallyMatchIgnoringCase_ShouldReturnPageOfRecords(String title) {
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByBookTitle(title, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(br -> br.getBook().getTitle().toLowerCase().contains(title.toLowerCase())));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"non-matching title"})
    void testGetBorrowingRecordByBookTitle_WhenTitleDoesNotMatch_ShouldReturnEmptyPage(String title) {
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByBookTitle(title, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"Member", "MeMbEr", "MEMber"})
    void testGetBorrowingRecordByMemberName_WhenNameFullyMatchIgnoringCase_ShouldReturnPageOfRecords(String name) {
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByMemberName(name, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(br -> br.getMember().getName().equalsIgnoreCase(name)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"Mem", "mbe", "BER"})
    void testGetBorrowingRecordByMemberName_WhenNamePartiallyMatchIgnoringCase_ShouldReturnPageOfRecords(String name) {
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByMemberName(name, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(br -> br.getMember().getName().toLowerCase().contains(name.toLowerCase())));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"non-matching name"})
    void testGetBorrowingRecordByBookTitle_WhenNameDoesNotMatch_ShouldReturnEmptyPage(String name) {
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByMemberName(name, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetBorrowingRecordByBorrowDate_WhenDateAndTimeIsProvided_ShouldReturnPageOfRecords() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 2, 13, 12, 0, 0); // "13-Feb-2025 12:00:00"
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByBorrowDate(dateTime, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(1, result.getTotalElements()); // Only exact match should return
        assertTrue(result.stream().allMatch(br -> br.getBorrowDate().equals(dateTime)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetBorrowingRecordByBorrowDate_WhenDateIsOnlyProvided_ShouldReturnPageOfRecords() {
        LocalDate date = LocalDate.of(2025, 2, 13); // "13-Feb-2025"
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByBorrowDate(date.atStartOfDay(), 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(2, result.getTotalElements()); // Both records on this day should be returned
        assertTrue(result.stream().allMatch(br -> br.getBorrowDate().toLocalDate().equals(date)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetBorrowingRecordByBorrowDate_WhenDateDoesNotMatch_ShouldReturnPageOfRecords() {
        LocalDate date = LocalDate.of(1999, 2, 13);
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByBorrowDate(date.atStartOfDay(), 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(0, result.getTotalElements());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetBorrowingRecordByBorrowDate_WhenDateAndTimeDoesNotMatch_ShouldReturnPageOfRecords() {
        LocalDateTime dateTime = LocalDateTime.of(1999, 2, 13, 12, 0, 0);
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByBorrowDate(dateTime, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(0, result.getTotalElements());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetBorrowingRecordByReturnDate_WhenDateAndTimeIsProvided_ShouldReturnPageOfRecords() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 2, 15, 12, 0, 0); // "15-Feb-2025 12:00:00"
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByReturnDate(dateTime, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(1, result.getTotalElements()); // Only exact match should return
        assertTrue(result.stream().allMatch(br -> br.getReturnDate().equals(dateTime)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetBorrowingRecordByReturnDate_WhenDateIsOnlyProvided_ShouldReturnPageOfRecords() {
        LocalDate date =  LocalDate.of(2025, 2, 15); // "15-Feb-2025"
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByReturnDate(date.atStartOfDay(), 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(2, result.getTotalElements()); // Both records on this day should be returned
        assertTrue(result.stream().allMatch(br -> br.getReturnDate().toLocalDate().equals(date)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetBorrowingRecordByReturnDate_WhenDateDoesNotMatch_ShouldReturnPageOfRecords() {
        LocalDate date = LocalDate.of(1999, 2, 13);
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByReturnDate(date.atStartOfDay(), 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(0, result.getTotalElements());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetBorrowingRecordByReturnDate_WhenDateAndTimeDoesNotMatch_ShouldReturnPageOfRecords() {
        LocalDateTime dateTime = LocalDateTime.of(1999, 2, 13, 12, 0, 0);
        Page<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordByReturnDate(dateTime, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(0, result.getTotalElements());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testApproveBorrowRequest_WhenRecordExists_ShouldSetApprovedTrue() {
        Long recordId = borrowingRecord1.getId();

        borrowingRecordService.approveBorrowRequest(recordId);

        BorrowingRecord updatedRecord = borrowingRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found after update"));

        assertTrue(updatedRecord.isApproved(), "The borrowing record should be approved");
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testApproveBorrowRequest_WhenRecordDoesNotExist_ShouldThrowException() {
        Long invalidId = 999L;

        Exception exception = assertThrows(BorrowingRecordNotFound.class, () ->
                borrowingRecordService.approveBorrowRequest(invalidId)
        );

        assertEquals("Record not found with the id: 999", exception.getMessage());
    }
}