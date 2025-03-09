package com.system.libraryManagementSystem.service;

import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.exception.MemberProfileNotFoundException;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.model.MemberProfile;
import com.system.libraryManagementSystem.repository.MemberProfileRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MemberProfileService {

    @Autowired
    MemberProfileRepository memberProfileRepository;

    @Autowired
    MemberRepository memberRepository;

    public Page<MemberProfile> getAllMemberProfiles(int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortField)
        );
        return memberProfileRepository.findAll(pageRequest);
    }

    @Cacheable(cacheNames = "member profiles", key = "#id")
    public MemberProfile getMemberProfileById(Long id) {
        return memberProfileRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member profile not found with the id: " + id));
    }

    public MemberProfile saveNewMemberProfile(MemberProfile memberProfile) {
        if (memberProfileRepository.findMemberProfileByEmail(memberProfile.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already taken");
        }
        return memberProfileRepository.save(memberProfile);
    }

    @CachePut(cacheNames = "member profiles", key = "#id")
    public MemberProfile updateMemberProfile(Long id, MemberProfile updatedMemberProfile) {
        MemberProfile memberProfile = memberProfileRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member profile not found with the id: " + id));

        memberProfile.setEmail(updatedMemberProfile.getEmail());
        memberProfile.setAddress(updatedMemberProfile.getAddress());
        memberProfile.setPhoneNumber(updatedMemberProfile.getPhoneNumber());
        memberProfile.setDateOfBirth(updatedMemberProfile.getDateOfBirth());
        memberProfile.setMember(updatedMemberProfile.getMember());

        return memberProfileRepository.save(memberProfile);
    }

    @CacheEvict(cacheNames = "member profiles", key = "#id")
    public void deleteMemberProfileById(Long id) {

        MemberProfile memberProfile = memberProfileRepository.findById(id)
                        .orElseThrow(() -> new MemberProfileNotFoundException("Member profile not found with the id: " + id));

        Member member = memberProfile.getMember();

        if (member != null) {
            member.setMemberProfile(null); // This will trigger orphan removal
            memberRepository.save(member); // Save the member to apply changes
        }

        memberProfileRepository.deleteById(id);

    }

    public Page<MemberProfile> getMemberProfileByMemberName(String name, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberProfileRepository.findMemberProfileByMemberName(name, pageRequest);
    }

    public Page<MemberProfile> getMemberProfileByPhoneNumber(String phoneNumber, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberProfileRepository.findMemberProfileByPhoneNumber(phoneNumber, pageRequest);
    }

    public Page<MemberProfile> getMemberProfileByAddress(String address, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberProfileRepository.findMemberProfileByAddress(address, pageRequest);
    }

    public MemberProfile getMemberProfileByEmail(String email) {
        return memberProfileRepository.findMemberProfileByEmail(email)
                .orElseThrow(() -> new MemberProfileNotFoundException("Member profile not found with the email: " + email));
    }

    public Page<MemberProfile> findMemberProfileByDateOfBirth(LocalDate startDate, LocalDate endDate, int page, int size, String sortDirection, String sortField) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        return memberProfileRepository.findMemberProfileByDateOfBirth(startDate, endDate, pageRequest);
    }
}
