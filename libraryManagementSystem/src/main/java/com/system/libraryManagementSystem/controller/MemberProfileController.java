package com.system.libraryManagementSystem.controller;


import com.system.libraryManagementSystem.dto.MemberProfileDTO;
import com.system.libraryManagementSystem.mapper.MemberProfileMapper;
import com.system.libraryManagementSystem.model.MemberProfile;
import com.system.libraryManagementSystem.service.MemberProfileService;
import com.system.libraryManagementSystem.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


@RestController
@RequestMapping("/member-profile")
public class MemberProfileController {

    @Autowired
    MemberProfileService memberProfileService;
    @Autowired
    MemberService memberService;

    public MemberProfileController(MemberProfileService memberProfileService) {
        this.memberProfileService = memberProfileService;
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<MemberProfileDTO>> getAllMemberProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                memberProfileService.getAllMemberProfiles(page, size, sortDirection, sortField)
                        .map(MemberProfileMapper::toDTO),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<MemberProfileDTO> getMemberProfileById(@PathVariable Long id) {
        MemberProfile memberProfile = memberProfileService.getMemberProfileById(id);
        return new ResponseEntity<>(MemberProfileMapper.toDTO(memberProfile), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<MemberProfileDTO> updateMemberProfile(@Valid @RequestBody MemberProfileDTO updatedMemberProfileDTO) {
        MemberProfile newMemberProfile = MemberProfileMapper.toEntity(updatedMemberProfileDTO);
        MemberProfile updatedMemberProfile = memberProfileService.updateMemberProfile(newMemberProfile.getId(), newMemberProfile);
        return new ResponseEntity<>(MemberProfileMapper.toDTO(updatedMemberProfile), HttpStatus.OK);
    }
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')") //this is for admin and librarian
    @PostMapping
    public ResponseEntity<MemberProfileDTO> saveNewMemberProfile(@Valid @RequestBody MemberProfileDTO memberProfileDTO) {
        MemberProfile memberProfile = MemberProfileMapper.toEntity(memberProfileDTO);
        MemberProfile savedMemberProfile = memberProfileService.saveNewMemberProfile(memberProfile);
        return new ResponseEntity<>(MemberProfileMapper.toDTO(savedMemberProfile), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemberProfileById(@PathVariable Long id) {
        memberProfileService.deleteMemberProfileById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("@memberService.isMemberOwner(#memberProfileDTO.memberId, authentication)") //can create their own member profile
    @PostMapping("/own")
    public ResponseEntity<MemberProfileDTO> saveOwnMemberProfile(@Valid @RequestBody MemberProfileDTO memberProfileDTO) {
        MemberProfile memberProfile = MemberProfileMapper.toEntity(memberProfileDTO);
        MemberProfile savedMemberProfile = memberProfileService.saveNewMemberProfile(memberProfile);
        return new ResponseEntity<>(MemberProfileMapper.toDTO(savedMemberProfile), HttpStatus.CREATED);
    }

    @PreAuthorize("@memberProfileService.isMemberProfileOwner(#id, authentication)")
    @GetMapping("/own/{id}")
    public ResponseEntity<MemberProfileDTO> getOwnMemberProfile(@PathVariable Long id, Authentication authentication) {     //maybe can use email instead of id
        MemberProfile memberProfile = memberProfileService.getMemberProfileById(id);
        return new ResponseEntity<>(MemberProfileMapper.toDTO(memberProfile), HttpStatus.OK);
    }

    @PreAuthorize("@memberProfileService.isMemberProfileOwner(#memberProfileDTO.memberProfileId, authentication)")
    @PutMapping("/update/own")
    public ResponseEntity<MemberProfileDTO> updateOwnMemberProfile(@RequestBody MemberProfileDTO memberProfileDTO) {
        MemberProfile memberProfile = MemberProfileMapper.toEntity(memberProfileDTO);
        MemberProfile updatedMemberProfile = memberProfileService.updateMemberProfile(memberProfile.getId(), memberProfile);
        return new ResponseEntity<>(MemberProfileMapper.toDTO(updatedMemberProfile), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/member-name")
    public ResponseEntity<Page<MemberProfileDTO>> getMemberProfileByMemberName(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                memberProfileService.getMemberProfileByMemberName(name, page, size, sortDirection, sortField)
                        .map(MemberProfileMapper::toDTO),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/contact")
    public ResponseEntity<Page<MemberProfileDTO>> getMemberProfileByPhoneNumber(
            @RequestParam(defaultValue = "") String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                memberProfileService.getMemberProfileByPhoneNumber(phoneNumber, page, size, sortDirection, sortField)
                        .map(MemberProfileMapper::toDTO),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/address")
    public ResponseEntity<Page<MemberProfileDTO>> getMemberProfileByAddress(
            @RequestParam(defaultValue = "") String address,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                memberProfileService.getMemberProfileByAddress(address, page, size, sortDirection, sortField)
                        .map(MemberProfileMapper::toDTO),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/email")
    public ResponseEntity<MemberProfileDTO> getMemberProfileByMemberEmail(@RequestParam(defaultValue = "") String email) {
        MemberProfile memberProfile =  memberProfileService.getMemberProfileByMemberEmail(email);
        MemberProfileDTO memberProfileDTO = MemberProfileMapper.toDTO(memberProfile);
        return new ResponseEntity<>(memberProfileDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/birth-date")
    public ResponseEntity<Page<MemberProfileDTO>> getMemberProfilesByDateOfBirth(
            @RequestParam(defaultValue = "2025") String dateOfBirth,  // Can be year, year-month, or full date
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField) {

        // Convert the input into startDate and endDate
        Pair<LocalDate, LocalDate> dateRange = getDateRange(dateOfBirth);
        LocalDate startDate = dateRange.getFirst();
        LocalDate endDate = dateRange.getSecond();

        Page<MemberProfileDTO> memberProfiles = memberProfileService
                .findMemberProfileByDateOfBirth(startDate, endDate, page, size, sortDirection, sortField)
                .map(MemberProfileMapper::toDTO);

        return new ResponseEntity<>(memberProfiles, HttpStatus.OK);
    }

    public Pair<LocalDate, LocalDate> getDateRange(String dateString) {
        try {
            // Case 1: Full date provided (e.g., "2006-02-13")
            DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(dateString, fullFormatter);
            return Pair.of(date, date); // Exact date range
        } catch (DateTimeParseException e1) {
            try {
                // Case 2: Year and Month provided (e.g., "2006-02")
                DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
                YearMonth yearMonth = YearMonth.parse(dateString, yearMonthFormatter);
                return Pair.of(yearMonth.atDay(1), yearMonth.atEndOfMonth()); // First to last day of the month
            } catch (DateTimeParseException e2) {
                try {
                    // Case 3: Only Year provided (e.g., "2006")
                    DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
                    int year = Year.parse(dateString, yearFormatter).getValue();
                    return Pair.of(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)); // Whole year range
                } catch (DateTimeParseException e3) {
                    throw new IllegalArgumentException("Invalid date format. Use 'yyyy', 'yyyy-MM', or 'yyyy-MM-dd'.");
                }
            }
        }
    }
}
