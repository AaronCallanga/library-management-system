package com.system.libraryManagementSystem.mapper;

import com.system.libraryManagementSystem.dto.MemberDTO;
import com.system.libraryManagementSystem.dto.format.BookTitleAuthorDTO;
import com.system.libraryManagementSystem.model.Member;

import java.util.List;
import java.util.stream.Collectors;

public class MemberMapper {

    public static MemberDTO toDTO(Member member) {
        MemberDTO memberDTO = new MemberDTO();

        memberDTO.setId(member.getId());
        memberDTO.setName(member.getName());

        List<BookTitleAuthorDTO> borrowedBooks = member.getBorrowedBooks() == null ? List.of() :
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

        return member;
    }
}
