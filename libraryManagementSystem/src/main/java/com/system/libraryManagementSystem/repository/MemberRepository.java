package com.system.libraryManagementSystem.repository;

import com.system.libraryManagementSystem.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    //by name, by borrowedBooks(admin)
    @Query("SELECT m FROM Member m WHERE m.name LIKE %:name%")
    Page<Member> findMemberByName(String name, Pageable pageable);
    @Query("SELECT DISTINCT m FROM Member m JOIN m.borrowedBooks b WHERE b.title LIKE %:title%")    //same as joined table
    Page<Member> findMemberByBorrowedBookTitle(String title, Pageable pageable);

}
