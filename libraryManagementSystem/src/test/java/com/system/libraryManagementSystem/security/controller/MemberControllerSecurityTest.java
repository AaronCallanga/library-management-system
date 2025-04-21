package com.system.libraryManagementSystem.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class MemberControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    AuthorRepository authorRepository;

    private static Member member;
    private static Member librarian;
    private static Member admin;
    private static Member regularMember;
    private static Book borrowedBook;
    private static Author author1;


    @BeforeAll
    static void setUpOnce(
            @Autowired BookRepository bookRepository,
            @Autowired AuthorRepository authorRepository,
            @Autowired MemberRepository memberRepository,
            @Autowired JwtService jwtService
    ) {
        memberRepository.deleteAll();
        authorRepository.deleteAll();
        bookRepository.deleteAll();

         author1 = Author.builder()
                .name("J.K. Rowling")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();
        authorRepository.save(author1);

         borrowedBook = Book.builder()
                .title("Harry Potter and the Philosopher's Stone")
                .genre("Fantasy")
                .publicationYear(1997)
                .author(author1)
                .members(new ArrayList<>())
                .build();
        bookRepository.saveAll(List.of(borrowedBook));

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

        regularMember = Member.builder()
                .name("Regular")
                .email("regular@gmail.com")
                .password("12345regular")
                .roles(Set.of("ROLE_MEMBER"))
                .borrowedBooks(List.of(borrowedBook))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        memberRepository.saveAll(List.of(member, librarian, admin, regularMember));
    }


    @Test
    void testGetAllMember_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/members"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200"
    })
    void testGetAllMember_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/members")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testGetMemberById_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/members/{id}", regularMember.getId()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200"
    })
    void testGetMemberById_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/members/{id}", regularMember.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testGetMemberByName_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/members/name")
                        .param("name", regularMember.getName()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200"
    })
    void testGetMemberByName_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/members/name")
                        .param("name", regularMember.getName())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }




    @Test
    void testGetMemberByBorrowedBookTitle_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/members/book-title")
                        .param("bookTitle", regularMember.getBorrowedBooks().get(0).getTitle()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200"
    })
    void testGetMemberByBorrowedBookTitle_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/members/book-title")
                        .param("bookTitle", regularMember.getBorrowedBooks().get(0).getTitle())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testSaveMember_WhenUnauthenticated_ShouldReturn403() throws Exception {
        Member newMember = Member.builder()
                .name("Member")
                .email("member@gmail.com")
                .password("12345member")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        mockMvc.perform(post("/members")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newMember)))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 403",
            "admin@gmail.com, 201"
    })
    void testSaveMember_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        Member newMember = Member.builder()
                .name("New Member")
                .email("newMember@gmail.com")
                .password("12345newmember")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(post("/members")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newMember))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testUpdateMember_WhenUnauthenticated_ShouldReturn403() throws Exception {
        Member updatedMember = Member.builder()
                .id(regularMember.getId())
                .name("Updated")
                .email("updatedregular@gmail.com")
                .password("12345regular")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        mockMvc.perform(put("/members/update")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedMember)))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200"
    })
    void testUpdateMember_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        Member updatedMember = Member.builder()
                .id(regularMember.getId())
                .name("Updated")
                .email("updatedregular@gmail.com")
                .password("12345regular")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(put("/members/update")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedMember))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testDeleteMember_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/members/{id}", regularMember.getId()))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 403",
            "admin@gmail.com, 204"
    })
    void testDeleteMember_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(delete("/members/{id}", regularMember.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testGetOwnMemberDetails_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/members/own/{id}", regularMember.getId()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 403",
            "admin@gmail.com, 403",
            "regular@gmail.com, 200"
    })
    void testGetOwnMemberDetails_WhenDetailsIsOwnedOrNotOwned_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/members/own/{id}", regularMember.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testUpdateOwnMemberDetails_WhenUnauthenticated_ShouldReturn403() throws Exception {
        Member updatedMember = Member.builder()
                .id(regularMember.getId())
                .name("Updated")
                .email("updatedregular@gmail.com")
                .password("12345regular")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        mockMvc.perform(put("/members/update/own")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedMember)))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 403",
            "admin@gmail.com, 403",
            "regular@gmail.com, 200"
    })
    void testUpdateOwnMemberDetails_WhenDetailsIsOwnedOrNotOwned_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        Member updatedMember = Member.builder()
                .id(regularMember.getId())
                .name("Updated")
                .email("updatedregular@gmail.com")
                .password("12345regular")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(put("/members/update/own")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedMember))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testBorrowBook_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/members/{memberId}/borrow/{bookId}", regularMember.getId(), borrowedBook.getId()))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 403",
            "admin@gmail.com, 403",
            "regular@gmail.com, 200"
    })
    void testBorrowBook_WhenAccountIsOwnedOrNot_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(post("/members/{memberId}/borrow/{bookId}", regularMember.getId(), borrowedBook.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testReturnBook_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/members/{memberId}/return/{bookId}", regularMember.getId(), borrowedBook.getId()))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 403",
            "admin@gmail.com, 403",
            "regular@gmail.com, 200"
    })
    void testReturnBook_WhenAccountIsOwnedOrNot_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(post("/members/{memberId}/return/{bookId}", regularMember.getId(), borrowedBook.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testBorrowBookWithAuthority_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/members/authorized/{memberId}/borrow/{bookId}", regularMember.getId(), borrowedBook.getId()))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
            "regular@gmail.com, 403"
    })
    void testBorrowBookWithAuthority_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(post("/members/authorized/{memberId}/borrow/{bookId}", regularMember.getId(), borrowedBook.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testReturnBookWithAuthority_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/members/authorized/{memberId}/return/{bookId}", regularMember.getId(), borrowedBook.getId()))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
            "regular@gmail.com, 403"
    })
    void testReturnBookWithAuthority_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(post("/members/authorized/{memberId}/return/{bookId}", regularMember.getId(), borrowedBook.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }



}