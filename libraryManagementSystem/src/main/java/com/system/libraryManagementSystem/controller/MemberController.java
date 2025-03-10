package com.system.libraryManagementSystem.controller;

import com.system.libraryManagementSystem.dto.MemberDTO;
import com.system.libraryManagementSystem.mapper.MemberMapper;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;

    }
    @GetMapping
    public ResponseEntity<Page<MemberDTO>> getALlMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                memberService.getAllMembers(page, size, sortDirection, sortField)
                        .map(MemberMapper::toDTO),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDTO> getMemberById(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        return new ResponseEntity<>(MemberMapper.toDTO(member), HttpStatus.OK);
    }

//    @PostMapping
//    public ResponseEntity<MemberDTO> saveNewMember(@Valid @RequestBody MemberDTO memberDTO) {
//        Member member = MemberMapper.toEntity(memberDTO);
//        Member memberSaved = memberService.saveNewMember(member);
//        return new ResponseEntity<>(MemberMapper.toDTO(memberSaved), HttpStatus.CREATED);
//    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MemberDTO> updateMember(@Valid @RequestBody MemberDTO updatedMemberDTO) {
        Member newMember = MemberMapper.toEntity(updatedMemberDTO);
        Member updatedMember = memberService.updateMember(newMember.getId(), newMember);
        return new ResponseEntity<>(MemberMapper.toDTO(updatedMember), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemberById(@PathVariable Long id) {
        memberService.deleteMemberById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{memberId}/borrow/{bookId}")
    public ResponseEntity<MemberDTO> borrowBook(@PathVariable Long memberId, @PathVariable Long bookId) {
        Member member = memberService.borrowBook(memberId, bookId);
        return new ResponseEntity<>(MemberMapper.toDTO(member), HttpStatus.OK);
    }

    @PostMapping("/{memberId}/return/{bookId}")
    public ResponseEntity<MemberDTO> returnBook(@PathVariable Long memberId, @PathVariable Long bookId) {
        Member member = memberService.returnBook(memberId, bookId);
        return new ResponseEntity<>(MemberMapper.toDTO(member), HttpStatus.OK);
    }

    @GetMapping("/name")
    public ResponseEntity<Page<MemberDTO>> getMemberByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                memberService.getMemberByName(name, page, size, sortDirection, sortField)
                        .map(MemberMapper::toDTO),
                HttpStatus.OK
        );
    }

    @GetMapping("/book-title")
    public ResponseEntity<Page<MemberDTO>> getMemberByBorrowedBookTitle(
            @RequestParam String bookTitle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortField
    ) {
        return new ResponseEntity<>(
                memberService.getMemberByBorrowedBookTitle(bookTitle, page, size, sortDirection, sortField)
                        .map(MemberMapper::toDTO),
                HttpStatus.OK
        );
    }
}
