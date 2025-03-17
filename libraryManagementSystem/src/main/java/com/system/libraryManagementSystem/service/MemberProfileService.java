package com.system.libraryManagementSystem.service;

import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.exception.MemberProfileNotFoundException;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.model.MemberProfile;
import com.system.libraryManagementSystem.repository.MemberProfileRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MemberProfileService {

    @Autowired
    private MemberProfileRepository memberProfileRepository;
    @Autowired
    CacheManager cacheManager;
    @Autowired
    MemberRepository memberRepository;


    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public Page<MemberProfile> getAllMemberProfiles(int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortField)
        );
        return memberProfileRepository.findAll(pageRequest);
    }

    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    @Cacheable(cacheNames = "member_profiles", key = "#id")
    public MemberProfile getMemberProfileById(Long id) {
        return memberProfileRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member profile not found with the id: " + id));
    }

    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    public MemberProfile saveNewMemberProfile(MemberProfile memberProfile) {
        return memberProfileRepository.save(memberProfile);
    }

    @PreAuthorize("hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN')")
    @CachePut(cacheNames = "member_profiles", key = "#id")
    public MemberProfile updateMemberProfile(Long id, MemberProfile updatedMemberProfile) {
        MemberProfile memberProfile = memberProfileRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member profile not found with the id: " + id));

        memberProfile.setAddress(updatedMemberProfile.getAddress());
        memberProfile.setPhoneNumber(updatedMemberProfile.getPhoneNumber());
        memberProfile.setDateOfBirth(updatedMemberProfile.getDateOfBirth());
        memberProfile.setMember(updatedMemberProfile.getMember());

        // Save first!
        MemberProfile savedProfile = memberProfileRepository.save(memberProfile);

        // Then manually update cache using email
        String email = updatedMemberProfile.getMember().getEmail();
        if (email != null) {
            cacheManager.getCache("member_profiles").put(email, savedProfile);
        }

        return savedProfile;
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @CacheEvict(cacheNames = "member_profiles", key = "#id")
    public void deleteMemberProfileById(Long id) {

        MemberProfile memberProfile = memberProfileRepository.findById(id)
                        .orElseThrow(() -> new MemberProfileNotFoundException("Member profile not found with the id: " + id));

        Member member = memberProfile.getMember();
        String email = member.getEmail();

        if (member != null) {
            member.setMemberProfile(null); // This will trigger orphan removal
            memberRepository.save(member); // Save the member to apply changes
        }
        if (email != null) {
            cacheManager.getCache("member_profiles").evict(email);
        }
        memberProfileRepository.deleteById(id);

    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public Page<MemberProfile> getMemberProfileByMemberName(String name, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberProfileRepository.findMemberProfileByMemberName(name, pageRequest);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public Page<MemberProfile> getMemberProfileByPhoneNumber(String phoneNumber, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberProfileRepository.findMemberProfileByPhoneNumber(phoneNumber, pageRequest);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public Page<MemberProfile> getMemberProfileByAddress(String address, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberProfileRepository.findMemberProfileByAddress(address, pageRequest);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Cacheable(cacheNames = "member_profiles", key = "#email")  //insert new cache value but with email as the key
    public MemberProfile getMemberProfileByMemberEmail(String email) {
        return memberProfileRepository.findMemberProfileByEmail(email)
                .orElseThrow(() -> new MemberProfileNotFoundException("Member profile not found with the email: " + email));
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public Page<MemberProfile> findMemberProfileByDateOfBirth(LocalDate startDate, LocalDate endDate, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberProfileRepository.findMemberProfileByDateOfBirth(startDate, endDate, pageRequest);
    }

    public boolean isMemberProfileOwner(Long id, Authentication authentication) {
        return memberProfileRepository.findById(id)
                .map(mp -> mp.getMember().getEmail().equals(authentication.getName()))
                .orElse(false);
    }

}
