package com.system.libraryManagementSystem.service;

import com.system.libraryManagementSystem.exception.BorrowingRecordNotFound;
import com.system.libraryManagementSystem.model.BorrowingRecord;
import com.system.libraryManagementSystem.repository.BorrowingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;


@Service
public class BorrowingRecordService {

    @Autowired
    private BorrowingRecordRepository borrowingRecordRepository;

    public Page<BorrowingRecord> getAllBorrowingRecords(int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortField)
        );
        return borrowingRecordRepository.findAll(pageRequest);
    }

    @Cacheable(cacheNames = "borrowing records", key = "#id")
    public BorrowingRecord getBorrowingRecordById(Long id) {
        return borrowingRecordRepository.findById(id)
                .orElseThrow(() -> new BorrowingRecordNotFound("Record not found with the id: " + id));
    }

    public BorrowingRecord saveNewBorrowingRecord(BorrowingRecord borrowingRecord) {
        return borrowingRecordRepository.save(borrowingRecord);
    }

    @CachePut(cacheNames = "borrowing records", key = "#id")
    public BorrowingRecord updateBorrowingRecord(Long id, BorrowingRecord updatedBorrowingRecord) {
        BorrowingRecord record = borrowingRecordRepository.findById(id)
                .orElseThrow(() -> new BorrowingRecordNotFound("Record not found with the id: " + id));

        record.setBook(updatedBorrowingRecord.getBook());
        record.setMember(updatedBorrowingRecord.getMember());
        record.setBorrowDate(updatedBorrowingRecord.getBorrowDate());
        record.setReturnDate(updatedBorrowingRecord.getReturnDate());

        return borrowingRecordRepository.save(record);
    }

    @CacheEvict(cacheNames = "borrowing records", key = "#id")
    public void deleteBorrowingRecordById(Long id) {
        borrowingRecordRepository.deleteById(id);
    }

    public Page<BorrowingRecord> getBorrowingRecordByMemberEmail(String email, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return borrowingRecordRepository.findByMemberEmail(email, pageRequest);
    }

    public Page<BorrowingRecord> getBorrowingRecordByBookTitle(String title, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return borrowingRecordRepository.findBorrowingRecordByBookTitle(title, pageRequest);
    }

    public Page<BorrowingRecord> getBorrowingRecordByMemberName(String name, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return borrowingRecordRepository.findBorrowingRecordByMemberName(name, pageRequest);
    }

    public Page<BorrowingRecord> getBorrowingRecordByBorrowDate(LocalDateTime borrowDate, int page, int size, String sortDirection, String sortField) {
        LocalDateTime startDate;
        LocalDateTime endDate;

        //this handles optional time input, whether we see a record for a specific time and date or the whole time for a specific date
        if (borrowDate.toLocalTime().equals(LocalTime.MIDNIGHT)) {  //00:00:00 atStartOfDay == MIDNIGHT
            // User provided only date, so we search for the whole day
            startDate = borrowDate.with(LocalTime.MIN);  // 00:00:00
            endDate = borrowDate.with(LocalTime.MAX);  // 23:59:59.999999999
        } else {
            // User provided full datetime, so we search exactly for that timestamp
            startDate = borrowDate;
            endDate = borrowDate;
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return borrowingRecordRepository.findBorrowingRecordByBorrowDate(startDate, endDate, pageRequest);
    }

    public Page<BorrowingRecord> getBorrowingRecordByReturnDate(LocalDateTime returnDate, int page, int size, String sortDirection, String sortField) {
        LocalDateTime startDate;
        LocalDateTime endDate;

        if (returnDate.toLocalTime().equals(LocalTime.MIDNIGHT)) { //check if midnight  00:00:00
            startDate = returnDate.with(LocalTime.MIN);
            endDate = returnDate.with(LocalTime.MAX);
        } else {
            startDate = returnDate;
            endDate = returnDate;
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return borrowingRecordRepository.findBorrowingRecordByReturnDate(startDate, endDate, pageRequest);
    }
//only admin and librarian
    public void approveBorrowRequest(Long borrowingRecordId) {
        BorrowingRecord record = borrowingRecordRepository.findById(borrowingRecordId)
                .orElseThrow(() -> new BorrowingRecordNotFound("Record not found with the id: " + borrowingRecordId));
        record.setApproved(true);
        borrowingRecordRepository.save(record);
    }

}
