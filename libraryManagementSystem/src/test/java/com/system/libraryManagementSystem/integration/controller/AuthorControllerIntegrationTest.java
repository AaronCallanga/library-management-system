package com.system.libraryManagementSystem.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.libraryManagementSystem.dto.AuthorDTO;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import com.system.libraryManagementSystem.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc()
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AuthorControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ObjectMapper objectMapper;      //for deserialization and serialization

    private Author author1, author2;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();
        bookRepository.deleteAll();
        author1 = Author.builder()
                .name("J.K. Rowling")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();

        author2 = Author.builder()
                .name("George Orwell")
                .biography("English novelist, best known for '1984' and 'Animal Farm'.")
                .publishedBooks(new ArrayList<>())
                .build();
        authorRepository.save(author1);
        authorRepository.save(author2);

        Book book1 = new Book(null, "Harry Potter and the Philosopher's Stone", "Fantasy", 1997, author1, new ArrayList<>());
        Book book2 = new Book(null , "Harry Potter and the Chamber of Secrets", "Fantasy", 1998, author1, new ArrayList<>());
        Book book3 = new Book(null, "Animal Farm", "Political Satire", 1945, author2, new ArrayList<>());
        Book book4 = new Book(null, "1984", "Dystopian", 1949, author2, new ArrayList<>());

        bookRepository.saveAll(List.of(book1, book2, book3, book4));
    }

    @Test
    void testGetAllAuthors_WhenAuthorsExist_ShouldReturnPageOfAuthors() throws Exception {
        mockMvc.perform(get("/authors")
                .param("page", "0")
                .param("size", "10")
                .param("sortDirection", "ASC")
                .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value(author1.getName()))
                .andExpect(jsonPath("$.content[1].name").value(author2.getName()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @Test
    void testGetAllAuthors_WhenAuthorsDoNotExist_ShouldReturnEmptyPage() throws Exception {
        authorRepository.deleteAll();

        mockMvc.perform(get("/authors")
                .param("page", "0")
                .param("size", "10")
                .param("sortDirection", "ASC")
                .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.unpaged").value(false));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testSaveNewAuthor_ShouldReturnCreatedAuthor() throws Exception {
        AuthorDTO newAuthor =  AuthorDTO.builder()
                .name("Agatha Christie")
                .biography("Famous for mystery novels")
                .build();

        mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAuthor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(newAuthor.getName()))
                .andExpect(jsonPath("$.biography").value(newAuthor.getBiography()));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testUpdateAuthor_ShouldReturnUpdatedAuthor() throws Exception {
        AuthorDTO newAuthor = AuthorDTO.builder()
                .id(author2.getId())
                .name("George Updated")
                .biography("Updated Bioooooooooo")
                .publishedBooks(new ArrayList<>())
                .build();

        mockMvc.perform(put("/authors/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAuthor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newAuthor.getName()))
                .andExpect(jsonPath("$.biography").value(newAuthor.getBiography()));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testDeleteAuthor_ShouldReturnStatusNoContent() throws Exception {
        mockMvc.perform(delete("/authors/{id}", author1.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetAuthorById_WhenAuthorExist_ShouldReturnAuthor() throws Exception {
        mockMvc.perform(get("/authors/{id}", author1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(author1.getId()))
                .andExpect(jsonPath("$.name").value(author1.getName()));
    }

    @Test
    void testGetAuthorById_WhenAuthorDoesNotExist_ShouldThrowException() throws Exception {
        mockMvc.perform(get("/authors/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("AUTHOR NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Author not found with the id: 99"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Harry Potter and the Philosopher's Stone",
            "HaRrY PoTteR aNd tHe PhIloSopHer'S StOne",
            "Animal Farm",
            "ANiMaL fArM",
    })
    void testGetAuthorByPublishedBookTitle_WhenBookTitleIsFullyProvidedIgnoringCase_ShouldReturnPageOfAuthor(String bookTitle) throws Exception {
        mockMvc.perform(get("/authors/published-book")
                        .param("bookTitle", bookTitle)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.content[0].publishedBooks[0].title", anyOf(equalToIgnoringCase(bookTitle))));
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "Harry Potter",
            "HaRrY PoTtEr",
            "Farm",
            "FaRm"
    })
    void testGetAuthorByPublishedBookTitle_WhenBookTitleIsPartiallyProvided_ShouldReturnPageOfAuthor(String bookTitle) throws Exception {
        mockMvc.perform(get("/authors/published-book")
                        .param("bookTitle", bookTitle)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].publishedBooks[0].title", containsStringIgnoringCase(bookTitle)));
    }

    @Test
    void testGetAuthorByPublishedBookTitle_WhenBookTitleDoesNotMatch_ShouldReturnEmptyPage() throws Exception {
        String bookTitle = "Non-Matching Book Title";
        mockMvc.perform(get("/authors/published-book")
                        .param("bookTitle", bookTitle)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.paged").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"J.k. RoWlInG", "J.K. Rowling", "GeOrGe OrWeLl", "George Orwell"})
    void testGetAuthorByName_WhenNameIsFullyProvidedIgnoringCase_ShouldReturnPageOfAuthor(String name) throws Exception {
        mockMvc.perform(get("/authors/name")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.content[0].name", anyOf(equalToIgnoringCase(name))));
    }


    @ParameterizedTest
    @ValueSource(strings = {"Rowling", "RoWlInG", "George", "gEoRgE"})
    void testGetAuthorByName_WhenNameIsPartiallyProvided_ShouldReturnPageOfAuthor(String name) throws Exception {
        mockMvc.perform(get("/authors/name")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name", containsStringIgnoringCase(name)));
    }

    @Test
    void testGetAuthorByName_WhenNameDoesNotMatch_ShouldReturnEmptyPage() throws Exception {
        String name = "Non-Matching Name";
        mockMvc.perform(get("/authors/name")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.paged").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "British author best known for the Harry Potter series.",
            "English novelist, best known for '1984' and 'Animal Farm'.",
            "BrItiSh auThoR beSt kNown For The HaRrY PoTteR seRieS.",
            "EnGliSh noVeList, bEst knOwn fOr '1984' aNd 'AnImaL FaRm'."
    })
    void testGetAuthorByBiography_WhenBiographyIsFullyProvidedIgnoringCase_ShouldReturnPageOfAuthor(String biography) throws Exception {
        mockMvc.perform(get("/authors/biography")
                        .param("keyword", biography)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.content[0].biography", anyOf(equalToIgnoringCase(biography))));
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "Harry Potter series.",
            "best known for '1984' and 'Animal Farm'.",
            "HaRrY PoTteR seRieS.",
            "bEst knOwn fOr '1984' aNd 'AnImaL FaRm'."
    })
    void testGetAuthorByBiography_WhenBiographyIsPartiallyProvided_ShouldReturnPageOfAuthor(String biography) throws Exception {
        mockMvc.perform(get("/authors/biography")
                        .param("keyword", biography)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].biography", containsStringIgnoringCase(biography)));
    }

    @Test
    void testGetAuthorByBiography_WhenBiographyDoesNotMatch_ShouldReturnEmptyPage() throws Exception {
        String biography = "Non-Matching Biography";
        mockMvc.perform(get("/authors/biography")
                        .param("keyword", biography)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.paged").value(true));
    }


}

/*
* {
  "content": [
    {
      "id": 1,
      "name": "J.K. Rowling",
      "biography": "British author best known for the Harry Potter series."
    },
    {
      "id": 2,
      "name": "George Orwell",
      "biography": "English novelist, best known for '1984' and 'Animal Farm'."
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "pageSize": 10,
    "pageNumber": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "first": true,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 2,
  "empty": false
}
* */