package com.system.libraryManagementSystem.service;

import com.system.libraryManagementSystem.exception.AuthorNotFoundException;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class AuthorService {

    @Autowired
    AuthorRepository authorRepository;

    public Page<Author> getAllAuthors(int page, int size, String sortDirection, String sortField) {

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortField)
        );

        return authorRepository.findAll(pageRequest);
    }

    @Cacheable(cacheNames = "authors", key = "#id")
    public Author getAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found with the id: " + id));
    }

    public Author saveNewAuthor(Author author) {
        return authorRepository.save(author);
    }

    @CachePut(cacheNames = "authors", key = "#id")  //if you passed the whole object only  without the Long id, you can use #updatedAuthor.id
    public Author updateAuthor(Long id, Author updatedAuthor) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found with the id: " + id));

        if (author.equals(updatedAuthor)) return author;    //so no need to save the updatedAuthor

        author.setName(updatedAuthor.getName());
        author.setBiography(updatedAuthor.getBiography());

        return authorRepository.save(author);
    }

    @CacheEvict(cacheNames = "authors", key = "#id")
    public void deleteAuthorById(Long id) {
        authorRepository.deleteById(id);
    }

    public Page<Author> getAuthorsByPublishedBookTitle(String bookTitle, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
       return authorRepository.findByPublishedBookTitle(bookTitle, pageRequest);
    }

    public Page<Author> getAuthorsByName(String name, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return authorRepository.findAuthorByName(name, pageRequest);

    }

    public Page<Author> getAuthorsByBiographyKeyword(String keyword, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return authorRepository.findByBiographyContainingIgnoreCase(keyword, pageRequest);
    }

}
