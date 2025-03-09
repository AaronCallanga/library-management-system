package com.system.libraryManagementSystem.mapper;

import com.system.libraryManagementSystem.dto.BookDTO;
import com.system.libraryManagementSystem.exception.AuthorNotFoundException;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component  //make it as a bean and let Spring maanged it, and Spring can inject the @Autowired field. Spring can only inject if it is a spring-managed bean (@Service @Repository @Controller / @RestController)
public class BookMapper {

    private static AuthorRepository authorRepository;  //cant inject wth @Autowired nor constructor, because Spring manages depedencies in instance level

    @Autowired     // use of method so we can inject here the dependencies
    public void setAuthorRepository( AuthorRepository authorRepository) {
        BookMapper.authorRepository = authorRepository;
    }

    public static BookDTO toDTO(Book book) {
        BookDTO bookDTO = new BookDTO();

        bookDTO.setId(book.getId());
        bookDTO.setTitle(book.getTitle());
        bookDTO.setGenre(book.getGenre());
        bookDTO.setPublicationYear(book.getPublicationYear());
        bookDTO.setAuthorId(book.getAuthor().getId());
        bookDTO.setAuthorName(book.getAuthor().getName());

        return bookDTO;
    }

    public static Book toEntity(BookDTO bookDTO) {
        Book book = new Book();
        Author author = authorRepository.findById(bookDTO.getAuthorId())
                .orElseThrow(() -> new AuthorNotFoundException("Author not found with the id: " + bookDTO.getAuthorId()));

        if (!author.getName().equalsIgnoreCase(bookDTO.getAuthorName())) {
            throw new AuthorNotFoundException(bookDTO.getAuthorName() + " did not match the author's name with the id of: " + bookDTO.getAuthorId());
        } //make it an anotation

        book.setId(bookDTO.getId());
        book.setTitle(bookDTO.getTitle());
        book.setGenre(bookDTO.getGenre());
        book.setPublicationYear(bookDTO.getPublicationYear());
        book.setAuthor(author);

        return book;
    }
}
