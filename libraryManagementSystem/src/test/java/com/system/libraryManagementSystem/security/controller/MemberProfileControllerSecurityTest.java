package com.system.libraryManagementSystem.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.libraryManagementSystem.dto.MemberProfileDTO;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.model.MemberProfile;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.repository.MemberProfileRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import com.system.libraryManagementSystem.security.JwtService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class MemberProfileControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberProfileRepository memberProfileRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtService jwtService;

    private static Member member;
    private static Member librarian;
    private static Member admin;
    private static Member regularMember;
    private static MemberProfile regularMemberProfile;

    @BeforeAll
    static void setUpOnce(
            @Autowired MemberProfileRepository memberProfileRepository,
            @Autowired MemberRepository memberRepository,
            @Autowired JwtService jwtService
    ) {
        memberRepository.deleteAll();
        memberProfileRepository.deleteAll();

        member = Member.builder()
                .name("Member")
                .email("member@gmail.com")
                .password("12345member")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        librarian = Member.builder()
                .name("Librarian")
                .email("librarian@gmail.com")
                .password("12345librarian")
                .roles(Set.of("ROLE_LIBRARIAN"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        admin = Member.builder()
                .name("Admin")
                .email("admin@gmail.com")
                .password("12345admin")
                .roles(Set.of("ROLE_ADMIN"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        regularMember = Member.builder()
                .name("Regular")
                .email("regular@gmail.com")
                .password("12345regular")
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        memberRepository.saveAll(List.of(member, librarian, admin, regularMember));

        regularMemberProfile = MemberProfile.builder()
                .member(regularMember)
                .address("address")
                .dateOfBirth(LocalDate.now().minusDays(2))
                .phoneNumber("0123456789")
                .build();
        memberProfileRepository.save(regularMemberProfile);
    }

    @Test
    void testGetAllMemberProfiles_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/member-profile"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    void testGetAllMemberProfiles_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/member-profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testGetMemberProfileById_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/member-profile/{id}", regularMemberProfile.getId()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    void testGetMemberProfileById_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/member-profile/{id}", regularMemberProfile.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testGetMemberProfileByMemberName_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/member-profile/member-name")
                        .param("name", regularMemberProfile.getMember().getName()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    void testGetMemberProfileByMemberName_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/member-profile/member-name")
                        .param("name", regularMemberProfile.getMember().getName())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testGetMemberProfileByPhoneNumber_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/member-profile/contact")
                        .param("phoneNumber", regularMemberProfile.getPhoneNumber()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    void testGetMemberProfileByPhoneNumber_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/member-profile/contact")
                        .param("phoneNumber",  regularMemberProfile.getPhoneNumber())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testGetMemberProfileByAddress_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/member-profile/address")
                        .param("address", regularMemberProfile.getAddress()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    void testGetMemberProfileByAddress_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/member-profile/address")
                        .param("address",  regularMemberProfile.getAddress())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testGetMemberProfileByMemberEmail_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/member-profile/email")
                        .param("email", regularMemberProfile.getMember().getEmail()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    void testGetMemberProfileByMemberEmail_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/member-profile/email")
                        .param("email", regularMemberProfile.getMember().getEmail())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testGetMemberProfilesByDateOfBirth_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/member-profile/birth-date")
                        .param("dateOfBirth", String.valueOf(regularMemberProfile.getDateOfBirth())))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    void testGetMemberProfilesByDateOfBirth_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/member-profile/birth-date")
                        .param("dateOfBirth", String.valueOf(regularMemberProfile.getDateOfBirth()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testUpdateMemberProfile_WhenUnauthenticated_ShouldReturn403() throws Exception {
        MemberProfileDTO updatedMemberProfile = MemberProfileDTO.builder()
                .memberProfileId(regularMemberProfile.getId())
                .memberId(regularMember.getId())
                .memberName(regularMember.getName())
                .address("updated address")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456788")
                .build();

        mockMvc.perform(put("/member-profile/update")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedMemberProfile)))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 200",
            "admin@gmail.com, 200",
    })
    void testUpdateMemberProfile_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        MemberProfileDTO updatedMemberProfile = MemberProfileDTO.builder()
                .memberProfileId(regularMemberProfile.getId())
                .memberId(regularMember.getId())
                .memberName(regularMember.getName())
                .address("updated address")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456788")
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(put("/member-profile/update")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedMemberProfile))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testSaveNewMemberProfile_WhenUnauthenticated_ShouldReturn403() throws Exception {

        MemberProfileDTO newProfile = MemberProfileDTO.builder()
                .memberId(member.getId())
                .memberName(member.getName())
                .address("address must be 10 to 50 characters")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456789")
                .build();

        mockMvc.perform(post("/member-profile")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newProfile)))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 201",
            "admin@gmail.com, 201",
    })
    void testSaveNewMemberProfile_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {

        MemberProfileDTO newProfile = MemberProfileDTO.builder()
                .memberId(member.getId())
                .memberName(member.getName())
                .address("address must be 10 to 50 characters")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456789")
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(post("/member-profile")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newProfile))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testDeleteMemberProfile_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/member-profile/{id}", regularMemberProfile.getId()))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 403",
            "librarian@gmail.com, 204",
            "admin@gmail.com, 204",
    })
    void testDeleteMemberProfile_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(delete("/member-profile/{id}", regularMemberProfile.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testSaveOwnMemberProfile_WhenUnauthenticated_ShouldReturn403() throws Exception {
        MemberProfileDTO newProfile = MemberProfileDTO.builder()
                .memberId(member.getId())
                .memberName(member.getName())
                .address("address must be 10 to 50 characters")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456789")
                .build();

        mockMvc.perform(post("/member-profile/own")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newProfile)))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "regular@gmail.com, 403",       //trying to save member's member profile even it is not his/her own account
            "member@gmail.com, 201",
            "librarian@gmail.com, 403",
            "admin@gmail.com, 403",
    })
    void testSaveOwnMemberProfile_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        MemberProfileDTO newProfile = MemberProfileDTO.builder()
                .memberId(member.getId())
                .memberName(member.getName())
                .address("address must be 10 to 50 characters")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456789")
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(post("/member-profile/own")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newProfile))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testUpdateOwnMemberProfile_WhenUnauthenticated_ShouldReturn403() throws Exception {
        MemberProfileDTO newProfile = MemberProfileDTO.builder()
                .memberProfileId(regularMemberProfile.getId())
                .memberId(regularMember.getId())
                .memberName(regularMember.getName())
                .address("updated address must be 10 to 50 characters")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456788")
                .build();

        mockMvc.perform(put("/member-profile/update/own")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newProfile)))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "regular@gmail.com, 200",
            "member@gmail.com, 403",        //trying to update regular's member profile even it is not his/her own account
            "librarian@gmail.com, 403",
            "admin@gmail.com, 403",
    })
    void testUpdateOwnMemberProfile_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        MemberProfileDTO newProfile = MemberProfileDTO.builder()
                .memberProfileId(regularMemberProfile.getId())
                .memberId(regularMember.getId())
                .memberName(regularMember.getName())
                .address("updated  address must be 10 to 50 characters")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456788")
                .build();

        String token = jwtService.getToken(email);
        mockMvc.perform(put("/member-profile/update/own")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newProfile))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void testGetOwnMemberProfile_WhenUnauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/member-profile/own/{id}", regularMemberProfile.getId()))
                .andExpect(status().isForbidden());
    }

    @Transactional
    @ParameterizedTest
    @CsvSource({
            "regular@gmail.com, 200",
            "member@gmail.com, 403",        //trying to get regular's member profile even it is not his/her own account
            "librarian@gmail.com, 403",
            "admin@gmail.com, 403",
    })
    void testGetOwnMemberProfile_WhenAuthorizedOrUnauthorized_ShouldReturnExpectedStatus(String email, int expectedStatus) throws Exception {
        String token = jwtService.getToken(email);
        mockMvc.perform(get("/member-profile/own/{id}", regularMemberProfile.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(expectedStatus));
    }

}