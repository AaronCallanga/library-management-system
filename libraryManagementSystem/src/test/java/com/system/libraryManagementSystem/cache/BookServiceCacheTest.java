package com.system.libraryManagementSystem.cache;


import com.system.libraryManagementSystem.exception.BookNotFoundException;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableCaching
@ActiveProfiles("test")
@SpringBootTest //we dont need to use MockitoExtension because we are autowiring real dependencies and not manually injecting mocks @InjectMocks
public class BookServiceCacheTest {

    @Autowired
    private BookService bookService;  // This is the real service

    @MockitoBean
    private BookRepository bookRepository;  // Mocked repository (correct approach)

    @Autowired
    private CacheManager cacheManager;

    private Book book;
    private Author author;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("books").clear();
        author = new Author(1L, "J.K. Rowling", "Famous author", null);
        book = new Book(1L, "Harry Potter", "Fantasy", 1997, author, null);

        // Mock repository behavior
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
    }

    @Test
    void getBookById_ShouldUseCache_AfterFirstCall() {
        Book firstCall = bookService.getBookById(book.getId());
        assertNotNull(firstCall);
        assertEquals(book.getId(), firstCall.getId());
        verify(bookRepository, times(1)).findById(book.getId());

        Book secondCall = bookService.getBookById(book.getId());
        assertNotNull(secondCall);
        assertEquals(book.getId(), firstCall.getId());
        //verify that we use the cache and not execute another operations in the database
        verify(bookRepository, times(1)).findById(book.getId());

        assertNotNull(cacheManager.getCache("books").get(book.getId()));
    }

    @Test
    void updateBook_ShouldUpdateCache() {
        Book newBook = new Book(1L, "Harry Potter 1", "Horror", 1997, author, null);
        when(bookRepository.findById(newBook.getId())).thenReturn(Optional.of(newBook));

        Book updatedBook = bookService.updateBook(newBook.getId(), newBook);

        verify(bookRepository, times(1)).findById(newBook.getId());
        assertNotNull(updatedBook);
        assertNotNull(cacheManager.getCache("books").get(updatedBook.getId()));
        assertEquals(updatedBook, (Book) cacheManager.getCache("books").get(updatedBook.getId()).get());

        Book getUpdatedBookInCache = bookService.getBookById(updatedBook.getId());
        assertNotNull(getUpdatedBookInCache);
        verify(bookRepository, times(1)).findById(updatedBook.getId()); //ensures that the data is from the cache
    }

    @Test
    void deleteBookById_ShouldEvictCache() {
        bookService.getBookById(book.getId());

        assertNotNull(cacheManager.getCache("books").get(book.getId()));

        doNothing().when(bookRepository).deleteById(book.getId());
        when(bookRepository.findById(book.getId())).thenReturn(Optional.empty());

        bookService.deleteBookById(book.getId());

        assertNull(cacheManager.getCache("books").get(book.getId()));
        assertThrows(BookNotFoundException.class, () -> bookService.getBookById(book.getId()));
        verify(bookRepository, times(2)).findById(book.getId());
    }
}
