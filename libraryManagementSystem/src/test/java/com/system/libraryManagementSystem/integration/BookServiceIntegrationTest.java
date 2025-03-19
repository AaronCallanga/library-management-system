package com.system.libraryManagementSystem.integration;

import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.service.BookService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional  //every method has its own transaction, and all the methods like save, update etc will not be persisted but rolledback after each test
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class BookServiceIntegrationTest {

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    BookService bookService;

    private Author author1, author2;
    private Book book1, book2, book3, book4;

    @BeforeEach
    void setUp() {
        // not necessary because we have @transactional, which rollbacks database interaction every test
        //we still use deleteAll because in other test, the value would be save after the test, so just to ensure the database is empty we use deleteAll
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
        author1 = authorRepository.save(author1);
        author2 = authorRepository.save(author2);

        book1 = new Book(null, "Harry Potter and the Philosopher's Stone", "Fantasy", 1997, author1, new ArrayList<>());
        book2 = new Book(null, "Harry Potter and the Chamber of Secrets", "Fantasy", 1998, author1, new ArrayList<>());
        book3 = new Book(null, "1984", "Dystopian", 1949, author2, new ArrayList<>());
        book4 = new Book(null, "Animal Farm", "Political Satire", 1945, author2, new ArrayList<>());

        bookRepository.saveAll(List.of(book1, book2, book3, book4));
    }

    @Test
    void getAllBooks_WhenDataExist_ShouldReturnPageOfBooks() {
        Page<Book> result = bookService.getAllBooks(0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(4, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Animal Farm", "ANIMAL FARM", "aNiMaL fArM"})
    void getBooksByTitle_WhenFullTitleMatch_ShouldReturnPageOfBook(String title) {
        Page<Book> result = bookService.getBooksByTitle(title, 0, 10, "ASC", "id");

        assertTrue(result.hasContent());
        assertEquals(1, result.getTotalElements());
        assertTrue(result.stream().anyMatch(book -> book.getTitle().toLowerCase().contains(title.toLowerCase())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Harry", "HARRY", "HaRrY"})
    void getBooksByTitle_WhenPartialTitleMatch_ShouldReturnPageOfBooks(String title) {
        Page<Book> result = bookService.getBooksByTitle(title, 0, 10, "ASC", "id");

        assertEquals(2, result.getTotalElements());
        assertTrue(result.stream().anyMatch(book -> book.getTitle().toLowerCase().contains("Harry".toLowerCase())));
        assertEquals("id: ASC", result.getPageable().getSort().toString());
    }

    @Test
    void getBooksByTitle_WhenTitleDidNotMatchAny_ShouldReturnEmptyPage() {
        Page<Book> result = bookService.getBooksByTitle("Non-existing title", 0, 10, "ASC", "id");

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Fantasy", "FaNtAsY", "FANTASY"})
    void getBooksByGenre_WhenGenreFullyMatch_ShouldReturnPageOfBooks(String title) {
        Page<Book> result = bookService.getBooksByGenre(title, 0, 10, "ASC", "id");
        assertEquals(2, result.getTotalElements());
        assertTrue(result.stream().anyMatch(book -> book.getGenre().equalsIgnoreCase(title)));
        assertEquals("id: ASC", result.getPageable().getSort().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"fant", "fAnT", "FANT"})
    void getBooksByGenre_WhenGenrePartiallyMatch_ShouldReturnPageOfBooks(String title) {
        Page<Book> result = bookService.getBooksByGenre(title, 0, 10, "ASC", "id");
        assertEquals(2, result.getTotalElements());
        assertTrue(result.stream().anyMatch(book -> book.getGenre().toLowerCase().contains(title.toLowerCase())));
    }

    @Test
    void getBooksByGenre_WhenGenreNotMatchAny_ShouldReturnEmptyPage() {
        Page<Book> result = bookService.getBooksByGenre("Non-matching Genre", 0 ,10, "ASC", "id");

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @ParameterizedTest
    @ValueSource(ints = {1997, 1998, 1949, 1945})
    void getBooksByPublicationYear_WhenPublicationYearMatch_ShouldReturnPageOfBooks(int publicationYear) {
        Page<Book> result = bookService.getBooksByPublicationYear(publicationYear, 0 , 10, "ASC", "id");
        assertEquals(1, result.getTotalElements());
        assertEquals(publicationYear, result.getContent().getFirst().getPublicationYear());
        assertEquals("id: ASC", result.getPageable().getSort().toString());
    }

    @Test
    void getBooksByPublicationYear_WhenPublicationYearNotMatch_ShouldReturnEmptyPage() {
        Page<Book> result = bookService.getBooksByPublicationYear(2025, 0 , 10, "ASC", "id");
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"J.K. Rowling", "j.K. rOwLiNg"})
    void getBooksByAuthorsName_WhenNameFullyMatch_ShouldReturnPageOfBooks(String name) {
        Page<Book> result = bookService.getBooksByAuthorsName(name, 0, 10, "ASC", "id");
        assertEquals(2, result.getTotalElements());
        assertTrue(result.stream().anyMatch(book -> book.getAuthor().getName().equalsIgnoreCase(name)));
        assertEquals("id: ASC", result.getPageable().getSort().toString());

    }

    @ParameterizedTest
    @ValueSource(strings = {"Rowling", "rOwLiNg"})
    void getBooksByAuthorsName_WhenNamePartiallyMatch_ShouldReturnPageOfBooks(String name) {
        Page<Book> result = bookService.getBooksByAuthorsName(name, 0, 10, "ASC", "id");

        assertEquals(2, result.getTotalElements());
        assertTrue(result.stream().anyMatch(book -> book.getAuthor().getName().toLowerCase().contains(name.toLowerCase())));
    }

    @Test
    void getBooksByAuthorsName_WhenNameDoesNotMatch_ShouldReturnEmptyPage() {
        Page<Book> result = bookService.getBooksByAuthorsName("Non-matching name", 0, 10, "ASC", "id");
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals("id: ASC", result.getPageable().getSort().toString());
    }


}