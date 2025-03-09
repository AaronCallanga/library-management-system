package com.system.libraryManagementSystem.integration;

import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@ActiveProfiles("test")
class AuthorServiceIntegrationTest {

    @Autowired
    AuthorRepository authorRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    AuthorService authorService;

    private Author author1, author2;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
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
    void getAllAuthors_WhenAuthorsExist_ShouldReturnPageOfAuthors() {
        Page<Author> result = authorService.getAllAuthors(0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Harry Potter and the Philosopher's Stone",
            "Animal Farm",
            "HaRrY PoTteR aNd ThE PhIloSoPheR's StOnE",
            "ANiMaL FaRM"
    })
    void getAuthorsByPublishedBookTitle_WhenTitleIsFullyProvidedIgnoringCase_ShouldReturnPageOfAuthors(String bookTitle) {
        Page<Author> result = authorService.getAuthorsByPublishedBookTitle(bookTitle,0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .flatMap(author -> author.getPublishedBooks().stream())
                .anyMatch(book -> book.getTitle().equalsIgnoreCase(bookTitle))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Harry Potter",
            "Farm",
            "HaRrY PoTteR",
            "FaRM"
    })
    void getAuthorsByPublishedBookTitle_WhenTitleIsPartiallyProvidedIgnoringCase_ShouldReturnPageOfAuthors(String bookTitle) {
        Page<Author> result = authorService.getAuthorsByPublishedBookTitle(bookTitle,0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .flatMap(author -> author.getPublishedBooks().stream())
                .anyMatch(book -> book.getTitle().toLowerCase().contains(bookTitle.toLowerCase()))
        );
    }

    @Test
    void getAuthorsByPublishedBookTitle_WhenTitleIsDoesNotMatch_ShouldReturnEmptyPage() {
        String bookTitle = "Non-matching title";
        Page<Author> result = authorService.getAuthorsByBiographyKeyword(bookTitle, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.getContent().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "J.K. Rowling",
            "George Orwell",
            "J.k. RoWlInG",
            "geOrGe OrWeLl",
    })
    void getAuthorsByName_WhenNameIsFullyProvidedIgnoringCase_ShouldReturnPageOfAuthors(String name) {
        Page<Author> result = authorService.getAuthorsByName(name, 0, 10, "ASC", "id");

        assertEquals(1, result.getTotalElements());
        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(author -> author.getName().equalsIgnoreCase(name)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Rowling",
            "Orwell",
            "RoWlInG",
            "geOrGe",
    })
    void getAuthorsByName_WhenNameIsPartiallyProvidedIgnoringCase_ShouldReturnPageOfAuthors(String name) {
        Page<Author> result = authorService.getAuthorsByName(name, 0, 10, "ASC", "id");

        assertEquals(1, result.getTotalElements());
        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(author -> author.getName().toLowerCase().contains(name.toLowerCase())));
    }

    @Test
    void getAuthorsByName_WhenNameDoesNotMatch_ShouldReturnEmptyPage() {
        String name = "Non-matching name";
        Page<Author> result = authorService.getAuthorsByBiographyKeyword(name, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.getContent().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "British author best known for the Harry Potter series.",
            "English novelist, best known for '1984' and 'Animal Farm'.",
            "BrItiSh auThoR beSt kNowN foR tHe HArRy PotTEr sEriEs.",
            "EnGliSh nOveList, bEst KnoWn fOr '1984' aNd 'AnImaL FaRm'.",
    })
    void getAuthorsByBiographyKeyword_WhenBiographyIsFullyProvidedIgnoringCase_ShouldReturnPageOfAuthors(String biography) {
        Page<Author> result = authorService.getAuthorsByBiographyKeyword(biography, 0 , 10, "ASC", "id");

        assertEquals(1, result.getTotalElements());
        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(author -> author.getBiography().equalsIgnoreCase(biography)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "best known for the Harry Potter series.",
            "English novelist, best known for '1984'",
            "BrItiSh auThoR",
            "EnGliSh nOveList",
    })
    void getAuthorsByBiographyKeyword_WhenBiographyIsPartiallyProvidedIgnoringCase_ShouldReturnPageOfAuthors(String biography) {
        Page<Author> result = authorService.getAuthorsByBiographyKeyword(biography, 0 , 10, "ASC", "id");

        assertEquals(1, result.getTotalElements());
        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(author -> author.getBiography().toLowerCase().contains(biography.toLowerCase())));
    }

    @Test
    void getAuthorsByBiography_WhenBiographyDoesNotMatch_ShouldReturnEmptyPage() {
        String biography = "Non-matching biography";
        Page<Author> result = authorService.getAuthorsByBiographyKeyword(biography, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.getContent().isEmpty());
    }
}