package com.system.libraryManagementSystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.libraryManagementSystem.dto.AuthorDTO;
import com.system.libraryManagementSystem.dto.BookDTO;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import com.system.libraryManagementSystem.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BookControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    AuthorRepository authorRepository;
    @Autowired
    BookRepository bookRepository;

    private Author author1, author2;
    private Book book1, book2, book3, book4;


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

         book1 = new Book(null, "Harry Potter and the Philosopher's Stone", "Fantasy", 1997, author1, new ArrayList<>());
         book2 = new Book(null , "Harry Potter and the Chamber of Secrets", "Fantasy", 1998, author1, new ArrayList<>());
         book3 = new Book(null, "Animal Farm", "Political Satire", 1945, author2, new ArrayList<>());
         book4 = new Book(null, "1984", "Dystopian", 1949, author2, new ArrayList<>());

        bookRepository.saveAll(List.of(book1, book2, book3, book4));
    }

    @Test
    void testGetAllBooks_WhenBooksExist_ShouldReturnPageOfBooks() throws Exception {
        mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.content[0].title").value(book1.getTitle()))
                .andExpect(jsonPath("$.content[1].title").value(book2.getTitle()))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @Test
    void testGetAllAuthors_WhenAuthorsDoNotExist_ShouldReturnEmptyPage() throws Exception {
        authorRepository.deleteAll();

        mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.unpaged").value(false));
    }

    @Test
    void testGetBookById_WhenBookExist_ShouldReturnBook() throws Exception {
        mockMvc.perform(get("/books/{id}", book1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(book1.getId()))
                .andExpect(jsonPath("$.title").value(book1.getTitle()));
    }

    @Test
    void testGetBookById_WhenBookDoesNotExist_ShouldThrowException() throws Exception {
        mockMvc.perform(get("/books/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("BOOK NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Book not found with the id: 99"));
    }

    @Test
    void testSaveNewBook_ShouldReturnCreatedBook() throws Exception {
        BookDTO newBook = BookDTO.builder()
                .title("New Book")
                .genre("Latest")
                .publicationYear(2025)
                .authorId(author1.getId())
                .authorName("J.K. Rowling")
                .build();

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(newBook.getTitle()))
                .andExpect(jsonPath("$.genre").value(newBook.getGenre()))
                .andExpect(jsonPath("$.authorId").value(author1.getId()))
                .andExpect(jsonPath("$.authorName").value(author1.getName()));
    }

    @Test
    void testUpdateBook_ShouldReturnUpdatedBook() throws Exception {
        BookDTO newBook = BookDTO.builder()
                .id(book1.getId())
                .title("Updated Book")
                .genre("Updated Genre")
                .publicationYear(2025)
                .authorId(author1.getId())
                .authorName("J.K. Rowling")
                .build();

        mockMvc.perform(put("/books/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(newBook.getTitle()))
                .andExpect(jsonPath("$.genre").value(newBook.getGenre()))
                .andExpect(jsonPath("$.authorId").value(author1.getId()))
                .andExpect(jsonPath("$.authorName").value(author1.getName()));
    }

    @Test
    void testDeleteBooks_ShouldReturnStatusNoContent() throws Exception {
        mockMvc.perform(delete("/books/{id}", book1.getId()))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Harry Potter and the Philosopher's Stone",
            "HaRrY PoTteR aNd tHe PhIloSopHer'S StOne",
            "Animal Farm",
            "ANiMaL fArM",
    })
    void testGetBooksByTitle_WhenBookTitleIsFullyProvidedIgnoringCase_ShouldReturnPageOfBooks(String bookTitle) throws Exception {
        mockMvc.perform(get("/books/title")
                        .param("title", bookTitle)
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
                .andExpect(jsonPath("$.content[0].title", allOf(equalToIgnoringCase(bookTitle))));
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "Harry Potter",
            "HaRrY PoTtEr",
    })
    void testGetBooksByTitle_WhenBookTitleIsPartiallyProvided_ShouldReturnPageOfBooks(String bookTitle) throws Exception {
        mockMvc.perform(get("/books/title")
                        .param("title", bookTitle)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title", containsStringIgnoringCase(bookTitle)));
    }

    @Test
    void testGetAuthorByPublishedBookTitle_WhenBookTitleDoesNotMatch_ShouldReturnEmptyPage() throws Exception {
        String bookTitle = "Non-Matching Book Title";
        mockMvc.perform(get("/books/title")
                        .param("title", bookTitle)
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
            "Fantasy",
            "FaNtaSY",
    })
    void testGetBooksByGenre_WhenGenreIsFullyProvidedIgnoringCase_ShouldReturnPageOfBooks(String genre) throws Exception {
        mockMvc.perform(get("/books/genre")
                        .param("genre", genre)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.content[0].genre", allOf(equalToIgnoringCase(genre))))
                .andExpect(jsonPath("$.content[1].genre", allOf(equalToIgnoringCase(genre))));
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "Fant",
            "FaNTa"
    })
    void testGetBooksByGenre_WhenGenreIsPartiallyProvided_ShouldReturnPageOfBooks(String genre) throws Exception {
        mockMvc.perform(get("/books/genre")
                        .param("genre", genre)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].genre", containsStringIgnoringCase(genre)))
                .andExpect(jsonPath("$.content[1].genre", containsStringIgnoringCase(genre)));
    }

    @Test
    void testGetAuthorByGenre_WhenGenreDoesNotMatch_ShouldReturnEmptyPage() throws Exception {
        String genre = "Non-Matching Book Genre";
        mockMvc.perform(get("/books/genre")
                        .param("genre", genre)
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
    @ValueSource(ints = {1945, 1949, 1997, 1998})
    void testGetBooksByPublicationYear_WhenYearMatch_ShouldReturnPageOfBook(int publicationYear) throws Exception {
        mockMvc.perform(get("/books/publication-year")
                    .param("publicationYear", String.valueOf(publicationYear))
                    .param("page", "0")
                    .param("size", "10")
                    .param("sortDirection", "ASC")
                    .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].publicationYear").value(publicationYear));
    }

    @Test
    void testGetBooksByPublicationYear_WhenYearMatch_ShouldReturnEmptyPage() throws Exception {
        int publicationYear = 9999;
        mockMvc.perform(get("/books/publication-year")
                        .param("publicationYear", String.valueOf(publicationYear))
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
            "J.k. RoWlInG",
            "J.K. Rowling"
    })
    void testGetBooksByAuthorsName_WhenNameIsFullyProvidedIgnoringCase_ShouldReturnPageOfBooks(String authorName) throws Exception {
        mockMvc.perform(get("/books/authors-name")
                    .param("authorsName", authorName)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sortDirection", "ASC")
                    .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].authorName", equalToIgnoringCase(authorName)))
                .andExpect(jsonPath("$.content[0].authorId").value(author1.getId()))
                .andExpect(jsonPath("$.content[1].authorName", equalToIgnoringCase(authorName)))
                .andExpect(jsonPath("$.content[1].authorId").value(author1.getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "RoWlInG",
            "Rowling"
    })
    void testGetBooksByAuthorsName_WhenNameIsPartiallyProvidedIgnoringCase_ShouldReturnPageOfBooks(String authorName) throws Exception {
        mockMvc.perform(get("/books/authors-name")
                        .param("authorsName", authorName)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].authorName", containsStringIgnoringCase(authorName)))
                .andExpect(jsonPath("$.content[0].authorId").value(author1.getId()))
                .andExpect(jsonPath("$.content[1].authorName", containsStringIgnoringCase(authorName)))
                .andExpect(jsonPath("$.content[1].authorId").value(author1.getId()));
    }

    @Test
    void testGetBooksByAuthorsName_WhenNameDoesNotMatch_ShouldReturnEmptyPage() throws Exception {
        String authorName = "Non-matching author's name";
        mockMvc.perform(get("/books/authors-name")
                        .param("authorsName", authorName)
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