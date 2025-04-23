package com.system.libraryManagementSystem.security;

import com.system.libraryManagementSystem.model.Book;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    JwtService jwtService;
    @Autowired
    MemberDetailsService memberDetailsService;

    public static final List<String> PUBLIC_URLS = List.of(
            "/auth",
            "/books",
            "/authors",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs.yaml"
    );

    public boolean isPublicEndpoint(String path) {
        return PUBLIC_URLS.stream()
                .anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isPublicEndpoint(request.getServletPath())) {
            filterChain.doFilter(request, response);        //skips authentication
            return;
        }

        String authenticationHeader = request.getHeader("Authorization");
        if (authenticationHeader == null || !authenticationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }
        String jwt = authenticationHeader.substring(7);
        String memberEmail = jwtService.extractEmail(jwt);

        if (jwtService.isTokenBlacklisted(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has been logged out");
            return;
        }

        if (memberEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails member = memberDetailsService.loadUserByUsername(memberEmail);

            if (member != null && jwtService.isTokenValid(jwt)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        member, null, member.getAuthorities()
                );

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
