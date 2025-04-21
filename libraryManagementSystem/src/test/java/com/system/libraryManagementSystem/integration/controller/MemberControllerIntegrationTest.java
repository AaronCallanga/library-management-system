package com.system.libraryManagementSystem.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.libraryManagementSystem.dto.MemberDTO;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class MemberControllerIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Member member;

    private Book book;

    private Author author;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();           //always remember they have relationships, must delete in systematic order
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        author = Author.builder()
                .name("J.K. Rowling")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();
        authorRepository.save(author);

        book = Book.builder()
                .title("Harry Potter and the Philosopher's Stone")
                .genre("Fantasy")
                .publicationYear(1997)
                .author(author)
                .members(new ArrayList<>())
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
                .borrowedBooks(new ArrayList<>())
                .build();
        memberRepository.save(member);

    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testGetAllMembers_WhenMembersExist_ShouldReturnPageOfMembers() throws Exception {
        mockMvc.perform(get("/members")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value(member.getName()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testGetALlMembers_WhenMembersDoesNotExist_ShouldReturnEmptyPage() throws Exception {
        memberRepository.deleteAll();

        mockMvc.perform(get("/members")
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
    void testGetMemberById_WhenMemberExist_ShouldReturnRecords() throws Exception {
        mockMvc.perform(get("/members/{id}", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.email").value(member.getEmail()));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testGetMemberById_WhenMemberDoesNotExist_ShouldThrowException() throws Exception {
        mockMvc.perform(get("/members/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("MEMBER NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Member not found with the id: 99"));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testSaveNewMember_ShouldReturnCreatedMember() throws Exception {
        MemberDTO memberDTO = MemberDTO.builder()
                .name("test member")
                .email("testmember@gmail.com")
                .password(passwordEncoder.encode("12345testmember"))
                .roles(Set.of("ROLE_MEMBER"))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(memberDTO.getName()))
                .andExpect(jsonPath("$.email").value(memberDTO.getEmail()));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testUpdateMember_ShouldReturnUpdatedMember() throws Exception {
        MemberDTO updatedMemberDTO = MemberDTO.builder()
                .id(member.getId())
                .name("Updated Member")
                .email("updatedmember@gmail.com")
                .password(passwordEncoder.encode("12345updatedmember"))
                .roles(Set.of("ROLE_MEMBER"))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        mockMvc.perform(put("/members/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMemberDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.name").value(updatedMemberDTO.getName()))
                .andExpect(jsonPath("$.email").value(updatedMemberDTO.getEmail()))
                .andExpect(jsonPath("$.password").value("HIDDEN"));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void deleteBorrowingRecordById_ShouldReturnStatusNoContent() throws Exception {
        mockMvc.perform(delete("/members/{id}", member.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/members/{id}", member.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("MEMBER NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Member not found with the id: " + member.getId()));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void testGetOwnMemberDetails_WhenMemberExist_ShouldReturnMember() throws Exception {
        mockMvc.perform(get("/members/own/{id}", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.email").value(member.getEmail()));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void testUpdateOwnMemberDetails_WhenMemberExist_ShouldReturnMember() throws Exception {
        MemberDTO updatedMemberDTO = MemberDTO.builder()
                .id(member.getId())
                .name("Updated Member")
                .email("updatedmember@gmail.com")
                .password(passwordEncoder.encode("12345updatedmember"))
                .roles(Set.of("ROLE_MEMBER"))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        mockMvc.perform(put("/members/update/own")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMemberDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.name").value(updatedMemberDTO.getName()))
                .andExpect(jsonPath("$.email").value(updatedMemberDTO.getEmail()))
                .andExpect(jsonPath("$.password").value("HIDDEN"));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void testBorrowBook_WhenMemberAndBookExist_ShouldReturnMember() throws Exception {
        mockMvc.perform(post("/members/{memberId}/borrow/{bookId}", member.getId(), book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.borrowedBooks[0].authorName").value(book.getAuthor().getName()))
                .andExpect(jsonPath("$.borrowedBooks[0].bookTitle").value(book.getTitle()))
                .andExpect(jsonPath("$.borrowedBooks", hasSize(1)));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void testBorrowBook_WhenBookDoesNotExist_ShouldReturnException() throws Exception {
        mockMvc.perform(post("/members/{memberId}/borrow/{bookId}", member.getId(), 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("BOOK NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Book not found with the id: 99"));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void testReturnBook_WhenMemberAndBookExist_ShouldReturnMember() throws Exception {
        member.getBorrowedBooks().add(book);
        memberRepository.save(member);

        mockMvc.perform(post("/members/{memberId}/return/{bookId}", member.getId(), book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.borrowedBooks", hasSize(0)));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void testReturnBook_WhenBookDoesNotExist_ShouldReturnException() throws Exception {
        mockMvc.perform(post("/members/{memberId}/return/{bookId}", member.getId(), 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("BOOK NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Book not found with the id: 99"));
    }

    @WithMockUser(username = "adming@gmail.com", roles = "ADMIN")
    @Test
    void testBorrowBookWithAuthority_WhenMemberAndBookExist_ShouldReturnMember() throws Exception {
        mockMvc.perform(post("/members/authorized/{memberId}/borrow/{bookId}", member.getId(), book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.borrowedBooks[0].authorName").value(book.getAuthor().getName()))
                .andExpect(jsonPath("$.borrowedBooks[0].bookTitle").value(book.getTitle()))
                .andExpect(jsonPath("$.borrowedBooks", hasSize(1)));
    }

    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    @Test
    void testBorrowBookWithAuthority_WhenMemberOrBookDoesNotExist_ShouldReturnException() throws Exception {
        mockMvc.perform(post("/members/authorized/{memberId}/borrow/{bookId}", 99L, book.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("MEMBER NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Member not found with the id: 99"));

        mockMvc.perform(post("/members/authorized/{memberId}/borrow/{bookId}", member.getId(), 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("BOOK NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Book not found with the id: 99"));

        mockMvc.perform(post("/members/authorized/{memberId}/borrow/{bookId}", 99L, 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("MEMBER NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Member not found with the id: 99"));
    }

    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    @Test
    void testReturnBookWithAuthority_WhenMemberAndBookExist_ShouldReturnMember() throws Exception {
        member.getBorrowedBooks().add(book);
        memberRepository.save(member);

        mockMvc.perform(post("/members/authorized/{memberId}/return/{bookId}", member.getId(), book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.borrowedBooks", hasSize(0)));
    }

    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    @Test
    void testReturnBookWithAuthority_WhenMemberOrBookDoesNotExist_ShouldReturnException() throws Exception {
        mockMvc.perform(post("/members/authorized/{memberId}/return/{bookId}", 99L, book.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("MEMBER NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Member not found with the id: 99"));

        mockMvc.perform(post("/members/authorized/{memberId}/return/{bookId}", member.getId(), 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("BOOK NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Book not found with the id: 99"));

        mockMvc.perform(post("/members/authorized/{memberId}/return/{bookId}", 99L, 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("MEMBER NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Member not found with the id: 99"));

    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "member",
            "MEMBER",
            "Member",
            "MeMbEr"
    })
    void testGetMemberByName_WhenMemberNameIsFullyProvidedIgnoringCase_ShouldReturnPageOfRecords(String name) throws Exception {
        mockMvc.perform(get("/members/name")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[*].name", everyItem(equalToIgnoringCase(name))))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "mem",
            "MEM",
            "bEr",
            "emB"
    })
    void testGetMemberByName_WhenMemberNameIsPartiallyProvided_ShouldReturnEmptyPage(String name) throws Exception {
        mockMvc.perform(get("/members/name")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", everyItem(containsStringIgnoringCase(name))))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"nonmatchingname"})
    void testGetMemberByName_WhenMemberNameDoesNotMatch_ShouldReturnEmptyPage(String name) throws Exception {
        mockMvc.perform(get("/members/name")
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
            "Harry Potter and the Philosopher's Stone",
            "HaRrY PoTteR aNd tHe PhIlOsoPheR's StOnE",
            "HARRY POTTER AND THE PHILOSOPHER'S STONE",
            "harry potter and the philosopher's stone"
    })
    void testGetMemberByBorrowedBookTitle_WhenBookTitleIsFullyProvidedIgnoringCase_ShouldReturnPageOfRecords(String bookTitle) throws Exception {
        member.getBorrowedBooks().add(book);
        memberRepository.save(member);

        mockMvc.perform(get("/members/book-title")
                        .param("bookTitle", bookTitle)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[*].borrowedBooks[*].booktitle", everyItem(equalToIgnoringCase(bookTitle))))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "Harry ",
            "HaRrY PoTteR ",
            "POTTER AND THE",
            "philosopher's stone"
    })
    void testGetMemberByBorrowedBookTitle_WhenBookTitleIsPartiallyProvided_ShouldReturnEmptyPage(String bookTitle) throws Exception {
        member.getBorrowedBooks().add(book);
        memberRepository.save(member);
        mockMvc.perform(get("/members/book-title")
                        .param("bookTitle", bookTitle)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].borrowedBooks[*].booktitle", everyItem(containsStringIgnoringCase(bookTitle))))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"nonmatchingtitle"})
    void testGetMemberByBorrowedBookTitle_WhenBookTitleDoesNotMatch_ShouldReturnEmptyPage(String bookTitle) throws Exception {
        mockMvc.perform(get("/members/book-title")
                        .param("bookTitle", bookTitle)
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

}