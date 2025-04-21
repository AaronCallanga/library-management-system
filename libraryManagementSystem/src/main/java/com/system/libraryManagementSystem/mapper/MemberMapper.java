package com.system.libraryManagementSystem.mapper;

import com.system.libraryManagementSystem.dto.MemberDTO;
import com.system.libraryManagementSystem.dto.format.BookTitleAuthorDTO;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.BookRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MemberMapper {

    public static MemberDTO toDTO(Member member) {
        MemberDTO memberDTO = new MemberDTO();

        memberDTO.setId(member.getId());
        memberDTO.setName(member.getName());
        memberDTO.setEmail(member.getEmail());
        memberDTO.setPassword("HIDDEN");
        memberDTO.setEnabled(member.isEnabled());
        memberDTO.setAccountNonExpired(member.isAccountNonExpired());
        memberDTO.setAccountNonLocked(member.isAccountNonLocked());
        memberDTO.setCredentialsNonExpired(member.isCredentialsNonExpired());
        memberDTO.setRoles(new HashSet<>(member.getRoles()));

        List<BookTitleAuthorDTO> borrowedBooks = member.getBorrowedBooks() == null ? new ArrayList<>() :
                member.getBorrowedBooks()
                        .stream()
                        .map(book -> new BookTitleAuthorDTO(book.getTitle(), book.getAuthor().getName()))
                        .collect(Collectors.toList());

        memberDTO.setBorrowedBooks(borrowedBooks);

        return memberDTO;
    }

    public static Member toEntity(MemberDTO memberDTO) {
        Member member = new Member();

        member.setId(memberDTO.getId());
        member.setName(memberDTO.getName());
        member.setEmail(memberDTO.getEmail());
        member.setPassword(memberDTO.getPassword());
        member.setEnabled(memberDTO.getEnabled());
        member.setAccountNonLocked(memberDTO.getAccountNonLocked());
        member.setAccountNonExpired(memberDTO.getAccountNonExpired());
        member.setCredentialsNonExpired(memberDTO.getCredentialsNonExpired());
        member.setRoles(new HashSet<>(memberDTO.getRoles()));

//        List<Book> borrowedBooks = memberDTO.getBorrowedBooks() == null ? new ArrayList<>() :
//                memberDTO.getBorrowedBooks()
//                        .stream()
//                        .map(bookDTO -> bookRepository.findByTitle(bookDTO.getBookTitle())
//                                .orElseThrow(() -> new RuntimeException("Book with title '" + bookDTO.getBookTitle() + "' not found")))
//                        .collect(Collectors.toList());

        member.setBorrowedBooks(new ArrayList<>());

        return member;
    }
}
