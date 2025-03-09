package com.system.libraryManagementSystem.service;


import com.system.libraryManagementSystem.exception.BookNotFoundException;
import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BookRepository bookRepository;

    public Page<Member> getAllMembers(int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortField)
        );
        return memberRepository.findAll(pageRequest);
    }

    @Cacheable(cacheNames = "members", key = "#id")
    public Member getMemberById(Long id) {
        return fetchMemberById(id);
    }

    public Member saveNewMember(Member member) {
        return memberRepository.save(member);
    }

    @CachePut(cacheNames = "members", key = "#id")
    public Member updateMember(Long id, Member updatedMember) {
        Member member = fetchMemberById(id);

        member.setName(updatedMember.getName());
        return memberRepository.save(member);
    }

    @CacheEvict(cacheNames = "members", key = "#id")
    public void deleteMemberById(Long id) {
        memberRepository.deleteById(id);
    }

    public Page<Member> getMemberByName(String name, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberRepository.findMemberByName(name, pageRequest);
    }

    public Page<Member> getMemberByBorrowedBookTitle(String title, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberRepository.findMemberByBorrowedBookTitle(title, pageRequest);
    }

    public Member returnBook(Long memberId, Long bookId) {
        Member member = fetchMemberById(memberId);
        Book book = fetchBookById(bookId);

        member.getBorrowedBooks().remove(book);
        return memberRepository.save(member);
    }

    public Member borrowBook(Long memberId, Long bookId) {
        Member member = fetchMemberById(memberId);
        Book book = fetchBookById(bookId);

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

}
