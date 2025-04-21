package com.system.libraryManagementSystem.service;


import com.system.libraryManagementSystem.exception.BookNotFoundException;
import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


@Service
public class MemberService {
    //maybe you can create more api for like, isEnabled, isCredentialsExpired etc

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    public MemberService(MemberRepository memberRepository, BookRepository bookRepository, BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public Page<Member> getAllMembers(int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortField)
        );
        return memberRepository.findAll(pageRequest);
    }

    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    @Cacheable(cacheNames = "members", key = "#id")
    public Member getMemberById(Long id) {
        return fetchMemberById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Member saveNewMember(Member member) {
        return memberRepository.save(member);
    }

    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    @CachePut(cacheNames = "members", key = "#id")
    public Member updateMember(Long id, Member updatedMember) {
        Member member = fetchMemberById(id);

        if (updatedMember.equals(member)) return updatedMember;

        member.setName(updatedMember.getName());
        member.setEmail(updatedMember.getEmail());
        if (!updatedMember.getPassword().startsWith("$2a$")) {  // bcrypt passwords start with "$2a$"
            member.setPassword(passwordEncoder.encode(updatedMember.getPassword()));
        } else {
            member.setPassword(updatedMember.getPassword());
        }
        member.setRoles(new HashSet<>(updatedMember.getRoles()));
        member.setEnabled(updatedMember.isEnabled());
        member.setAccountNonExpired(updatedMember.isAccountNonExpired());
        member.setCredentialsNonExpired(updatedMember.isCredentialsNonExpired());
        member.setAccountNonLocked(updatedMember.isAccountNonLocked());
        return memberRepository.save(member);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(cacheNames = "members", key = "#id")
    public void deleteMemberById(Long id) {
        memberRepository.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public Page<Member> getMemberByName(String name, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberRepository.findMemberByName(name, pageRequest);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public Page<Member> getMemberByBorrowedBookTitle(String title, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberRepository.findMemberByBorrowedBookTitle(title, pageRequest);
    }

    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    @CachePut(cacheNames = "members", key = "#memberId")
    public Member returnBook(Long memberId, Long bookId) {
        Member member = fetchMemberById(memberId);
        Book book = fetchBookById(bookId);

        if (member.getBorrowedBooks() != null) {
            member.getBorrowedBooks().remove(book);
        } //else throw exception if member still not borrowed any books and the client is already returning

        return memberRepository.save(member);
    }

    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    @CachePut(cacheNames = "members", key = "#memberId")
    public Member borrowBook(Long memberId, Long bookId) {
        Member member = fetchMemberById(memberId);
        Book book = fetchBookById(bookId);

        if (member.getBorrowedBooks() == null) {
            member.setBorrowedBooks(new ArrayList<>()); // ✅ Ensure it is initialized
        }


        member.getBorrowedBooks().add(book);
        return memberRepository.save(member);
    }

    public Member fetchMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with the id: " + id));
    }

    public Book fetchBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with the id: " + id));
    }

    public boolean isMemberOwner(Long id, Authentication authentication) {
        return memberRepository.findById(id)
                .map(member -> member.getEmail().equals(authentication.getName()))
                .orElse(false);
    }

}
