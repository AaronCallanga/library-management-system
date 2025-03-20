package com.system.libraryManagementSystem.cache;

import com.system.libraryManagementSystem.exception.AuthorNotFoundException;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import com.system.libraryManagementSystem.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableCaching
@ActiveProfiles("test")
@SpringBootTest
class AuthorServiceCacheTest {

    @MockitoBean
    AuthorRepository authorRepository;      //we mocked the repository and its method so we dont need to use real db/repo and just focus on testing the cache

    @Autowired
    AuthorService authorService;

    @Autowired
    CacheManager cacheManager;

    private Author author;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("authors").clear();
        author = Author.builder()
                .id(1L)
                .name("J.K. Rowling")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();

        Book book1 = new Book(1L, "Harry Potter and the Philosopher's Stone", "Fantasy", 1997, author, new ArrayList<>());
        Book book2 = new Book(2L, "Harry Potter and the Chamber of Secrets", "Fantasy", 1998, author, new ArrayList<>());

        author.setPublishedBooks(List.of(book1, book2));

        when(authorRepository.findById(author.getId())).thenReturn(Optional.of(author));
        when(authorRepository.save(any(Author.class))).thenReturn(author);      //use for update service
    }

    @Test
    void getAuthorById_ShouldUseCache_AfterFirstCall() {
        assertNull(cacheManager.getCache("authors").get(author.getId()));

        Author firstCall = authorService.getAuthorById(author.getId());
        assertNotNull(firstCall);
        assertNotNull(cacheManager.getCache("authors").get(author.getId()));
        verify(authorRepository, times(1)).findById(author.getId());

        Author secondCall = authorService.getAuthorById(author.getId());
        assertNotNull(secondCall);
        assertEquals(secondCall, cacheManager.getCache("authors").get(author.getId(), Author.class));
        verify(authorRepository, times(1)).findById(author.getId());

    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void updateAuthor_ShouldUpdateCache() {
        assertNull(cacheManager.getCache("authors").get(author.getId()));
        Author newAuthor = Author.builder()
                .id(1L)
                .name("Updated Name")
                .biography("Updated Biography")
                .publishedBooks(List.of(
                        new Book(1L, "Updated Title", "Fantasy", 1997, author, new ArrayList<>()),
                        new Book(2L, "Updated Title 2", "Fantasy", 1998, author, new ArrayList<>())
                ))
                .build();

        Author updatedAuthor = authorService.updateAuthor(newAuthor.getId(), newAuthor);

        assertNotNull(cacheManager.getCache("authors").get(updatedAuthor.getId()));
        assertEquals(updatedAuthor, cacheManager.getCache("authors").get(updatedAuthor.getId(), Author.class));
        verify(authorRepository, times(1)).findById(updatedAuthor.getId());

        when(authorRepository.findById(updatedAuthor.getId())).thenReturn(Optional.of(updatedAuthor));
        Author getAuthorFromCache = authorService.getAuthorById(updatedAuthor.getId());

        assertNotNull(getAuthorFromCache);
        assertEquals(getAuthorFromCache, cacheManager.getCache("authors").get(getAuthorFromCache.getId(), Author.class));
        verify(authorRepository, times(1)).findById(updatedAuthor.getId()); //ensures that the data is from the cache
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void deleteAuthorById_ShouldEvictCache() {
        assertNull(cacheManager.getCache("authors").get(author.getId()));

        Author result = authorService.getAuthorById(author.getId());

        assertNotNull(cacheManager.getCache("authors").get(author.getId()));
        assertEquals(result, cacheManager.getCache("authors").get(author.getId(), Author.class));
        verify(authorRepository, times(1)).findById(author.getId());

        doNothing().when(authorRepository).deleteById(author.getId());

        authorService.deleteAuthorById(author.getId());

        assertNull(cacheManager.getCache("authors").get(author.getId()));       //ensures the the cache is evicted
        verify(authorRepository, times(1)).deleteById(author.getId());
        verify(authorRepository, times(1)).findById(author.getId());

        when(authorRepository.findById(author.getId())).thenReturn(Optional.empty());
        assertThrows(AuthorNotFoundException.class, () -> authorService.getAuthorById(author.getId()));
        verify(authorRepository, times(2)).findById(author.getId());        //ensures that the getById method is invoke because there are no cache anymore

    }
}