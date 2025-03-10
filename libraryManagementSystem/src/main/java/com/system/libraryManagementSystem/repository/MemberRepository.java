package com.system.libraryManagementSystem.repository;

import com.system.libraryManagementSystem.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    //by name, by borrowedBooks(admin)
    @Query("SELECT m FROM Member m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Member> findMemberByName(String name, Pageable pageable);
    @Query("SELECT DISTINCT m FROM Member m JOIN m.borrowedBooks b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")    //same as joined table
    Page<Member> findMemberByBorrowedBookTitle(String title, Pageable pageable);
    Optional<Member> findByEmail(String email);
//    boolean existByEmail(String email);

//    @Query("SELECT m FROM Member m JOIN m.roles r WHERE r.name = :roleName")
//    Page<Member> findMembersByRole(String roleName, Pageable pageable);
}
