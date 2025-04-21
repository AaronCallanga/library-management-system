package com.system.libraryManagementSystem.controller;

import com.system.libraryManagementSystem.dto.BorrowingRecordDTO;
import com.system.libraryManagementSystem.mapper.BorrowingRecordMapper;
import com.system.libraryManagementSystem.model.BorrowingRecord;
import com.system.libraryManagementSystem.service.BorrowingRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@RestController
@RequestMapping("/borrowing-record")
public class BorrowingRecordController { //maybe you can also create a api end points where regular member can get their own record by id, title, return date

    @Autowired
    BorrowingRecordService borrowingRecordService;

    public BorrowingRecordController(BorrowingRecordService borrowingRecordService) {
        this.borrowingRecordService = borrowingRecordService;
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
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

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<BorrowingRecordDTO> getBorrowingRecordById(@PathVariable Long id) {
        BorrowingRecord borrowingRecord = borrowingRecordService.getBorrowingRecordById(id);
        return new ResponseEntity<>(BorrowingRecordMapper.toDTO(borrowingRecord), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @PostMapping
    public ResponseEntity<BorrowingRecordDTO> saveNewBorrowingRecord(@Valid @RequestBody BorrowingRecordDTO recordDTO) {        //borrow request
        BorrowingRecord borrowingRecord = BorrowingRecordMapper.toEntity(recordDTO);
        BorrowingRecord savedBorrowingRecord = borrowingRecordService.saveNewBorrowingRecord(borrowingRecord);
        return new ResponseEntity<>(BorrowingRecordMapper.toDTO(savedBorrowingRecord), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<BorrowingRecordDTO> updateBorrowingRecord(@Valid @RequestBody BorrowingRecordDTO updatedBorrowingRecordDTO) {
        BorrowingRecord newBorrowingRecord = BorrowingRecordMapper.toEntity(updatedBorrowingRecordDTO);
        BorrowingRecord updatedBorrowingRecord = borrowingRecordService.updateBorrowingRecord(newBorrowingRecord.getId(), newBorrowingRecord);
        return new ResponseEntity<>(BorrowingRecordMapper.toDTO(updatedBorrowingRecord), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBorrowingRecordById(@PathVariable Long id) {
        borrowingRecordService.deleteBorrowingRecordById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("authentication.name == #authentication.name") // Ensures user can only access their own records
    @GetMapping("/own")
    public ResponseEntity<Page<BorrowingRecordDTO>> getOwnBorrowingRecord(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField,
            Authentication authentication
    ) {
        return new ResponseEntity<>(
                borrowingRecordService.getBorrowingRecordByMemberEmail(authentication.getName(), page, size, sortDirection, sortField)
                        .map(BorrowingRecordMapper::toDTO),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#recordDTO.memberEmail == authentication.name")
    @PostMapping("/request")
    public ResponseEntity<BorrowingRecordDTO> sendOwnBorrowingRequest(@Valid @RequestBody BorrowingRecordDTO recordDTO) {        //borrow request
        BorrowingRecord borrowingRecord = BorrowingRecordMapper.toEntity(recordDTO);
        BorrowingRecord savedBorrowingRecord = borrowingRecordService.saveNewBorrowingRecord(borrowingRecord);
        return new ResponseEntity<>(BorrowingRecordMapper.toDTO(savedBorrowingRecord), HttpStatus.CREATED);
    }

    @PreAuthorize("#updatedBorrowingRecordDTO.memberEmail == authentication.name")
    @PutMapping("/update/own")
    public ResponseEntity<BorrowingRecordDTO> updateOwnBorrowingRecordDTO(@Valid @RequestBody BorrowingRecordDTO updatedBorrowingRecordDTO) {
        BorrowingRecord newBorrowingRecord = BorrowingRecordMapper.toEntity(updatedBorrowingRecordDTO);
        BorrowingRecord updatedBorrowingRecord = borrowingRecordService.updateBorrowingRecord(newBorrowingRecord.getId(), newBorrowingRecord);
        return new ResponseEntity<>(BorrowingRecordMapper.toDTO(updatedBorrowingRecord), HttpStatus.OK);
    }

    @PreAuthorize("@borrowingRecordService.isMemberOwnerOfTheRecord(#id, authentication)")
    @DeleteMapping("/own/{id}")
    public ResponseEntity<Void> deleteOwnBorrowingRecordById(@PathVariable Long id) {
        borrowingRecordService.deleteBorrowingRecordById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/book-title")
    public ResponseEntity<Page<BorrowingRecordDTO>> getBorrowingRecordByBookTitle(
            @RequestParam(defaultValue = "") String title,
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

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/member-name")
    public ResponseEntity<Page<BorrowingRecordDTO>> getBorrowingRecordByMemberName(
            @RequestParam(defaultValue = "") String name,
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

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/borrow-date")
    public ResponseEntity<Page<BorrowingRecordDTO>> getBorrowingRecordByBorrowDate(
            @RequestParam(defaultValue = "13-Feb-2025") String borrowDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        //if (returnDate == null) returnDate = currentTime
        LocalDateTime parsedBorrowDate = parseDateTime(borrowDate);
        return new ResponseEntity<>(
                borrowingRecordService.getBorrowingRecordByBorrowDate(parsedBorrowDate, page, size, sortDirection, sortField)
                        .map(BorrowingRecordMapper::toDTO),
                HttpStatus.OK
                );
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/return-date")
    public ResponseEntity<Page<BorrowingRecordDTO>> getBorrowingRecordsByReturnDate(
            @RequestParam(defaultValue = "13-Feb-2025") String returnDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        //if (returnDate == null) returnDate = currentTime
        LocalDateTime parsedReturnDate = parseDateTime(returnDate);
        return new ResponseEntity<>(
                borrowingRecordService.getBorrowingRecordByReturnDate(parsedReturnDate, page, size, sortDirection, sortField)
                        .map(BorrowingRecordMapper::toDTO),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')") // Ensures user can only access their own records
    @GetMapping("/email")
    public ResponseEntity<Page<BorrowingRecordDTO>> getBorrowingRecordByMemberEmail(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "") String email
    ) {
        return new ResponseEntity<>(
                borrowingRecordService.getBorrowingRecordByMemberEmail(email, page, size, sortDirection, sortField)
                        .map(BorrowingRecordMapper::toDTO),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approveBorrowRequest(@PathVariable Long id) {
        borrowingRecordService.approveBorrowRequest(id);
        return new ResponseEntity<>("Request has been approved", HttpStatus.ACCEPTED);
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
