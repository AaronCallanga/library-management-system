package com.system.libraryManagementSystem.service;

import com.system.libraryManagementSystem.exception.AuthorNotFoundException;
import com.system.libraryManagementSystem.exception.BookNotFoundException;
import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.exception.MemberProfileNotFoundException;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.model.MemberProfile;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private MemberService memberService;

    private Member member;
    private PageRequest pageRequest;
    private List<Member> members;
    private Book book;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .name("test example")
                .email("test@example.com")
                .password("$2a$10$newhashedpassword")
                .borrowedBooks(new ArrayList<>())
                .build();

        book = Book.builder()
                .id(1L)
                .title("Spring Boot in Action")
                .build();

        pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.fromString("ASC"), "id"));
        members = List.of(member);

    }


    @Test
    void getAllMembers_ShouldReturnPagedOfMembers() {
        //Arrange
        Page<Member> expectedPage = new PageImpl<>(members, pageRequest, members.size());

        when(memberRepository.findAll(pageRequest)).thenReturn(expectedPage);

        // Act
        Page<Member> result = memberService.getAllMembers(0, 10, "ASC", "id");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(member.getEmail(), result.getContent().get(0).getEmail());
        assertEquals("id: ASC", result.getPageable().getSort().toString());

        verify(memberRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getAllMembers_WhenMembersIsEmpty_ShouldReturnEmptyPage() {
        List<Member> emptyPage = new ArrayList<>();
        Page<Member> expectedPage = new PageImpl<>(emptyPage, pageRequest, emptyPage.size());

        when(memberRepository.findAll(pageRequest)).thenReturn(expectedPage);

        // Act
        Page<Member> result = memberService.getAllMembers(0, 10, "ASC", "id");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals("id: ASC", result.getPageable().getSort().toString());

        verify(memberRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getMembersById_WhenIdExist_ShouldReturnBorrowingRecord() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

        Member result = memberService.getMemberById(member.getId());

        assertNotNull(result);
        assertEquals(member.getId(), result.getId());
        verify(memberRepository, times(1)).findById(1L);
    }

    @Test
    void getMembersById_WhenIdDoesNotExist_ShouldThrowException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        MemberNotFoundException result = assertThrows(MemberNotFoundException.class, () -> memberService.getMemberById(99L));

        assertNotNull(result);
        assertEquals("Member not found with the id: 99", result.getMessage());
        verify(memberRepository, times(1)).findById(99L);
    }

    @Test
    void saveNewMember() {
        Member newMember = Member.builder()
                .id(2L)
                .name("New Member")
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(newMember);

        // Act
        Member result = memberService.saveNewMember(newMember);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(memberRepository, times(1)).save(newMember);
    }

    @Test
    void updateMember_WhenProfileIsChanged_ShouldUpdateSuccessfully() {
        Member updatedMember = Member.builder()
                .id(member.getId())
                .name("Updated Member Name")
                .email("test@example.com")
                .password("$2a$10$updatedhashedpassword")
                .borrowedBooks(new ArrayList<>())
                .roles(new HashSet<>())
                .build();

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member result = memberService.updateMember(member.getId(), updatedMember);

        assertNotNull(result);
        assertEquals("Updated Member Name", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("$2a$10$updatedhashedpassword", result.getPassword());
        assertEquals(updatedMember, result);

        verify(memberRepository, times(1)).findById(member.getId());
        verify(memberRepository, times(1)).save(updatedMember);
    }

    @Test
    void updateMember_WithTheSameData_ShouldAvoidUnnecessaryUpdates() {
        Member updatedMember = Member.builder()
                .id(member.getId())
                .name("test example")
                .email("test@example.com")
                .password("$2a$10$newhashedpassword")
                .borrowedBooks(new ArrayList<>())
                .build();

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(updatedMember));

        Member result = memberService.updateMember(member.getId(), updatedMember);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(updatedMember, result);
        assertEquals(member, result);

        verify(memberRepository, times(1)).findById(member.getId());
        verify(memberRepository, never()).save(updatedMember);
    }

    @Test
    void updateMember_WhenPasswordIsNotHashed_ShouldHashIt() {
        Member updatedMember = Member.builder()
                .id(member.getId())
                .name("test example")
                .email("test@example.com")
                .roles(new HashSet<>())
                .password("plaintextPassword")
                .build();

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(passwordEncoder.encode("plaintextPassword")).thenReturn("$2a$10$hashedPassword");

        Member savedMember = Member.builder()
                .id(member.getId())
                .name(updatedMember.getName())
                .email(updatedMember.getEmail())
                .roles(new HashSet<>())
                .password("$2a$10$hashedPassword") // Expecting the hashed password
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        Member result = memberService.updateMember(member.getId(), updatedMember);

        assertNotNull(result);
        assertEquals("$2a$10$hashedPassword", result.getPassword()); // Ensure password was hashed
        verify(passwordEncoder, times(1)).encode("plaintextPassword");
        verify(memberRepository, times(1)).findById(member.getId());
        verify(memberRepository, times(1)).save(any(Member.class));
    }


    @Test
    void deleteMemberById_ShouldCallRepositoryDeleteById() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());
        memberService.deleteMemberById(member.getId());

        assertThrows(MemberNotFoundException.class, () -> memberService.getMemberById(member.getId()));
        verify(memberRepository, times(1)).deleteById(member.getId());
    }

    @Test
    void testReturnBook_WhenMemberDoesNotExist_ShouldThrowException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        MemberNotFoundException result = assertThrows(MemberNotFoundException.class,
                () -> memberService.returnBook(99L, book.getId()));

        assertNotNull(result);
        assertEquals("Member not found with the id: 99", result.getMessage());

        verify(memberRepository, times(1)).findById(99L);
        verify(bookRepository, times(0)).findById(anyLong());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void testReturnBook_WhenBookDoesNotExist_ShouldThrowException() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        BookNotFoundException result = assertThrows(BookNotFoundException.class,
                () -> memberService.returnBook(member.getId(), 99L));

        assertNotNull(result);
        assertEquals("Book not found with the id: 99", result.getMessage());

        verify(memberRepository, times(1)).findById(member.getId());
        verify(bookRepository, times(1)).findById(99L);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void testReturnBook_WhenMemberHasBorrowedBook_ShouldReturnAndSave() {
        member.setBorrowedBooks(new ArrayList<>(List.of(book)));

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member result = memberService.returnBook(member.getId(), book.getId());

        assertNotNull(result);
        assertFalse(result.getBorrowedBooks().contains(book)); // Ensure book was removed

        verify(memberRepository, times(1)).findById(member.getId());
        verify(bookRepository, times(1)).findById(book.getId());
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    void testReturnBook_WhenMemberDoesNotHaveBook_ShouldDoNothing() {
        member.setBorrowedBooks(new ArrayList<>()); // No books borrowed

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member result = memberService.returnBook(member.getId(), book.getId());

        assertNotNull(result);
        assertEquals(0, result.getBorrowedBooks().size()); // Ensure no changes

        verify(memberRepository, times(1)).findById(member.getId());
        verify(bookRepository, times(1)).findById(book.getId());
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    void testBorrowBook_WhenBookAndMemberExist_ShouldBorrowSuccesfully() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member result = memberService.borrowBook(member.getId(), book.getId());

        assertNotNull(result);
        assertEquals(1, result.getBorrowedBooks().size());
        assertEquals(book.getId(), result.getBorrowedBooks().get(0).getId());
        verify(bookRepository, times(1)).findById(book.getId());
        verify(memberRepository, times(1)).findById(member.getId());
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    void testBorrowBook_WhenMemberDoesNotExist_ShouldThrowException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        MemberNotFoundException result = assertThrows(MemberNotFoundException.class, () -> memberService.borrowBook(99L, book.getId()));

        assertNotNull(result);
        assertEquals("Member not found with the id: 99", result.getMessage());
        verify(memberRepository, times(1)).findById(99L);
        verify(bookRepository, never()).findById(book.getId());
        verify(memberRepository, never()).save(member);
    }

    @Test
    void testBorrowBook_WhenBookDoesNotExist_ShouldThrowException() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        BookNotFoundException result = assertThrows(BookNotFoundException.class, () -> memberService.borrowBook(member.getId(), 99L));

        assertNotNull(result);
        assertEquals("Book not found with the id: 99", result.getMessage());
        verify(memberRepository, times(1)).findById(member.getId());
        verify(bookRepository, times(1)).findById(99L);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void testIsMemberOwner_ReturnsTrue_WhenEmailMatches() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(authentication.getName()).thenReturn("test@example.com");

        boolean result = memberService.isMemberOwner(member.getId(), authentication);

        assertTrue(result);
    }

    @Test
    void testIsMemberOwner_ReturnsFalse_WhenEmailDoesNotMatch() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(authentication.getName()).thenReturn("wrong@example.com");

        boolean result = memberService.isMemberOwner(member.getId(), authentication);

        assertFalse(result);
    }

    @Test
    void testIsMemberOwner_ReturnsFalse_WhenMemberNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = memberService.isMemberOwner(99L, authentication);

        assertFalse(result);
    }
}