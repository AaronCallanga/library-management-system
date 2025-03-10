package com.system.libraryManagementSystem.security;

import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    @Autowired
    public MemberRepository memberRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;

    public String register(Member member) {
        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            return "Email already in use";  //maybe create a Response format
        }
        if (member.getRoles() == null || member.getRoles().isEmpty()) {
            return "User must have at least one role";
        }
        String encodedPassword = passwordEncoder.encode(member.getPassword());

        member.setPassword(encodedPassword);
        member.setEnabled(true);
        member.setAccountNonExpired(true);
        member.setAccountNonLocked(true);
        member.setCredentialsNonExpired(true);


        memberRepository.save(member);

        return "Member registered succesfully";
    }

    public String login(Member member) {
//        System.out.println("Authentication successful for user: " + authentication.getName());
        Member dbMember = memberRepository.findByEmail(member.getEmail())
                .orElseThrow(() -> new MemberNotFoundException("Member not found with the email: " + member.getEmail()));

        if (!dbMember.isEnabled()) {
            throw new RuntimeException("Account not verified. Please verify your account.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(member.getEmail(), member.getPassword())
        );

        if (authentication.isAuthenticated()) {
            return "Login succesfully"; //jwt
        } else {
            throw new UsernameNotFoundException("Invalid Credentials");
        }

    }
}
