package com.system.libraryManagementSystem.service;

import com.system.libraryManagementSystem.exception.BorrowingRecordNotFound;
import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.exception.MemberProfileNotFoundException;
import com.system.libraryManagementSystem.model.BorrowingRecord;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.model.MemberProfile;
import com.system.libraryManagementSystem.repository.BorrowingRecordRepository;
import com.system.libraryManagementSystem.repository.MemberProfileRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

    @Mock
    private MemberProfileRepository memberProfileRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MemberProfileService memberProfileService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private Member member;
    private MemberProfile memberProfile;
    private List<MemberProfile> memberProfiles;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        memberProfile = MemberProfile.builder()
                .id(1L)
                .member(member)
                .address("My example address")
                .build();

        pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.fromString("ASC"), "id"));
        memberProfiles = List.of(memberProfile);
    }

    @Test
    void getAllMemberProfiles_ShouldReturnPagedOfBorrowingRecords() {
        //Arrange
        Page<MemberProfile> expectedPage = new PageImpl<>(memberProfiles, pageRequest, memberProfiles.size());

        when(memberProfileRepository.findAll(pageRequest)).thenReturn(expectedPage);

        // Act
        Page<MemberProfile> result = memberProfileService.getAllMemberProfiles(0, 10, "ASC", "id");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals("id: ASC", result.getPageable().getSort().toString());

        verify(memberProfileRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getAllMemberProfiles_WhenRecordsIsEmpty_ShouldReturnPagedOfBorrowingRecords() {
        List<MemberProfile> emptyPage = new ArrayList<>();
        Page<MemberProfile> expectedPage = new PageImpl<>(emptyPage, pageRequest, emptyPage.size());

        when(memberProfileRepository.findAll(pageRequest)).thenReturn(expectedPage);

        // Act
        Page<MemberProfile> result = memberProfileService.getAllMemberProfiles(0, 10, "ASC", "id");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals("id: ASC", result.getPageable().getSort().toString());

        verify(memberProfileRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getMemberProfileById_WhenIdExist_ShouldReturnBorrowingRecord() {
        when(memberProfileRepository.findById(memberProfile.getId())).thenReturn(Optional.of(memberProfile));

        MemberProfile result = memberProfileService.getMemberProfileById(memberProfile.getId());

        assertNotNull(result);
        assertEquals(memberProfile.getId(), result.getId());
        verify(memberProfileRepository, times(1)).findById(1L);
    }

    @Test
    void getMemberProfileById_WhenIdDoesNotExist_ShouldThrowException() {
        when(memberProfileRepository.findById(99L)).thenReturn(Optional.empty());

        MemberProfileNotFoundException result = assertThrows(MemberProfileNotFoundException.class, () -> memberProfileService.getMemberProfileById(99L));

        assertNotNull(result);
        assertEquals("Member profile not found with the id: 99", result.getMessage());
        verify(memberProfileRepository, times(1)).findById(99L);
    }

    @Test
    void saveNewMemberProfile_WhenMemberHasNoCurrentMemberProfile_ShouldSaveSuccesfully() {
        Member newMember = Member.builder().build();
        MemberProfile newMemberProfile = MemberProfile.builder()
                .id(2L)
                .member(newMember)
                .build();

        when(memberProfileRepository.existsByMemberId(newMember.getId())).thenReturn(false);
        when(memberProfileRepository.save(any(MemberProfile.class))).thenReturn(newMemberProfile);

        // Act
        MemberProfile result = memberProfileService.saveNewMemberProfile(newMemberProfile);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(memberProfileRepository, times(1)).save(newMemberProfile);
    }

    @Test
    void saveNewMemberProfile_WhenMemberAlreadyHasProfile_ShouldThrowException() {
        MemberProfile newMemberProfile = MemberProfile.builder()
                .id(2L)
                .member(member)
                .build();

        when(memberProfileRepository.existsByMemberId(member.getId())).thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> memberProfileService.saveNewMemberProfile(newMemberProfile)
        );

        assertEquals("Member already has a profile.", exception.getMessage());
        verify(memberProfileRepository, times(1)).existsByMemberId(member.getId());
        verify(memberProfileRepository, never()).save(any(MemberProfile.class));
    }

    @Test
    void updateMemberProfile_WhenProfileIsChanged_ShouldUpdateSuccessfully() {
        MemberProfile updatedMemberProfile = MemberProfile.builder()
                .id(memberProfile.getId())
                .member(member)
                .address("Updated address test")
                .build();

        when(memberProfileRepository.findById(memberProfile.getId())).thenReturn(Optional.of(memberProfile));
        when(memberProfileRepository.save(any(MemberProfile.class))).thenReturn(updatedMemberProfile);
        when(cacheManager.getCache("member_profiles")).thenReturn(cache);

        MemberProfile result = memberProfileService.updateMemberProfile(memberProfile.getId(), updatedMemberProfile);

        assertNotNull(result);
        assertEquals("Updated address test", result.getAddress());
        assertEquals("test@example.com", result.getMember().getEmail());
        assertEquals(updatedMemberProfile, result);

        verify(memberProfileRepository, times(1)).findById(memberProfile.getId());
        verify(memberProfileRepository, times(1)).save(updatedMemberProfile);
    }

    @Test
    void updateMemberProfile_WithTheSameData_ShouldAvoidUnnecessaryUpdates() {
        MemberProfile updatedMemberProfile = MemberProfile.builder()
                .id(memberProfile.getId())
                .member(member)
                .address("My example address")
                .build();

        when(memberProfileRepository.findById(memberProfile.getId())).thenReturn(Optional.of(memberProfile));

        MemberProfile result = memberProfileService.updateMemberProfile(memberProfile.getId(), updatedMemberProfile);

        assertNotNull(result);
        assertEquals("My example address", result.getAddress());
        assertEquals("test@example.com", result.getMember().getEmail());
        assertEquals(updatedMemberProfile, result);
        assertEquals(memberProfile, result);

        verify(memberProfileRepository, times(1)).findById(memberProfile.getId());
        verify(memberProfileRepository, never()).save(updatedMemberProfile);
    }

    @Test
    void updateBorrowingRecord_WhenRecordNotFound_ShouldThrowException() {
        MemberProfile updatedMemberProfile = new MemberProfile();

        when(memberProfileRepository.findById(99L)).thenReturn(Optional.empty());

        MemberProfileNotFoundException result = assertThrows(MemberProfileNotFoundException.class, () -> memberProfileService.updateMemberProfile(99L, updatedMemberProfile));

        assertNotNull(result);
        assertEquals("Member profile not found with the id: 99", result.getMessage());
        verify(memberProfileRepository, times(1)).findById(99L);
        verify(memberProfileRepository, never()).save(any(MemberProfile.class));
    }

    @Test
    void deleteMemberProfileById_WhenProfileExist_ShouldDeleteSuccesfully() {
        when(memberProfileRepository.findById(memberProfile.getId())).thenReturn(Optional.of(memberProfile));
        when(memberRepository.save(member)).thenReturn(member);
        when(cacheManager.getCache("member_profiles")).thenReturn(cache);
        doNothing().when(memberProfileRepository).deleteById(memberProfile.getId());

        memberProfileService.deleteMemberProfileById(memberProfile.getId());

        verify(memberProfileRepository, times(1)).deleteById(memberProfile.getId());
        assertNull(member.getMemberProfile());
        verify(memberProfileRepository, times(1)).findById(memberProfile.getId());
        verify(memberRepository, times(1)).save(member);
        verify(cache, times(1)).evict("test@example.com");
    }

    @Test
    void deleteMemberProfileById_ShouldThrowException_WhenProfileDoesNotExist() {
        when(memberProfileRepository.findById(99L)).thenReturn(Optional.empty());

        MemberProfileNotFoundException result = assertThrows(MemberProfileNotFoundException.class, () -> memberProfileService.deleteMemberProfileById(99L));

        assertNotNull(result);
        assertEquals("Member profile not found with the id: 99", result.getMessage());
        verify(memberProfileRepository, times(1)).findById(99L);
        verify(memberProfileRepository, never()).deleteById(99L);
    }

    @Test
    void testIsMemberOwnerOfTheProfile_ReturnsTrue_WhenEmailMatches() {
        when(memberProfileRepository.findById(1L)).thenReturn(Optional.of(memberProfile));
        when(authentication.getName()).thenReturn("test@example.com");

        boolean result = memberProfileService.isMemberProfileOwner(1L, authentication);

        assertTrue(result);
    }

    @Test
    void testIsMemberOwnerOfTheProfile_ReturnsFalse_WhenEmailDoesNotMatch() {
        when(memberProfileRepository.findById(1L)).thenReturn(Optional.of(memberProfile));
        when(authentication.getName()).thenReturn("wrong@example.com");

        boolean result = memberProfileService.isMemberProfileOwner(1L, authentication);

        assertFalse(result);
    }

    @Test
    void testIsMemberOwnerOfTheProfile_ReturnsFalse_WhenMemberProfileNotFound() {
        when(memberProfileRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = memberProfileService.isMemberProfileOwner(99L, authentication);

        assertFalse(result);
    }
}