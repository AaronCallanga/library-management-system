package com.system.libraryManagementSystem.service;

import com.system.libraryManagementSystem.exception.BookNotFoundException;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;


    public Page<Book> getAllBooks(int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortField)
        );
        return bookRepository.findAll(pageRequest);
    }

    @Cacheable(cacheNames = "books", key = "#id")
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with the id: " + id));
    }

    public Book saveNewBook(Book book) {
        return bookRepository.save(book);
    }

    @CachePut(cacheNames = "books", key = "#id")
    public Book updateBook(Long id, Book updatedBook) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with the id: " + id));

        if (book.equals(updatedBook)) return updatedBook;

        book.setTitle(updatedBook.getTitle());
        book.setGenre(updatedBook.getGenre());
        book.setPublicationYear(updatedBook.getPublicationYear());
        book.setAuthor(book.getAuthor());

        return bookRepository.save(book);
    }

    @CacheEvict(cacheNames = "books", key = "#id")
    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }

    public Page<Book> getBooksByTitle(String title, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return bookRepository.findByTitleContainingIgnoreCase(title, pageRequest);
    }

    public Page<Book> getBooksByGenre(String genre, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return bookRepository.findBooksByGenre(genre, pageRequest);
    }

    public Page<Book> getBooksByPublicationYear(int publicationYear, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return bookRepository.findBooksByPublicationYear(publicationYear, pageRequest);
    }
    //get books by range of publication year
    public Page<Book> getBooksByAuthorsName(String authorsName, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return bookRepository.findBooksByAuthorsName(authorsName, pageRequest);
    }
}
