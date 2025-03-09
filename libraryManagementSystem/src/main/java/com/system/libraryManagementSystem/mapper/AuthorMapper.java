package com.system.libraryManagementSystem.mapper;

import com.system.libraryManagementSystem.dto.AuthorDTO;
import com.system.libraryManagementSystem.dto.format.BookTitleYearDTO;
import com.system.libraryManagementSystem.model.Author;

import java.util.List;
import java.util.stream.Collectors;

public class AuthorMapper {

    public static AuthorDTO toDTO(Author author) {      //static method can be called without the needs to create new object of AuthorMapper
        AuthorDTO authorDTO = new AuthorDTO();

        authorDTO.setId(author.getId());
        authorDTO.setName(author.getName());
        authorDTO.setBiography(author.getBiography());

        List<BookTitleYearDTO> books = author.getPublishedBooks() == null ? List.of() : //if author has no books published, it will only return a empty list. else continue the operation
                author.getPublishedBooks()
                        .stream()
                        .map(book -> new BookTitleYearDTO(book.getTitle(), book.getPublicationYear()))
                        .collect(Collectors.toList());

        authorDTO.setPublishedBooks(books);
        return authorDTO;
    }

    public static Author toEntity(AuthorDTO authorDTO) {
        Author author = new Author();

        author.setId(authorDTO.getId());
        author.setName(authorDTO.getName());
        author.setBiography(authorDTO.getBiography());

        return author;
    }

//    public static Author toEntity(AuthorDTO authorDTO) {
//        Author author = new Author();
//
//        author.setId(authorDTO.getId());
//        author.setName(authorDTO.getName());
//        author.setBiography(authorDTO.getBiography());
//
//        List<Book> books = authorDTO.getPublishedBooks() == null ? List.of() :
//                authorDTO.getPublishedBooks()
//                        .stream()
//                        .map(book -> new Book(book.getTitle(), book.getPublicationYear()))
//                        .collect(Collectors.toList());
//
//        author.setPublishedBooks(books);
//
//        return author;
//
//    }
}
