package com.system.libraryManagementSystem.controller;

import com.system.libraryManagementSystem.dto.MemberDTO;
import com.system.libraryManagementSystem.mapper.MemberMapper;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;

    }
    @Operation(summary = "Get All Members")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
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

    @Operation(summary = "Get Member By ID")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<MemberDTO> getMemberById(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        return new ResponseEntity<>(MemberMapper.toDTO(member), HttpStatus.OK);
    }

    @Operation(summary = "Create Member")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MemberDTO> saveNewMember(@Valid @RequestBody MemberDTO memberDTO) {
        Member member = MemberMapper.toEntity(memberDTO);
        Member memberSaved = memberService.saveNewMember(member);
        return new ResponseEntity<>(MemberMapper.toDTO(memberSaved), HttpStatus.CREATED);
    }


    @Operation(summary = "Update Member")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<MemberDTO> updateMember(@Valid @RequestBody MemberDTO updatedMemberDTO) {
        Member newMember = MemberMapper.toEntity(updatedMemberDTO);
        Member updatedMember = memberService.updateMember(newMember.getId(), newMember);
        return new ResponseEntity<>(MemberMapper.toDTO(updatedMember), HttpStatus.OK);
    }

    @Operation(summary = "Delete Member")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemberById(@PathVariable Long id) {
        memberService.deleteMemberById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get Own Member Details")
    @PreAuthorize("@memberService.isMemberOwner(#id, authentication)")
    @GetMapping("/own/{id}")
    public ResponseEntity<MemberDTO> getOwnMemberDetails(@PathVariable Long id) {  //automatically inject Authentication object, can use the email instead of id
        Member member = memberService.getMemberById(id);
        return new ResponseEntity<>(MemberMapper.toDTO(member), HttpStatus.OK);
    }

//    @PreAuthorize("#memberDTO.email == authentication.name") //pwede rin
    @Operation(summary = "Update Own Member Details")
    @PreAuthorize("@memberService.isMemberOwner(#memberDTO.id, authentication)")
    @PutMapping("/update/own")
    public ResponseEntity<MemberDTO> updateOwnMemberDetails(@RequestBody MemberDTO memberDTO) {
        Member member = MemberMapper.toEntity(memberDTO);
        Member updatedMember = memberService.updateMember(member.getId(), member);

        return new ResponseEntity<>(MemberMapper.toDTO(updatedMember), HttpStatus.OK);
    }

    // A member can only borrow or return books using their own account.
// The borrowing process follows these steps:
// 1. A borrowing request is first posted.
// 2. After approval, the borrowing action can be executed.
//
// The authorization check ensures that only the owner of the account (memberId)
// can perform borrowing and returning actions.
    @Operation(summary = "Borrow Book")
    @PreAuthorize("@memberService.isMemberOwner(#memberId, authentication)")
    @PostMapping("/{memberId}/borrow/{bookId}")
    public ResponseEntity<MemberDTO> borrowBook(@PathVariable Long memberId, @PathVariable Long bookId) {
        Member member = memberService.borrowBook(memberId, bookId);
        return new ResponseEntity<>(MemberMapper.toDTO(member), HttpStatus.OK);
    }

    @Operation(summary = "Return Book")
    @PreAuthorize("@memberService.isMemberOwner(#memberId, authentication)")
    @PostMapping("/{memberId}/return/{bookId}")
    public ResponseEntity<MemberDTO> returnBook(@PathVariable Long memberId, @PathVariable Long bookId) {
        Member member = memberService.returnBook(memberId, bookId);
        return new ResponseEntity<>(MemberMapper.toDTO(member), HttpStatus.OK);
    }

    @Operation(summary = "Borrow Book - ADMIN/LIBRARIAN")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")       //for admin/librarian
    @PostMapping("/authorized/{memberId}/borrow/{bookId}")
    public ResponseEntity<MemberDTO> borrowBookWithAuthority(@PathVariable Long memberId, @PathVariable Long bookId) {
        Member member = memberService.borrowBook(memberId, bookId);
        return new ResponseEntity<>(MemberMapper.toDTO(member), HttpStatus.OK);
    }

    @Operation(summary = "Return Book - ADMIN/LIBRARIAN")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @PostMapping("/authorized/{memberId}/return/{bookId}")
    public ResponseEntity<MemberDTO> returnBookWithAuthority(@PathVariable Long memberId, @PathVariable Long bookId) {
        Member member = memberService.returnBook(memberId, bookId);
        return new ResponseEntity<>(MemberMapper.toDTO(member), HttpStatus.OK);
    }

    @Operation(summary = "Get Members By Name")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/name")
    public ResponseEntity<Page<MemberDTO>> getMemberByName(
            @RequestParam(defaultValue = "") String name,
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

    @Operation(summary = "Get Members By Borrowed Book's Title")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @GetMapping("/book-title")
    public ResponseEntity<Page<MemberDTO>> getMemberByBorrowedBookTitle(
            @RequestParam(defaultValue = "") String bookTitle,
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
