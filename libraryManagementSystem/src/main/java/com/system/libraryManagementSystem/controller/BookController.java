package com.system.libraryManagementSystem.controller;

import com.system.libraryManagementSystem.dto.BookDTO;
import com.system.libraryManagementSystem.mapper.BookMapper;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "Get All Books", security = @SecurityRequirement(name = ""))
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

    @Operation(summary = "Get Book By Id", security = @SecurityRequirement(name = ""))
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return new ResponseEntity<>(BookMapper.toDTO(book), HttpStatus.OK);
    }

    @Operation(summary = "Create Book")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @PostMapping
    public ResponseEntity<BookDTO> saveNewBook(@Valid @RequestBody BookDTO bookDTO) {
        Book book = BookMapper.toEntity(bookDTO);
        Book savedBook = bookService.saveNewBook(book);
        return new ResponseEntity<>(BookMapper.toDTO(savedBook), HttpStatus.CREATED);
    }

    @Operation(summary = "Update Book")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<BookDTO> updateBook(@Valid @RequestBody BookDTO updatedBookDTO) {
        Book newBook = BookMapper.toEntity(updatedBookDTO);
        Book updatedBook = bookService.updateBook(newBook.getId(), newBook);
        return new ResponseEntity<>(BookMapper.toDTO(updatedBook), HttpStatus.OK);
    }

    @Operation(summary = "Delete Book")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookById(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get Books By Title", security = @SecurityRequirement(name = ""))
    @GetMapping("/title")
    public ResponseEntity<Page<BookDTO>> getBooksByTitle(
            @RequestParam(defaultValue = "")String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(bookService.getBooksByTitle(title, page, size, sortDirection, sortField)
                .map(BookMapper::toDTO), HttpStatus.OK);
    }

    @Operation(summary = "Get Books By Genre", security = @SecurityRequirement(name = ""))
    @GetMapping("/genre")
    public ResponseEntity<Page<BookDTO>> getBooksByGenre(
            @RequestParam(defaultValue = "") String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(bookService.getBooksByGenre(genre, page, size, sortDirection, sortField)
                .map(BookMapper::toDTO), HttpStatus.OK);
    }

    @Operation(summary = "Get Books By Publication Year", security = @SecurityRequirement(name = ""))
    @GetMapping("/publication-year")
    public ResponseEntity<Page<BookDTO>> getBooksByPublicationYear(
            @RequestParam(defaultValue = "") int publicationYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(bookService.getBooksByPublicationYear(publicationYear, page, size, sortDirection, sortField)
                .map(BookMapper::toDTO), HttpStatus.OK);
    }

    @Operation(summary = "Get Books By Author's Name", security = @SecurityRequirement(name = ""))
    @GetMapping("/authors-name")
    public ResponseEntity<Page<BookDTO>> getBooksByAuthorsName(
            @RequestParam(defaultValue = "") String authorsName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(bookService.getBooksByAuthorsName(authorsName, page, size, sortDirection, sortField)
                .map(BookMapper::toDTO), HttpStatus.OK);
    }

}
