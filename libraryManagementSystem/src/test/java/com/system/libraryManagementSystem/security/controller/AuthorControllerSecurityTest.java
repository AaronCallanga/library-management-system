package com.system.libraryManagementSystem.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.AuthorRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
class AuthorControllerSecurityTest {        //integration test, cause we are using the real controller when we interact with api which also uses real service and repository
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtService jwtService;


    private static Author author1;
    private static Member member;
    private static Member librarian;
    private static Member admin;

    @BeforeAll
    static void setUpOnce(@Autowired AuthorRepository authorRepository, @Autowired MemberRepository memberRepository, @Autowired JwtService jwtService) {
        authorRepository.deleteAll();
        memberRepository.deleteAll();

        author1 = Author.builder()
                .name("J.K. Rowling")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();
        authorRepository.save(author1);

        Member member = Member.builder()
                .name("Member")
                .email("member@gmail.com")
                .password("12345member")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        Member librarian = Member.builder()
                .name("Librarian")
                .email("librarian@gmail.com")
                .password("12345librarian")
                .roles(Set.of("ROLE_LIBRARIAN"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        Member admin = Member.builder()
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
    }

    @Test
    public void testGetAllAuthors_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
            "member@gmail.com, 200"
    })
        public void testGetAllAuthors_WhenAuthenticated_ShouldReturn200(String email, int expectedStatus) throws Exception {       //role is used for debugging
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/authors")
                        .header("Authorization", token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    public void testGetAuthorsById_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/authors/{id}", author1.getId()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
            "member@gmail.com, 200"
    })
    public void testGetAuthorsById_WhenAuthenticated_ShouldReturn200(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/authors/{id}", author1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    public void testGetAuthorByPublishedBookTitle_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/authors/published-book"))     //param "bookTitle" is not required because we have default values
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
            "member@gmail.com, 200"
    })
    public void testGetAuthorByPublishedBookTitle_WhenAuthenticated_ShouldReturn200(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/authors/published-book")
                        .header("Authorization","Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAuthorsByName_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/authors/name"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
            "member@gmail.com, 200"
    })
    public void testGetAuthorsByName_WhenAuthenticated_ShouldReturn200(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/authors/name")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAuthorsByBiography_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/authors/biography"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
            "member@gmail.com, 200"
    })
    public void testGetAuthorsByBiography_WhenAuthenticated_ShouldReturn200(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/authors/biography")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testSaveAuthors_WhenUnauthenticated_ShouldReturn403() throws Exception {
        Author author2 = Author.builder()
                .name("J.K. Rowling")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();

        mockMvc.perform(post("/authors")
                        .content(objectMapper.writeValueAsString(author2)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testSaveAuthors_WhenUnauthorized_ShouldReturn403(String email, int expectedStatus) throws Exception {
        Author author2 = Author.builder()
                .name("J.K. Rowling")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();
        String token = jwtService.getToken(email);

        mockMvc.perform(post("/authors")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(author2)))
                .andExpect(status().isForbidden());
    }


    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 201",
            "librarian@gmail.com, 201",
    })
    public void testSaveAuthors_WhenAuthorized_ShouldReturn201(String email, int expectedStatus) throws Exception {
        Author author2 = Author.builder()
                .name("J.K. Rowling")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();
        String token = jwtService.getToken(email);
        mockMvc.perform(post("/authors")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(author2)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testUpdateAuthor_WhenUnauthenticated_ShouldReturn403() throws Exception {
        Author updatedAuthor = Author.builder()
                .id(author1.getId())
                .name("J.K. Updated")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();

        mockMvc.perform(put("/authors/update")
                        .content(objectMapper.writeValueAsString(updatedAuthor)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testUpdateAuthor_WhenUnauthorized_ShouldReturn403(String email, int expectedStatus) throws Exception {
        Author updatedAuthor = Author.builder()
                .id(author1.getId())
                .name("J.K. Updated")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();

        String token = jwtService.getToken(email);

        mockMvc.perform(put("/authors/update")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updatedAuthor)))
                .andExpect(status().isForbidden());
    }


    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 201",
            "librarian@gmail.com, 201",
    })
    public void testUpdateAuthor_WhenAuthorized_ShouldReturn200(String email, int expectedStatus) throws Exception {
        Author updatedAuthor = Author.builder()
                .id(author1.getId())
                .name("J.K. Updated")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(put("/authors/update")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updatedAuthor)))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteAuthor_WhenUnauthenticated_ShouldReturn403() throws Exception {

        mockMvc.perform(delete("/authors/{id}", author1.getId()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testDeleteAuthor_WhenUnauthorized_ShouldReturn403(String email, int expectedStatus) throws Exception {
        Author updatedAuthor = Author.builder()
                .id(author1.getId())
                .name("J.K. Updated")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();

        String token = jwtService.getToken(email);

        mockMvc.perform(delete("/authors/{id}", author1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }


    @Transactional      //to revert the delete
    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 201",
            "librarian@gmail.com, 201",
    })
    public void testDeleteAuthor_WhenAuthorized_ShouldReturn204(String email, int expectedStatus) throws Exception {
        Author updatedAuthor = Author.builder()
                .id(author1.getId())
                .name("J.K. Updated")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(delete("/authors/{id}", author1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }


}