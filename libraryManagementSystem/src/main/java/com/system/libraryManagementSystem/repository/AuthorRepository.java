package com.system.libraryManagementSystem.repository;

import com.system.libraryManagementSystem.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    @Query("SELECT a FROM Author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")        //you can provide your own query
    Page<Author> findAuthorByName(String name, Pageable pageable);
//    @Query("SELECT a FROM Author a WHERE a.biography LIKE %:keyword%")
    Page<Author> findByBiographyContainingIgnoreCase(String keyword, Pageable pageable);        //or use derived query, must usse specific naming
    @Query("SELECT a FROM Author a JOIN FETCH a.publishedBooks b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :bookTitle, '%'))")
    Page<Author> findByPublishedBookTitle(String bookTitle, Pageable pageable);

//    Optional<Author> findByPublishedBooksTitle(String bookTitle); //✅ use Optional<> to use method chain with .orElseThrow
}
