package com.system.libraryManagementSystem.integration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.libraryManagementSystem.dto.BorrowingRecordDTO;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.BorrowingRecord;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.repository.BorrowingRecordRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Transactional
class BorrowingRecordControllerIntegrationTest {

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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                .isApproved(false)
                .build();
        borrowingRecord2 = BorrowingRecord.builder()
                .member(member)
                .book(book)
                .borrowDate(LocalDate.of(2025, 2, 13).atTime(18, 30, 0))
                .returnDate(LocalDate.of(2025, 2, 15).atTime(18, 30, 0))
                .isApproved(true)
                .build();
        borrowingRecordRepository.saveAll(List.of(borrowingRecord1, borrowingRecord2));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testGetAllBorrowingRecords_WhenRecordsExist_ShouldReturnPageOfRecords() throws Exception {
        mockMvc.perform(get("/borrowing-record")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].memberName").value(borrowingRecord1.getMember().getName()))
                .andExpect(jsonPath("$.content[1].memberName").value(borrowingRecord2.getMember().getName()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testGetAllBorrowingRecords_WhenRecordsDoesNotExist_ShouldReturnEmptyPage() throws Exception {
        borrowingRecordRepository.deleteAll();

        mockMvc.perform(get("/borrowing-record")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testGetBorrowingRecordById_WhenRecordExist_ShouldReturnRecords() throws Exception {
        mockMvc.perform(get("/borrowing-record/{id}", borrowingRecord1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(borrowingRecord1.getId()))
                .andExpect(jsonPath("$.memberEmail").value(borrowingRecord1.getMember().getEmail()))
                .andExpect(jsonPath("$.bookTitle").value(borrowingRecord1.getBook().getTitle()))
                .andExpect(jsonPath("$.approved").value(borrowingRecord1.isApproved()));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testGetBorrowingRecordById_WhenRecordDoesNotExist_ShouldThrowException() throws Exception {
        mockMvc.perform(get("/borrowing-record/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("BORROWING RECORD NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Record not found with the id: 99"));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void saveNewBorrowingRecord_ShouldReturnCreatedRecord() throws Exception {
        BorrowingRecordDTO borrowingRecordDTO = BorrowingRecordDTO.builder()
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/borrowing-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowingRecordDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookTitle").value(borrowingRecordDTO.getBookTitle()))
                .andExpect(jsonPath("$.memberEmail").value(borrowingRecordDTO.getMemberEmail()));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void updateBorrowingRecordDTO_ShouldReturnUpdatedRecord() throws Exception {
        BorrowingRecordDTO updatedRecordDTO = BorrowingRecordDTO.builder()
                .recordId(borrowingRecord1.getId())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDate.of(2025, 2, 13).atTime(12, 0, 0))
                .returnDate(LocalDate.of(2025, 3, 13).atTime(12, 0, 0))
                .isApproved(false)
                .build();

        mockMvc.perform(put("/borrowing-record/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRecordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(borrowingRecord1.getId()))
                .andExpect(jsonPath("$.returnDate").value("13-Mar-2025 12:00:00"))
                .andExpect(jsonPath("$.approved").value(false));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void deleteBorrowingRecordById_ShouldReturnStatusNoContent() throws Exception {
        mockMvc.perform(delete("/borrowing-record/{id}", borrowingRecord1.getId()))
                .andExpect(status().isNoContent());
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void getOwnBorrowingRecord() throws Exception {
        mockMvc.perform(get("/borrowing-record/own")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].memberName").value(borrowingRecord1.getMember().getName()))
                .andExpect(jsonPath("$.content[1].memberName").value(borrowingRecord2.getMember().getName()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void sendOwnBorrowingRequest() throws Exception {
        BorrowingRecordDTO borrowingRecordDTO = BorrowingRecordDTO.builder()
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/borrowing-record/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowingRecordDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookTitle").value(borrowingRecordDTO.getBookTitle()))
                .andExpect(jsonPath("$.memberEmail").value(borrowingRecordDTO.getMemberEmail()));

        mockMvc.perform(get("/borrowing-record/own")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].memberName").value(borrowingRecord1.getMember().getName()))
                .andExpect(jsonPath("$.content[1].memberName").value(borrowingRecord2.getMember().getName()))
                .andExpect(jsonPath("$.content[2].memberEmail").value(borrowingRecordDTO.getMemberEmail()))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void updateOwnBorrowingRecordDTO() throws Exception {
        BorrowingRecordDTO updatedRecordDTO = BorrowingRecordDTO.builder()
                .recordId(borrowingRecord1.getId())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDate.of(2025, 2, 13).atTime(12, 0, 0))
                .returnDate(LocalDate.of(2025, 3, 13).atTime(12, 0, 0))
                .build();

        mockMvc.perform(put("/borrowing-record/update/own")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRecordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(borrowingRecord1.getId()))
                .andExpect(jsonPath("$.returnDate").value("13-Mar-2025 12:00:00"))
                .andExpect(jsonPath("$.approved").value(false));

        mockMvc.perform(get("/borrowing-record/own")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].memberName").value(borrowingRecord1.getMember().getName()))
                .andExpect(jsonPath("$.content[1].memberEmail").value(updatedRecordDTO.getMemberEmail()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.content[0].returnDate").value("13-Mar-2025 12:00:00"))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void deleteOwnBorrowingRecordById() throws Exception {
        mockMvc.perform(delete("/borrowing-record/own/{id}", borrowingRecord1.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/borrowing-record/own")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].memberName").value(borrowingRecord1.getMember().getName()))
                .andExpect(jsonPath("$.content[0].returnDate").value("15-Feb-2025 18:30:00"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "Harry Potter",
            "HaRrY PoTtEr",
            "HARRY POTTER",
            "harry potter"
    })
    void testGetBorrowingRecordByBookTitle_WhenBookTitleIsFullyProvidedIgnoringCase_ShouldReturnPageOfRecords(String title) throws Exception {
        mockMvc.perform(get("/borrowing-record/book-title")
                        .param("title", title)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].bookTitle", everyItem(equalToIgnoringCase(title))))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "Potter",
            "RrY PoTt",
            "POTTER",
            "potter"
    })
    void testGetBooksByTitle_WhenBookTitleIsPartiallyProvided_ShouldReturnPageOfRecords(String title) throws Exception {
        mockMvc.perform(get("/borrowing-record/book-title")
                        .param("title", title)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].bookTitle", everyItem(containsStringIgnoringCase(title))))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"non-matching title"})
    void testGetBooksByTitle_WhenBookTitleDoesNotMatch_ShouldReturnEmptyPage(String title) throws Exception {
        mockMvc.perform(get("/borrowing-record/book-title")
                        .param("title", title)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "Member",
            "MEMBER",
            "member",
            "MeMbEr"
    })
    void testGetBorrowingRecordByMemberName_WhenMemberNameIsFullyProvidedIgnoringCase_ShouldReturnPageOfRecords(String name) throws Exception {
        mockMvc.perform(get("/borrowing-record/member-name")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].memberName", everyItem(equalToIgnoringCase(name))))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "ber",
            "BER",
            "embe",
            "eMbE"
    })
    void testGetBorrowingRecordByMemberName_WhenMemberNameIsPartiallyProvided_ShouldReturnPageOfRecords(String name) throws Exception {
        mockMvc.perform(get("/borrowing-record/member-name")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].memberName", everyItem(containsStringIgnoringCase(name))))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"non-matching name"})
    void testGetBorrowingRecordByMemberName_WhenMemberNameDoesNotMatch_ShouldReturnEmptyPage(String name) throws Exception {
        mockMvc.perform(get("/borrowing-record/member-name")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "13-Feb-2025 12:00:00",
            "13-Feb-2025 18:30:00"
    })
    void getBorrowingRecordByBorrowDate_WhenDateAndTimeIsProvided_ShouldReturnPageOfRecords(String dateTime) throws Exception {
        mockMvc.perform(get("/borrowing-record/borrow-date")
                        .param("borrowDate", dateTime)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[*].borrowDate", everyItem(equalTo(dateTime))))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));

    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"13-Feb-2025"})
    void getBorrowingRecordByBorrowDate_WhenOnlyDateIsProvided_ShouldReturnPageOfRecords(String date) throws Exception {
        mockMvc.perform(get("/borrowing-record/borrow-date")
                        .param("borrowDate", date)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].borrowDate", everyItem(startsWith(date))))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"13-Feb-2006"})
    void getBorrowingRecordByBorrowDate_WhenDateAndTimeDoesNotMatch_ShouldReturnEmptyPage(String date) throws Exception {
        mockMvc.perform(get("/borrowing-record/borrow-date")
                        .param("borrowDate", date)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "15-Feb-2025 12:00:00",
            "15-Feb-2025 18:30:00"
    })
    void testGetBorrowingRecordsByReturnDate_WhenDateAndTimeIsProvided_ShouldReturnPageOfRecords(String dateTime) throws Exception {
        mockMvc.perform(get("/borrowing-record/return-date")
                        .param("returnDate", dateTime)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[*].returnDate", everyItem(equalTo(dateTime))))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));

    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"15-Feb-2025"})
    void testGetBorrowingRecordsByReturnDate_WhenOnlyDateIsProvided_ShouldReturnPageOfRecords(String date) throws Exception {
        mockMvc.perform(get("/borrowing-record/return-date")
                        .param("returnDate", date)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].returnDate", everyItem(startsWith(date))))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"15-Feb-2006"})
    void testGetBorrowingRecordsByReturnDate_WhenDateAndTimeDoesNotMatch_ShouldReturnEmptyPage(String date) throws Exception {
        mockMvc.perform(get("/borrowing-record/return-date")
                        .param("returnDate", date)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "member@gmail.com",
            "MEMBER@GMAIL.COM",
            "Member@gmail.com",
            "MeMbEr@gMaiL.CoM"
    })
    void testGetBorrowingRecordByMemberEmail_WhenMemberEmailIsFullyProvidedIgnoringCase_ShouldReturnPageOfRecords(String email) throws Exception {
        mockMvc.perform(get("/borrowing-record/email")
                        .param("email", email)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].memberEmail", everyItem(equalToIgnoringCase(email))))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "member",
            "MEMBER",
            "ber@gmail.com",
            "bEr@gMaiL.CoM"
    })
    void testGetBorrowingRecordByMemberEmail_WhenMemberEmailIsPartiallyProvided_ShouldReturnEmptyPage(String email) throws Exception {
        mockMvc.perform(get("/borrowing-record/email")
                        .param("email", email)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"nonmatchingemail@gmail.com"})
    void testGetBorrowingRecordByMemberEmail_WhenMemberEmailDoesNotMatch_ShouldReturnEmptyPage(String email) throws Exception {
        mockMvc.perform(get("/borrowing-record/email")
                        .param("email", email)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testApproveBorrowRequest_ShouldUpdateRecord_AndReturnConfirmation() throws Exception {
        mockMvc.perform(put("/borrowing-record/approve/{id}", borrowingRecord1.getId()))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Request has been approved"));

        mockMvc.perform(get("/borrowing-record/{id}", borrowingRecord1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(borrowingRecord1.getId()))
                .andExpect(jsonPath("$.memberEmail").value(borrowingRecord1.getMember().getEmail()))
                .andExpect(jsonPath("$.bookTitle").value(borrowingRecord1.getBook().getTitle()))
                .andExpect(jsonPath("$.approved").value(true));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testApproveBorrowRequest_WhenRecordDoesNotExist_ShouldThrowException() throws Exception {
        mockMvc.perform(put("/borrowing-record/approve/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("BORROWING RECORD NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Record not found with the id: 99"));
    }
}