package com.system.libraryManagementSystem.mapper;

import com.system.libraryManagementSystem.dto.MemberProfileDTO;
import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.model.MemberProfile;
import com.system.libraryManagementSystem.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberProfileMapper {

    private static MemberRepository memberRepository;

    @Autowired
    public void setMemberRepository(MemberRepository memberRepository) {
        MemberProfileMapper.memberRepository = memberRepository;
    }

    public static MemberProfileDTO toDTO(MemberProfile memberProfile) {

        MemberProfileDTO memberProfileDTO = new MemberProfileDTO();

        memberProfileDTO.setMemberProfileId(memberProfile.getId());
        memberProfileDTO.setMemberId(memberProfile.getMember().getId());
        memberProfileDTO.setMemberName(memberProfile.getMember().getName());
        memberProfileDTO.setMemberEmail(memberProfile.getMember().getEmail());
        memberProfileDTO.setPhoneNumber(memberProfile.getPhoneNumber());
        memberProfileDTO.setAddress(memberProfile.getAddress());
        memberProfileDTO.setDateOfBirth(memberProfile.getDateOfBirth());

        return memberProfileDTO;
    }

    public static MemberProfile toEntity(MemberProfileDTO memberProfileDTO) {
        Member member = memberRepository.findById(memberProfileDTO.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("Member not found with the id: " + memberProfileDTO.getMemberId())); //you can also use name, just provide findByName, however names must be unique so it is better to use id to reference the member

        if (!member.getName().equalsIgnoreCase(memberProfileDTO.getMemberName())) {
            throw new MemberNotFoundException(memberProfileDTO.getMemberName() + " did not match with the name of the member with the id: "+ memberProfileDTO.getMemberId());
        }   //you can make this as a validation anotation and pass it to the MemberProfileDTO

        MemberProfile memberProfile = new MemberProfile();

        memberProfile.setId(memberProfileDTO.getMemberProfileId());
        memberProfile.setPhoneNumber(memberProfileDTO.getPhoneNumber());
        memberProfile.setAddress(memberProfileDTO.getAddress());
        memberProfile.setDateOfBirth(memberProfileDTO.getDateOfBirth());
        memberProfile.setMember(member);

        return memberProfile;
    }
}
