package com.system.libraryManagementSystem.controller;

import com.system.libraryManagementSystem.dto.BorrowingRecordDTO;
import com.system.libraryManagementSystem.dto.validation.groups.OnCreate;
import com.system.libraryManagementSystem.dto.validation.groups.OnUpdate;
import com.system.libraryManagementSystem.mapper.BorrowingRecordMapper;
import com.system.libraryManagementSystem.model.BorrowingRecord;
import com.system.libraryManagementSystem.service.BorrowingRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/borrowing-record")
public class BorrowingRecordController {

    @Autowired
    BorrowingRecordService borrowingRecordService;

    public BorrowingRecordController(BorrowingRecordService borrowingRecordService) {
        this.borrowingRecordService = borrowingRecordService;
    }

    @GetMapping
    public ResponseEntity<Page<BorrowingRecordDTO>> getAllBorrowingRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                borrowingRecordService.getAllBorrowingRecords(page, size, sortDirection, sortField)
                        .map(BorrowingRecordMapper::toDTO),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowingRecordDTO> getBorrowingRecordById(@PathVariable Long id) {
        BorrowingRecord borrowingRecord = borrowingRecordService.getBorrowingRecordById(id);
        return new ResponseEntity<>(BorrowingRecordMapper.toDTO(borrowingRecord), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<BorrowingRecordDTO> saveNewBorrowingRecord(@Valid @RequestBody BorrowingRecordDTO recordDTO) {
        BorrowingRecord borrowingRecord = BorrowingRecordMapper.toEntity(recordDTO);
        BorrowingRecord savedBorrowingRecord = borrowingRecordService.saveNewBorrowingRecord(borrowingRecord);
        return new ResponseEntity<>(BorrowingRecordMapper.toDTO(savedBorrowingRecord), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<BorrowingRecordDTO> updateBorrowingRecordDTO(@Valid @RequestBody BorrowingRecordDTO updatedBorrowingRecordDTO) {
        BorrowingRecord newBorrowingRecord = BorrowingRecordMapper.toEntity(updatedBorrowingRecordDTO);
        BorrowingRecord updatedBorrowingRecord = borrowingRecordService.updateBorrowingRecord(newBorrowingRecord.getId(), newBorrowingRecord);
        return new ResponseEntity<>(BorrowingRecordMapper.toDTO(updatedBorrowingRecord), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBorrowingRecordById(@PathVariable Long id) {
        borrowingRecordService.deleteBorrowingRecordById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/book-title")
    public ResponseEntity<Page<BorrowingRecordDTO>> getBorrowingRecordByBookTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                borrowingRecordService.getBorrowingRecordByBookTitle(title, page, size ,sortDirection, sortField)
                        .map(BorrowingRecordMapper::toDTO),
                HttpStatus.OK
        );
    }
    @GetMapping("/member-name")
    public ResponseEntity<Page<BorrowingRecordDTO>> getBorrowingRecordByMemberName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                borrowingRecordService.getBorrowingRecordByMemberName(name, page, size ,sortDirection, sortField)
                        .map(BorrowingRecordMapper::toDTO),
                HttpStatus.OK
        );
    }

    @GetMapping("/borrow-date")
    public ResponseEntity<Page<BorrowingRecordDTO>> getBorrowingRecordByBorrowDate(
            @RequestParam String borrowDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        LocalDateTime parsedBorrowDate = parseDateTime(borrowDate);
        return new ResponseEntity<>(
                borrowingRecordService.getBorrowingRecordByBorrowDate(parsedBorrowDate, page, size, sortDirection, sortField)
                        .map(BorrowingRecordMapper::toDTO),
                HttpStatus.OK
                );
    }

    @GetMapping("/return-date")
    public ResponseEntity<Page<BorrowingRecordDTO>> getBorrowingRecordsByReturnDate(
            @RequestParam String returnDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        LocalDateTime parsedReturnDate = parseDateTime(returnDate);
        return new ResponseEntity<>(
                borrowingRecordService.getBorrowingRecordByReturnDate(parsedReturnDate, page, size, sortDirection, sortField)
                        .map(BorrowingRecordMapper::toDTO),
                HttpStatus.OK
        );
    }

    public LocalDateTime parseDateTime(String dateString) {
        try {
            // If user provides full date and time
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
            return LocalDateTime.parse(dateString, dateTimeFormatter);
        } catch (Exception e) {
            // If user provides only a date, set time to 00:00:00   //midnight
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
            return LocalDate.parse(dateString, dateTimeFormatter).atStartOfDay();   //parse as only date then add maybe 00:00 (atStartOfDay)
        }
    }


}
