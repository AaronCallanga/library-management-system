package com.system.libraryManagementSystem.controller;

import com.system.libraryManagementSystem.dto.AuthorDTO;
import com.system.libraryManagementSystem.mapper.AuthorMapper;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.service.AuthorService;
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
@RequestMapping("/authors")
public class AuthorController {

    //@Valid for @RequestBody, ensures the object is validation based on the constraint declared in their class e.g @NotNull
    //@Validated for Method Parameter, and allows Group validation

    @Autowired
    AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Operation(summary = "Get All Authors", security = @SecurityRequirement(name = ""))
    @GetMapping
    public  ResponseEntity<Page<AuthorDTO>> getAllAuthors(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "ASC") String sortDirection,
        @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                authorService.getAllAuthors(page, size, sortDirection, sortField)
                        .map(AuthorMapper::toDTO),
                HttpStatus.OK
        );
    }

    @Operation(summary = "Get Author By ID", security = @SecurityRequirement(name = ""))
    @GetMapping("/{id}")
    public ResponseEntity<AuthorDTO> getAuthorById(@PathVariable Long id) {
        Author author = authorService.getAuthorById(id);

        return new ResponseEntity<>(AuthorMapper.toDTO(author), HttpStatus.OK);
    }

    @Operation(summary = "Create Author")
    @PreAuthorize("hasAuthority('ROLE_LIBRARIAN') or hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public  ResponseEntity<AuthorDTO> saveNewAuthor(@Valid @RequestBody AuthorDTO authorDTO) {
        Author author = AuthorMapper.toEntity(authorDTO);
        Author savedAuthor = authorService.saveNewAuthor(author);
        return new ResponseEntity<>(AuthorMapper.toDTO(savedAuthor), HttpStatus.CREATED);
    }

    @Operation(summary = "Update Author")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<AuthorDTO> updateAuthor(@Valid @RequestBody AuthorDTO updatedAuthorDTO) { //baka no need na @PathVariable, just search using the id of the updatedAuthorDTO
        Author newAuthor = AuthorMapper.toEntity(updatedAuthorDTO);
        Author updatedAuthor = authorService.updateAuthor(newAuthor.getId(), newAuthor);
        return new ResponseEntity<>(AuthorMapper.toDTO(updatedAuthor), HttpStatus.OK);
    }

    @Operation(summary = "Delete Author")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthorById(@PathVariable Long id) {
        authorService.deleteAuthorById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get Authors By Published Book Title", security = @SecurityRequirement(name = ""))
    @GetMapping("/published-book")
    public ResponseEntity<Page<AuthorDTO>> getAuthorByPublishedBookTitle(
            @RequestParam(defaultValue = "") String bookTitle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                authorService.getAuthorsByPublishedBookTitle(bookTitle, page, size, sortDirection, sortField)
                        .map(AuthorMapper::toDTO),
                HttpStatus.OK
        );
    }

    @Operation(summary = "Get Authors By Name", security = @SecurityRequirement(name = ""))
    @GetMapping("/name")
    public ResponseEntity<Page<AuthorDTO>> getAuthorsByName(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(authorService.getAuthorsByName(name, page, size, sortDirection, sortField)
                .map(AuthorMapper::toDTO), HttpStatus.OK);
    }

    @Operation(summary = "Get Authors By Biography", security = @SecurityRequirement(name = ""))
    @GetMapping("/biography")
    public ResponseEntity<Page<AuthorDTO>> getAuthorsByBiography(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(authorService.getAuthorsByBiographyKeyword(keyword, page, size, sortDirection, sortField)
                .map(AuthorMapper::toDTO), HttpStatus.OK);
    }
}
