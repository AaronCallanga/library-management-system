package com.system.libraryManagementSystem.security;

import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class MemberDetailsService implements UserDetailsService {
    @Autowired
    MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found with the email: " + email));
//        if (member.getRoles() == null) {
//            member.setRoles(new HashSet<>("ROLE_MEMBER"));
//        }
        return new MemberDetails(member);
    }
}
