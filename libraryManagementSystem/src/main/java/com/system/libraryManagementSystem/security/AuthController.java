package com.system.libraryManagementSystem.security;

import com.system.libraryManagementSystem.dto.MemberDTO;
import com.system.libraryManagementSystem.dto.LoginRequestDTO;
import com.system.libraryManagementSystem.mapper.LoginRequestMapper;
import com.system.libraryManagementSystem.mapper.MemberMapper;
import com.system.libraryManagementSystem.model.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @Operation(summary = "Register", security = @SecurityRequirement(name = ""))
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody MemberDTO memberDTO) {
        Member member = MemberMapper.toEntity(memberDTO);

        return new ResponseEntity<>(authService.register(member), HttpStatus.CREATED);
    }

    @Operation(summary = "Login", security = @SecurityRequirement(name = ""))
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        Member member = LoginRequestMapper.toEntity(loginRequestDTO);

        return new ResponseEntity<>(authService.login(member), HttpStatus.OK);
    }

    @Operation(summary = "Logout", security = @SecurityRequirement(name = ""))
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer")) {
            String token = authHeader.substring(7);
            return new ResponseEntity<>(authService.logout(token), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No token provided", HttpStatus.BAD_REQUEST);
        }
    }
}
