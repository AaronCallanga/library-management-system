package com.system.libraryManagementSystem.repository;

import com.system.libraryManagementSystem.model.BorrowingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    @Query("SELECT br FROM BorrowingRecord br JOIN br.book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<BorrowingRecord> findBorrowingRecordByBookTitle(String title, Pageable pageable);
    @Query("SELECT br FROM BorrowingRecord br JOIN br.member m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<BorrowingRecord> findBorrowingRecordByMemberName(String name, Pageable pageable);
    Page<BorrowingRecord> findByMemberEmail(String email, Pageable pageable);
    @Query("SELECT br FROM BorrowingRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate")
    Page<BorrowingRecord> findBorrowingRecordByBorrowDate(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);  //by date, optional time
    @Query("SELECT br FROM BorrowingRecord br WHERE br.returnDate BETWEEN :startDate AND :endDate")
    Page<BorrowingRecord> findBorrowingRecordByReturnDate(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
