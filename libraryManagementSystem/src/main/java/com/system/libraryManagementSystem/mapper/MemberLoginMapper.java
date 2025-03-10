package com.system.libraryManagementSystem.mapper;

import com.system.libraryManagementSystem.dto.MemberDTO;
import com.system.libraryManagementSystem.dto.MemberLoginDTO;
import com.system.libraryManagementSystem.model.Member;

import java.util.HashSet;

public class MemberLoginMapper {

    public static Member toEntity(MemberLoginDTO memberLoginDTO) {
        Member member = new Member();

        member.setEmail(memberLoginDTO.getEmail());
        member.setPassword(memberLoginDTO.getPassword());
        return member;
    }
}
