package com.system.libraryManagementSystem.service;

import com.system.libraryManagementSystem.exception.AuthorNotFoundException;
import com.system.libraryManagementSystem.exception.BookNotFoundException;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private PageRequest  pageRequest;
    private List<Book> bookList;
    private Book book1, book2, book3, book4;
    private Author author1, author2;

    @BeforeEach
    void setUp() {
        author1 = Author.builder()
                .id(1L)
                .name("J.K. Rowling")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();

        author2 = Author.builder()
                .id(2L)
                .name("George Orwell")
                .biography("English novelist, best known for '1984' and 'Animal Farm'.")
                .publishedBooks(new ArrayList<>())
                .build();

        book1 = new Book(1L, "Harry Potter and the Philosopher's Stone", "Fantasy", 1997, author1, new ArrayList<>());
        book2 = new Book(2L, "Harry Potter and the Chamber of Secrets", "Fantasy", 1998, author1, new ArrayList<>());
        book3 = new Book(3L, "1984", "Dystopian", 1949, author2, new ArrayList<>());
        book4 = new Book(4L, "Animal Farm", "Political Satire", 1945, author2, new ArrayList<>());

        author1.setPublishedBooks(List.of(book1, book2));
        author2.setPublishedBooks(List.of(book3, book4));

        pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.fromString("ASC"), "id"));
        bookList = Arrays.asList(book1, book2, book3, book4);
    }

    @Test
    void getAllBooks_WhenBooksExist_ShouldReturnPageOfBooks() {
        Page<Book> bookPage = new PageImpl<>(bookList, pageRequest, bookList.size());
        when(bookRepository.findAll(pageRequest)).thenReturn(bookPage);

        Page<Book> result = bookService.getAllBooks(0, 10, "ASC", "id");

        assertTrue(result.hasContent());
        assertEquals(4, result.getTotalElements());
        assertEquals("id: ASC", result.getPageable().getSort().toString());
        verify(bookRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getAllBooks_WhenBooksDoesNotExist_ShouldReturnEmptyPage() {
        List<Book> emptyBooks = new ArrayList<>();
        Page<Book> bookPage = new PageImpl<>(emptyBooks, pageRequest, emptyBooks.size());
        when(bookRepository.findAll(pageRequest)).thenReturn(bookPage);

        Page<Book> result = bookService.getAllBooks(0, 10, "ASC", "id");

        assertEquals(0, result.getTotalElements());
        verify(bookRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getBookById_WhenIdExist_ShouldReturnBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));

        Book result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void getBookById_WhenIdDoesNotExist_ShouldThrowException() {

        BookNotFoundException result = assertThrows(BookNotFoundException.class, () -> bookService.getBookById(99L));

        assertNotNull(result);
        assertEquals("Book not found with the id: 99", result.getMessage());
        verify(bookRepository, times(1)).findById(99L);
    }

    @Test
    void saveNewBook() {
        Book book5 = new Book(5L, "New Book", "Book Title", 2025, author1, new ArrayList<>());
        when(bookRepository.save(book5)).thenReturn(book5);

        Book result = bookService.saveNewBook(book5);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        verify(bookRepository, times(1)).save(book5);
    }

    @Test
    void updateBook_WhenBookAreChanged_ShouldUpdateSuccessfully() {
        Book updatedBook = new Book(1L, "Updated Title", "Fantasy", 1997, author1, new ArrayList<>());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.save(book1)).thenReturn(book1);

        Book result = bookService.updateBook(1L, updatedBook);

        assertEquals("Updated Title", result.getTitle());
        assertEquals(1L, result.getId());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(book1);
    }

    @Test
    void updateBook_WithTheSameData_ShouldAvoidUnnecessaryUpdates() {
        Book updatedBook = new Book(1L, "Harry Potter and the Philosopher's Stone", "Fantasy", 1997, author1, new ArrayList<>());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));

        Book result = bookService.updateBook(1L, updatedBook);

        assertEquals(result, updatedBook);
        assertEquals(1L, result.getId());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(0)).save(book1);
    }

    @Test
    void deleteBookById() {
        doNothing().when(bookRepository).deleteById(book1.getId());
        when(bookRepository.findById(book1.getId())).thenReturn(Optional.empty());

        bookService.deleteBookById(author1.getId());

        assertThrows(BookNotFoundException.class, () -> bookService.getBookById(book1.getId()));
        verify(bookRepository,times(1)).deleteById(author1.getId());
    }
}