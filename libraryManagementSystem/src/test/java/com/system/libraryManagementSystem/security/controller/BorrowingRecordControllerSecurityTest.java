package com.system.libraryManagementSystem.security.controller;

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
import com.system.libraryManagementSystem.security.JwtService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)      //resets the application context before invoking the class for test. Because there are some modifications happening in the database such as delete, save, update so we want it to reset to have a fresh database
//use test container so every class test you have new database,
class BorrowingRecordControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    BorrowingRecordRepository borrowingRecordRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtService jwtService;

    private static Author author1;
    private static Book book1;
    private static BorrowingRecord borrowingRecord1;
    private static Member member;
    private static Member librarian;
    private static Member admin;

    @BeforeAll
    static void setUpOnce(
            @Autowired BookRepository bookRepository,
            @Autowired AuthorRepository authorRepository,
            @Autowired MemberRepository memberRepository,
            @Autowired BorrowingRecordRepository borrowingRecordRepository,
            @Autowired JwtService jwtService
    ) {
        authorRepository.deleteAll();
        memberRepository.deleteAll();
        bookRepository.deleteAll();
        borrowingRecordRepository.deleteAll();

        author1 = Author.builder()
                .name("J.K. Rowling")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();
        authorRepository.save(author1);

        book1 = Book.builder()
                .title("Harry Potter and the Philosopher's Stone")
                .genre("Fantasy")
                .publicationYear(1997)
                .author(author1)
                .members(new ArrayList<>())
                .build();
        bookRepository.saveAll(List.of(book1));

         member = Member.builder()
                .name("Member")
                .email("member@gmail.com")
                .password("12345member")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

         librarian = Member.builder()
                .name("Librarian")
                .email("librarian@gmail.com")
                .password("12345librarian")
                .roles(Set.of("ROLE_LIBRARIAN"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

         admin = Member.builder()
                .name("Admin")
                .email("admin@gmail.com")
                .password("12345admin")
                .roles(Set.of("ROLE_ADMIN"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        memberRepository.saveAll(List.of(member, librarian, admin));

        borrowingRecord1 = BorrowingRecord.builder()
                .book(book1)
                .member(member)
                .borrowDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().plusDays(2))
                .isApproved(false)
                .build();
        borrowingRecordRepository.save(borrowingRecord1);
    }

    @Test
    public void testGetAllBorrowingRecords_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/borrowing-record"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testGetAllBooks_WhenUnauthorized_ShouldReturn403(String email, int expectedStatus) throws Exception {       //role is used for debugging
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));    //can also be isForbidden
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200"
    })
    public void testGetAllBooks_WhenAuthorized_ShouldReturn200(String email, int expectedStatus) throws Exception {       //role is used for debugging
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));    //can also be isOk()
    }

    @Test
    public void testGetBorrowingRecordById_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/borrowing-record/{id}", borrowingRecord1.getId()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testGetBorrowingRecordById_WhenUnauthorized_ShouldReturn403(String email, int expectedStatus) throws Exception {       //role is used for debugging
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record/{id}", borrowingRecord1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
    })
    public void testGetBorrowingRecordById_WhenAuthorized_ShouldReturn200(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record/{id}", borrowingRecord1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetBorrowingRecordByBookTitle_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/borrowing-record/book-title")
                        .param("title", book1.getTitle()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testGetBorrowingRecordByBookTitle_WhenUnauthorized_ShouldReturn403(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record/book-title")
                        .param("title", book1.getTitle())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
    })
    public void testGetBorrowingRecordByBookTitle_WhenAuthorized_ShouldReturn200(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record/book-title")
                        .param("title", book1.getTitle())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetBorrowingRecordByMemberName_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/borrowing-record/member-name")
                        .param("name", member.getName()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testGetBorrowingRecordByMemberName_WhenUnauthorized_ShouldReturn403(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record/member-name")
                        .param("name", member.getName())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
    })
    public void testGetBorrowingRecordByMemberName_WhenAuthorized_ShouldReturn200(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record/member-name")
                        .param("name", member.getName())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetBorrowingRecordByBorrowDate_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/borrowing-record/borrow-date")
                        .param("borrowDate", String.valueOf(borrowingRecord1.getBorrowDate())))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({                        //for the sake of learning, we use one method to test authorized and unauthorized
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    public void testGetBorrowingRecordByBorrowDate_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatusCode(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record/member-name")
                        .param("borrowDate", String.valueOf(borrowingRecord1.getBorrowDate()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    public void testGetBorrowingRecordByReturnDate_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/borrowing-record/return-date")
                        .param("returnDate", String.valueOf(borrowingRecord1.getReturnDate())))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    public void testGetBorrowingRecordByReturnDate_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatusCode(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record/return-date")
                        .param("returnDate", "03-Mar-2025")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    public void testGetBorrowingRecordByMemberEmail_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/borrowing-record/email")
                        .param("email", member.getEmail()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    public void testGetBorrowingRecordByMemberEmail_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatusCode(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record/email")
                        .param("email", member.getEmail())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    public void testSaveBorrowingRecord_WhenUnauthenticated_ShouldReturn403() throws Exception {
        BorrowingRecordDTO newRecord = BorrowingRecordDTO.builder()
                .bookId(book1.getId())
                .bookTitle(book1.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDateTime.now())
                .returnDate(null)
                .isApproved(false)
                .build();

        mockMvc.perform(post("/borrowing-record")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newRecord)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 201",
            "admin@gmail.com, 201",
    })
    public void testSaveBorrowingRecord_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatusCode(String email, int expectedStatus) throws Exception {
        BorrowingRecordDTO newRecord = BorrowingRecordDTO.builder()
                .bookId(book1.getId())
                .bookTitle(book1.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDateTime.now())
                .returnDate(null)
                .isApproved(false)
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(post("/borrowing-record")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newRecord))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    public void testUpdateBorrowingRecord_WhenUnauthenticated_ShouldReturn403() throws Exception {
        BorrowingRecordDTO updatedRecord = BorrowingRecordDTO.builder()
                .recordId(borrowingRecord1.getId())
                .bookId(book1.getId())
                .bookTitle(book1.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().plusDays(2))
                .isApproved(false)
                .build();

        mockMvc.perform(post("/borrowing-record/update")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedRecord)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    public void testUpdateBorrowingRecord_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatusCode(String email, int expectedStatus) throws Exception {
        BorrowingRecordDTO updatedRecord = BorrowingRecordDTO.builder()
                .recordId(borrowingRecord1.getId())
                .bookId(book1.getId())
                .bookTitle(book1.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().plusDays(2))
                .isApproved(true)
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(put("/borrowing-record/update")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedRecord))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    public void testDeleteBorrowingRecord_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/borrowing-record/{id}",  borrowingRecord1.getId()))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 403",
            "admin@gmail.com, 204",
    })
    public void testDeleteBorrowingRecord_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatusCode(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(delete("/borrowing-record/{id}", borrowingRecord1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    public void testGetOwnBorrowingRecord_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/borrowing-record/own"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 200",        //test in accessing other record not owned
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    public void testGetOwnBorrowingRecord_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatusCode(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/borrowing-record/own")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    public void testUpdateOwnBorrowingRecord_WhenUnauthenticated_ShouldReturn403() throws Exception {
        BorrowingRecordDTO updatedRecord = BorrowingRecordDTO.builder()
                .recordId(borrowingRecord1.getId())
                .bookId(book1.getId())
                .bookTitle(book1.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().plusDays(2))
                .isApproved(true)
                .build();

        mockMvc.perform(put("/borrowing-record/update/own")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedRecord)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 200"
    })
    public void testUpdateOwnBorrowingRecord_WhenRecordIsOwned_ShouldReturn200(String email, int expectedStatus) throws Exception {
        BorrowingRecordDTO updatedRecord = BorrowingRecordDTO.builder()
                .recordId(borrowingRecord1.getId())
                .bookId(book1.getId())
                .bookTitle(book1.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().plusDays(2))
                .isApproved(true)
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(put("/borrowing-record/update/own")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedRecord))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testUpdateOwnBorrowingRecord_WhenUpdatingNotOwnedRecord_ShouldReturn403(String email, int expectedStatus) throws Exception {
        BorrowingRecord librarianBook = BorrowingRecord.builder()
                .book(book1)
                .member(member)
                .borrowDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().plusDays(2))
                .isApproved(true)
                .build();
        borrowingRecordRepository.save(librarianBook);

        BorrowingRecordDTO updateLibrarianBook = BorrowingRecordDTO.builder()
                .recordId(librarianBook.getId())
                .bookId(book1.getId())
                .bookTitle(book1.getTitle())
                .memberId(librarian.getId())
                .memberName(librarian.getName())
                .memberEmail(librarian.getEmail())
                .borrowDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().plusDays(3))
                .isApproved(true)
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(put("/borrowing-record/update/own")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateLibrarianBook))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteOwnBorrowingRecord_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/borrowing-record/own/{id}", borrowingRecord1.getId()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 204"
    })
    public void testDeleteOwnBorrowingRecord_WhenDeletingOwnedRecord_ShouldReturn204(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(delete("/borrowing-record/own/{id}", borrowingRecord1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testDeleteOwnBorrowingRecord_WhenDeletingNotOwnedRecord_ShouldReturn403(String email, int expectedStatus) throws Exception {
        BorrowingRecord librarianBook = BorrowingRecord.builder()
                .book(book1)
                .member(librarian)
                .borrowDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().plusDays(2))
                .isApproved(true)
                .build();
        borrowingRecordRepository.save(librarianBook);

        String token = jwtService.getToken(email);
        mockMvc.perform(delete("/borrowing-record/own/{id}", librarianBook.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSendOwnBorrowingRequest_WhenUnauthenticated_ShouldReturn403() throws Exception {
        BorrowingRecordDTO request = BorrowingRecordDTO.builder()
                .bookId(book1.getId())
                .bookTitle(book1.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDateTime.now())
                .returnDate(null)
                .isApproved(false)
                .build();

        mockMvc.perform(post("/borrowing-record/request")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 201"
    })
    public void testSendBorrowingRequest_WhenAuthenticated_ShouldReturn201(String email, int expectedStatus) throws Exception {
        BorrowingRecordDTO request = BorrowingRecordDTO.builder()
                .bookId(book1.getId())
                .bookTitle(book1.getTitle())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberEmail(member.getEmail())
                .borrowDate(LocalDateTime.now())
                .returnDate(null)
                .isApproved(false)
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(post("/borrowing-record/request")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testSendBorrowingRequest_WhenSendingUnauthorizedRequestToAnotherMember_ShouldReturn403(String email, int expectedStatus) throws Exception {
        BorrowingRecordDTO request = BorrowingRecordDTO.builder()
                .bookId(book1.getId())
                .bookTitle(book1.getTitle())
                .memberId(librarian.getId())
                .memberName(librarian.getName())
                .memberEmail(librarian.getEmail())
                .borrowDate(LocalDateTime.now())
                .returnDate(null)
                .isApproved(false)
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(post("/borrowing-record/request")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

}