package com.system.libraryManagementSystem.repository;

import com.system.libraryManagementSystem.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("SELECT DISTINCT b FROM Book b JOIN b.author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Book> findBooksByAuthorsName(String name, Pageable pageable);
//    @Query("SELECT b FROM Book b WHERE b.title LIKE %:title%") case sensitive
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);    //derived query, jpa provides the query based on its name
    @Query("SELECT b from Book b WHERE LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))")
    Page<Book> findBooksByGenre(String genre, Pageable pageable);
    Page<Book> findBooksByPublicationYear(int publicationYear, Pageable pageable);
}
