package com.system.libraryManagementSystem.security;

import com.system.libraryManagementSystem.dto.MemberDTO;
import com.system.libraryManagementSystem.dto.LoginRequestDTO;
import com.system.libraryManagementSystem.mapper.LoginRequestMapper;
import com.system.libraryManagementSystem.mapper.MemberMapper;
import com.system.libraryManagementSystem.model.Member;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody MemberDTO memberDTO) {
        Member member = MemberMapper.toEntity(memberDTO);

        return new ResponseEntity<>(authService.register(member), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        Member member = LoginRequestMapper.toEntity(loginRequestDTO);

        return new ResponseEntity<>(authService.login(member), HttpStatus.OK);
    }
}
