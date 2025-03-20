package com.system.libraryManagementSystem.cache;

import com.system.libraryManagementSystem.exception.MemberProfileNotFoundException;
import com.system.libraryManagementSystem.model.*;
import com.system.libraryManagementSystem.repository.MemberProfileRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import com.system.libraryManagementSystem.service.MemberProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@EnableCaching
@SpringBootTest
@ActiveProfiles("test")
class MemberProfileServiceCacheTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MemberProfileService memberProfileService;

    @MockitoBean
    MemberRepository memberRepository;
    @MockitoBean
    private MemberProfileRepository memberProfileRepository;

    private Member member;
    private MemberProfile memberProfile;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("member_profiles").clear();

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
                .build();
        memberProfile = MemberProfile.builder()
                .id(1L)
                .member(member)
                .address("address")
                .dateOfBirth(LocalDate.now().minusDays(2))
                .phoneNumber("0123456789")
                .build();

        when(memberProfileRepository.findById(memberProfile.getId())).thenReturn(Optional.of(memberProfile));
        when(memberProfileRepository.findMemberProfileByEmail(memberProfile.getMember().getEmail())).thenReturn(Optional.of(memberProfile));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void getMemberProfileById_ShouldReturnCache_AfterFirstCall() {
        assertNull(cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));

        MemberProfile firstCall = memberProfileService.getMemberProfileById(memberProfile.getId());
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        assertEquals(firstCall, cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        verify(memberProfileRepository, times(1)).findById(memberProfile.getId());

        MemberProfile secondCall = memberProfileService.getMemberProfileById(memberProfile.getId());
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        assertEquals(secondCall, cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        assertEquals(secondCall, firstCall);
        verify(memberProfileRepository, times(1)).findById(memberProfile.getId());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void updateMemberProfile_ShouldUpdateCache_FromEmailAndIdKey() {
        assertNull(cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));

        MemberProfile firstCallById = memberProfileService.getMemberProfileById(memberProfile.getId());
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        assertEquals(firstCallById, cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        verify(memberProfileRepository, times(1)).findById(memberProfile.getId());

        assertNull(cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));

        MemberProfile firstCallByEmail = memberProfileService.getMemberProfileByMemberEmail(memberProfile.getMember().getEmail());
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        assertEquals(firstCallByEmail, cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        verify(memberProfileRepository, times(1)).findMemberProfileByEmail(memberProfile.getMember().getEmail());


        //update profile, must update cache too
        MemberProfile newMemberProfile = MemberProfile.builder()
                .id(memberProfile.getId())
                .member(member)
                .address("updated address")
                .dateOfBirth(LocalDate.now().minusDays(10))
                .phoneNumber("0111111111")
                .build();

        when(memberProfileRepository.save(any(MemberProfile.class))).thenReturn(newMemberProfile);

        MemberProfile updatedProfile = memberProfileService.updateMemberProfile(newMemberProfile.getId(), newMemberProfile);
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        assertEquals(updatedProfile, cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        assertEquals(updatedProfile, cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        verify(memberProfileRepository, times(2)).findById(memberProfile.getId());

        //second call, check if it is getting from the cache and is updated
        when(memberProfileRepository.findById(newMemberProfile.getId())).thenReturn(Optional.of(newMemberProfile));

        MemberProfile secondCallById = memberProfileService.getMemberProfileById(memberProfile.getId());
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        assertEquals(secondCallById, cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        assertEquals(secondCallById, updatedProfile);
        verify(memberProfileRepository, times(2)).findById(memberProfile.getId());

        MemberProfile secondCallByEmail = memberProfileService.getMemberProfileByMemberEmail(memberProfile.getMember().getEmail());
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        assertEquals(secondCallByEmail, cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        assertEquals(secondCallByEmail, updatedProfile);
        verify(memberProfileRepository, times(1)).findMemberProfileByEmail(memberProfile.getMember().getEmail());

    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void deleteMemberProfileById_ShouldEvictCache_FromEmailAndIdKey() {
        //by id key
        assertNull(cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));

        MemberProfile firstCallById = memberProfileService.getMemberProfileById(memberProfile.getId());
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        assertEquals(firstCallById, cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        verify(memberProfileRepository, times(1)).findById(memberProfile.getId());

        //by email key
        assertNull(cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));

        MemberProfile firstCallByEmail = memberProfileService.getMemberProfileByMemberEmail(memberProfile.getMember().getEmail());
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        assertEquals(firstCallByEmail, cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        verify(memberProfileRepository, times(1)).findMemberProfileByEmail(memberProfile.getMember().getEmail());


        //delete data must delete the cache too
        when(memberRepository.save(member)).thenReturn(member);     //based on the service logic
        doNothing().when(memberProfileRepository).deleteById(memberProfile.getId());

        memberProfileService.deleteMemberProfileById(memberProfile.getId());

        assertNull(cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));
        assertNull(cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        verify(memberProfileRepository,times(2)).findById(memberProfile.getId());
        verify(memberProfileRepository,times(1)).deleteById(memberProfile.getId());

        //repository must be activated because cache is deleted
        when(memberProfileRepository.findById(memberProfile.getId())).thenReturn(Optional.empty());
        assertThrows(MemberProfileNotFoundException.class, () -> memberProfileService.getMemberProfileById(memberProfile.getId()));
        verify(memberProfileRepository,times(3)).findById(memberProfile.getId());

        when(memberProfileRepository.findMemberProfileByEmail(memberProfile.getMember().getEmail())).thenReturn(Optional.empty());
        assertThrows(MemberProfileNotFoundException.class, () -> memberProfileService.getMemberProfileByMemberEmail(memberProfile.getMember().getEmail()));
        verify(memberProfileRepository,times(2)).findMemberProfileByEmail(memberProfile.getMember().getEmail());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void getMemberProfileByMemberEmail_ShouldUseCache_AfterFirstCall() {
        assertNull(cacheManager.getCache("member_profiles").get(memberProfile.getId(), MemberProfile.class));

        MemberProfile firstCall = memberProfileService.getMemberProfileByMemberEmail(memberProfile.getMember().getEmail());
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        assertEquals(firstCall, cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        verify(memberProfileRepository, times(1)).findMemberProfileByEmail(memberProfile.getMember().getEmail());

        MemberProfile secondCall = memberProfileService.getMemberProfileByMemberEmail(memberProfile.getMember().getEmail());
        assertNotNull(cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        assertEquals(secondCall, cacheManager.getCache("member_profiles").get(memberProfile.getMember().getEmail(), MemberProfile.class));
        assertEquals(secondCall, firstCall);
        verify(memberProfileRepository, times(1)).findMemberProfileByEmail(memberProfile.getMember().getEmail());
    }

}