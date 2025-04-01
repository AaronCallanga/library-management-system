package com.system.libraryManagementSystem.integration.service;

import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import com.system.libraryManagementSystem.service.MemberService;
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

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Transactional
@SpringBootTest
class MemberServiceIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member member;
    private Author author;
    private Book book;

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
                .borrowedBooks(List.of(book))
                .password(passwordEncoder.encode("12345member"))
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        memberRepository.save(member);
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void getAllMembers() {
        Page<Member> result = memberService.getAllMembers(0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"Member", "mEmBeR", "MEMBER", "member"})
    void testGetMemberByName_WhenMemberNameFullyMatchIgnoringCase_ShouldReturnPageOfRecords(String name) {
        Page<Member> result = memberService.getMemberByName(name, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(m -> m.getName().equalsIgnoreCase(name)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"Mem", "emB", "er"})
    void testGetMemberByName_WhenMemberNamePartiallyMatchIgnoringCase_ShouldReturnPageOfRecords(String name) {
        Page<Member> result = memberService.getMemberByName(name, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(m -> m.getName().toLowerCase().contains(name.toLowerCase())));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"non-matching name"})
    void testGetMemberByName_WhenMemberNameDoesNotMatch_ShouldReturnEmptyPage(String name) {
        Page<Member> result = memberService.getMemberByName(name, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"Harry Potter", "HaRrY PoTtEr", "HARRY POTTER", "harry potter"})
    void testGetMemberByBorrowedBookTitle_WhenBookTitleFullyMatchIgnoringCase_ShouldReturnPageOfRecords(String bookTitle) {
        Page<Member> result = memberService.getMemberByBorrowedBookTitle(bookTitle, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(m ->
                        m.getBorrowedBooks().stream().allMatch(b ->
                                b.getTitle().equalsIgnoreCase(bookTitle))));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"Harry", "PoTtEr", "rRy poT"})
    void testGetMemberByBorrowedBookTitle_WhenBookTitlePartiallyMatchIgnoringCase_ShouldReturnPageOfRecords(String bookTitle) {
        Page<Member> result = memberService.getMemberByBorrowedBookTitle(bookTitle, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(m ->
                m.getBorrowedBooks().stream().allMatch(b ->
                        b.getTitle().toLowerCase().contains(bookTitle.toLowerCase()))));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"non-matching book title"})
    void testGetMemberByBorrowedBookTitle_WhenBookTitleDoesNotMatch_ShouldReturnEmptyPage(String bookTitle) {
        Page<Member> result = memberService.getMemberByBorrowedBookTitle(bookTitle, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }
}