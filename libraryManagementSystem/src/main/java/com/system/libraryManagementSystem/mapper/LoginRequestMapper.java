package com.system.libraryManagementSystem.mapper;

import com.system.libraryManagementSystem.dto.LoginRequestDTO;
import com.system.libraryManagementSystem.model.Member;

public class LoginRequestMapper {

    public static Member toEntity(LoginRequestDTO loginRequestDTO) {
        Member member = new Member();

        member.setEmail(loginRequestDTO.getEmail());
        member.setPassword(loginRequestDTO.getPassword());
        return member;
    }
}
