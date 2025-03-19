package com.system.libraryManagementSystem.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.libraryManagementSystem.dto.BookDTO;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import com.system.libraryManagementSystem.repository.BookRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class BookControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthorRepository authorRepository;
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
    private static Member member;
    private static Member librarian;
    private static Member admin;


    @BeforeAll
    static void setUpOnce(@Autowired BookRepository bookRepository, @Autowired AuthorRepository authorRepository, @Autowired MemberRepository memberRepository, @Autowired JwtService jwtService) {
        authorRepository.deleteAll();
        memberRepository.deleteAll();
        bookRepository.deleteAll();

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
    public void testGetAllBooks_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
            "member@gmail.com, 200"
    })
    public void testGetAllBooks_WhenAuthenticated_ShouldReturn200(String email, int expectedStatus) throws Exception {       //role is used for debugging
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/books")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));    //can also be isOk()
    }

    @Test
    public void testGetBookById_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/books/{id}", book1.getId()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
            "member@gmail.com, 200"
    })
    public void testGetBookById_WhenAuthenticated_ShouldReturn200(String email, int expectedStatus) throws Exception {       //role is used for debugging
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/books/{id}", book1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));    //can also be isOk()
    }

    @Test
    public void testGetBooksByTitle_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/books/title")
                        .param("title", ""))    //default value, dont need to add this param
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
            "member@gmail.com, 200"
    })
    public void testGetBooksByTitle_WhenAuthenticated_ShouldReturn200(String email, int expectedStatus) throws Exception {       //role is used for debugging
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/books/title")
                        .param("title", "")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetBooksByGenre_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/books/genre")
                        .param("genre", ""))    //default value, dont need to add this param
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
            "member@gmail.com, 200"
    })
    public void testGetBooksByGenre_WhenAuthenticated_ShouldReturn200(String email, int expectedStatus) throws Exception {       //role is used for debugging
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/books/genre")
                        .param("genre", "")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetBooksByPublicationYear_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/books/publication-year")
                        .param("publicationYear", String.valueOf("2025")))    //default value, dont need to add this param
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
            "member@gmail.com, 200"
    })
    public void testGetBooksByPublicationYear_WhenAuthenticated_ShouldReturn200(String email, int expectedStatus) throws Exception {       //role is used for debugging
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/books/publication-year")
                        .param("publicationYear", String.valueOf("2025"))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetBooksByAuthorName_WhenUnauthenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/books/authors-name")
                        .param("authorsName", ""))    //default value, dont need to add this param
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 200",
            "librarian@gmail.com, 200",
            "member@gmail.com, 200"
    })
    public void testGetBooksByAuthorName_WhenAuthenticated_ShouldReturn200(String email, int expectedStatus) throws Exception {       //role is used for debugging
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/books/authors-name")
                        .param("authorsName", "")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testSaveBook_WhenUnauthenticated_ShouldReturn403() throws Exception {
        BookDTO newBook = BookDTO.builder()
                .title("New Book")
                .genre("Latest")
                .publicationYear(2025)
                .authorId(author1.getId())
                .authorName("J.K. Rowling")
                .build();

        mockMvc.perform(post("/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testSaveBook_WhenUnauthorized_ShouldReturn403(String email, int expectedStatus) throws Exception {
        BookDTO newBook = BookDTO.builder()
                .title("New Book")
                .genre("Latest")
                .publicationYear(2025)
                .authorId(author1.getId())
                .authorName("J.K. Rowling")
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(post("/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newBook))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 201",
            "librarian@gmail.com, 201",
    })
    public void testSaveBook_Authorized_ShouldReturn201(String email, int expectedStatus) throws Exception {
        BookDTO newBook = BookDTO.builder()
                .title("New Book")
                .genre("Latest")
                .publicationYear(2025)
                .authorId(author1.getId())
                .authorName("J.K. Rowling")
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(post("/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newBook))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    @Test
    public void testUpdateBook_WhenUnauthenticated_ShouldReturn403() throws Exception {
        BookDTO updatedBook = BookDTO.builder()
                .id(book1.getId())
                .title("Updated Book")
                .genre("Updated Genre")
                .publicationYear(2025)
                .authorId(author1.getId())
                .authorName("J.K. Rowling")
                .build();

        mockMvc.perform(put("/books/update")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testUpdateBook_WhenUnauthorized_ShouldReturn403(String email, int expectedStatus) throws Exception {
        BookDTO updatedBook = BookDTO.builder()
                .id(book1.getId())
                .title("Updated Book")
                .genre("Updated Genre")
                .publicationYear(2025)
                .authorId(author1.getId())
                .authorName("J.K. Rowling")
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(put("/books/update")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedBook))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 201",
            "librarian@gmail.com, 201",
    })
    public void testUpdateBook_Authorized_ShouldReturn200(String email, int expectedStatus) throws Exception {
        BookDTO updatedBook = BookDTO.builder()
                .id(book1.getId())
                .title("Updated Book")
                .genre("Updated Genre")
                .publicationYear(2025)
                .authorId(author1.getId())
                .authorName("J.K. Rowling")
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(put("/books/update")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedBook))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteBook_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/books/{id}", book1.getId()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403"
    })
    public void testDeleteBook_WhenUnauthorized_ShouldReturn403(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(delete("/books/{id}", book1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "admin@gmail.com, 201",
            "librarian@gmail.com, 201",
    })
    public void testDeleteBook_Authorized_ShouldReturn200(String email, int expectedStatus) throws Exception {       //role is used for debugging
        String token = jwtService.getToken(email);
        mockMvc.perform(delete("/books/{id}", book1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

}