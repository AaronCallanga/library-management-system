package com.system.libraryManagementSystem.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    //create api for user profile information, so a member can see his own borrowed books, profile, transactions etc, most likele you return response with jwt and its email/id/name that can be use to fetch in database and use the MemberService to fetch the data
    //Add exception handling for unauthorized access (e.g., custom AuthenticationEntryPoint).
    //validate email
    // print out borrowing record transactions, word
    // add images for member profile
    //add bool field in the borrowed request, only admin and librarian can accept or reject the request. after accepting it the data will be persisted in the borrowing-record
    //borrowing-record field       ,        boolean isApproved;
    @Autowired
    MemberDetailsService memberDetailsService;
    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth                 //request matchers must be ordered as specific to general, to evaluate the specific one first
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books/**", "/authors/**").permitAll()                //guest

                        //Members, Librarians, Admins
                        .requestMatchers(HttpMethod.GET, "/members/**", "/member-profile/**", "/borrowing-record/**").hasAnyRole("MEMBER", "LIBRARIAN", "ADMIN")       //only view their own
                        .requestMatchers(HttpMethod.PUT, "/members/**", "/member-profile/**").hasAnyRole("MEMBER", "LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/member-profile/**", "/borrowing-record/**").hasAnyRole("MEMBER", "LIBRARIAN", "ADMIN")  //can request for books, which will be confirmed by admin or librarian only     //can post member-profile only for themselves

                        //Librarians, Admins
                        .requestMatchers(HttpMethod.POST, "/books/**", "/authors/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/books/**", "/authors/**", "/borrowing-record/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/books/**", "/authors/**").hasAnyRole("LIBRARIAN", "ADMIN")

                        //Admins only
                        .requestMatchers(HttpMethod.POST, "/members/**", "/member-profile").hasRole("ADMIN")
                        .requestMatchers("/members/**", "/member-profile", "/borrowing-record/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(memberDetailsService);

        return daoAuthenticationProvider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}
