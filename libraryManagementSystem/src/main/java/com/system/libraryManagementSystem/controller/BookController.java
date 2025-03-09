package com.system.libraryManagementSystem.controller;

import com.system.libraryManagementSystem.dto.BookDTO;
import com.system.libraryManagementSystem.mapper.BookMapper;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "publicationYear") String sortField
    ) {
        return new ResponseEntity<>(
                bookService.getAllBooks(page, size, sortDirection, sortField)
                        .map(BookMapper::toDTO),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return new ResponseEntity<>(BookMapper.toDTO(book), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<BookDTO> saveNewBook(@Valid @Validated @RequestBody BookDTO bookDTO) {
        Book book = BookMapper.toEntity(bookDTO);
        Book savedBook = bookService.saveNewBook(book);
        return new ResponseEntity<>(BookMapper.toDTO(savedBook), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<BookDTO> updateBook(@Valid @RequestBody BookDTO updatedBookDTO) {
        Book newBook = BookMapper.toEntity(updatedBookDTO);
        Book updatedBook = bookService.updateBook(newBook.getId(), newBook);
        return new ResponseEntity<>(BookMapper.toDTO(updatedBook), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookById(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/title")
    public ResponseEntity<Page<BookDTO>> getBooksByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(bookService.getBooksByTitle(title, page, size, sortDirection, sortField)
                .map(BookMapper::toDTO), HttpStatus.OK);
    }

    @GetMapping("/genre")
    public ResponseEntity<Page<BookDTO>> getBooksByGenre(
            @RequestParam String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(bookService.getBooksByGenre(genre, page, size, sortDirection, sortField)
                .map(BookMapper::toDTO), HttpStatus.OK);
    }

    @GetMapping("/publication-year")
    public ResponseEntity<Page<BookDTO>> getBooksByPublicationYear(
            @RequestParam int publicationYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(bookService.getBooksByPublicationYear(publicationYear, page, size, sortDirection, sortField)
                .map(BookMapper::toDTO), HttpStatus.OK);
    }

    @GetMapping("/authors-name")
    public ResponseEntity<Page<BookDTO>> getBooksByAuthorsName(
            @RequestParam String authorsName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(bookService.getBooksByAuthorsName(authorsName, page, size, sortDirection, sortField)
                .map(BookMapper::toDTO), HttpStatus.OK);
    }

}
