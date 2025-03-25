package com.system.libraryManagementSystem.cache;

import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.exception.MemberProfileNotFoundException;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.model.MemberProfile;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import com.system.libraryManagementSystem.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@EnableCaching
@SpringBootTest
@ActiveProfiles("test")
class MemberServiceCacheTest {
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MemberService memberService;

    @MockitoBean
    private MemberRepository memberRepository;
    @MockitoBean
    private BookRepository bookRepository;

    private Member member;
    private Book book;
    private Author author;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("members").clear();

        member = Member.builder()
                .id(1L)
                .name("Member")
                .email("member@gmail.com")
                .password("12345member")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .borrowedBooks(new ArrayList<>())
                .build();
        author = Author.builder()
                .id(1L)
                .name("J.K. Rowling")
                .biography("Famous author")
                .build();
        book = Book.builder()
                .id(1L)
                .title("Harry Potter")
                .genre("Fantasy")
                .publicationYear(1997)
                .author(author)
                .build();

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void getMemberById_ShouldReturnCache_AfterFirstCall() {
        assertNull(cacheManager.getCache("members").get(member.getId(), Member.class));

        Member firstCall = memberService.getMemberById(member.getId());
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(firstCall, cacheManager.getCache("members").get(member.getId(), Member.class));
        verify(memberRepository, times(1)).findById(member.getId());

        Member secondCall = memberService.getMemberById(member.getId());
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(firstCall, cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(secondCall, firstCall);
        verify(memberRepository, times(1)).findById(member.getId());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void updateMember_ShouldUpdateCache() {
        assertNull(cacheManager.getCache("members").get(member.getId(), Member.class));

        Member firstCall = memberService.getMemberById(member.getId());
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(firstCall, cacheManager.getCache("members").get(member.getId(), Member.class));
        verify(memberRepository, times(1)).findById(member.getId());

        Member newMember =  Member.builder()
                .id(member.getId())
                .name("Updated Member")
                .email("updatedmember@gmail.com")
                .password("12345updatedmember")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .borrowedBooks(List.of(book))
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(newMember);

        Member updatedMember = memberService.updateMember(newMember.getId(), newMember);
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(updatedMember, cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(updatedMember, newMember);
        verify(memberRepository, times(2)).findById(member.getId());

        when(memberRepository.findById(newMember.getId())).thenReturn(Optional.of(newMember));

        Member secondCall = memberService.getMemberById(member.getId());
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(secondCall, cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(secondCall, updatedMember);
        verify(memberRepository, times(2)).findById(member.getId());

    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void deleteMemberById_ShouldEvictCache() {
        assertNull(cacheManager.getCache("members").get(member.getId(), Member.class));

        Member firstCall = memberService.getMemberById(member.getId());
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(firstCall, cacheManager.getCache("members").get(member.getId(), Member.class));
        verify(memberRepository, times(1)).findById(member.getId());

        doNothing().when(memberRepository).deleteById(member.getId());

        memberService.deleteMemberById(member.getId());

        assertNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        verify(memberRepository,times(1)).findById(member.getId());
        verify(memberRepository,times(1)).deleteById(member.getId());

        when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());
        assertThrows(MemberNotFoundException.class, () -> memberService.getMemberById(member.getId()));
        verify(memberRepository,times(2)).findById(member.getId());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void returnBook_ShouldUpdateCache() {
        assertNull(cacheManager.getCache("members").get(member.getId(), Member.class));

        Member firstCall = memberService.getMemberById(member.getId());
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(firstCall, cacheManager.getCache("members").get(member.getId(), Member.class));
        verify(memberRepository, times(1)).findById(member.getId());

        when(memberRepository.save(any(Member.class))).thenReturn(member);

        memberService.borrowBook(member.getId(), book.getId());

        Member memberAfterBorrowing = memberService.getMemberById(member.getId());
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(memberAfterBorrowing, cacheManager.getCache("members").get(member.getId(), Member.class));

        memberService.returnBook(member.getId(), book.getId());

        Member memberAfterReturning  = memberService.getMemberById(member.getId());
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(memberAfterReturning, cacheManager.getCache("members").get(member.getId(), Member.class));
        verify(memberRepository, times(3)).findById(member.getId());

        when(memberRepository.findById(memberAfterReturning.getId())).thenReturn(Optional.of(memberAfterReturning));

        Member secondCall = memberService.getMemberById(member.getId());
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(secondCall, cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(secondCall, memberAfterReturning);
        verify(memberRepository, times(3)).findById(member.getId());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void borrowBook_ShouldUpdateCache() {
        assertNull(cacheManager.getCache("members").get(member.getId(), Member.class));

        Member firstCall = memberService.getMemberById(member.getId());
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(firstCall, cacheManager.getCache("members").get(member.getId(), Member.class));
        verify(memberRepository, times(1)).findById(member.getId());


        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member memberBorrower = memberService.borrowBook(member.getId(), book.getId());

        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(memberBorrower, cacheManager.getCache("members").get(member.getId(), Member.class));
        verify(memberRepository, times(2)).findById(member.getId());

        when(memberRepository.findById(memberBorrower.getId())).thenReturn(Optional.of(memberBorrower));

        Member secondCall = memberService.getMemberById(member.getId());
        assertNotNull(cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(secondCall, cacheManager.getCache("members").get(member.getId(), Member.class));
        assertEquals(secondCall, memberBorrower);
        verify(memberRepository, times(2)).findById(member.getId());
    }
}