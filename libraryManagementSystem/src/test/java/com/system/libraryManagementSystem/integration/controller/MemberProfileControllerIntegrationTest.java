package com.system.libraryManagementSystem.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.libraryManagementSystem.dto.MemberDTO;
import com.system.libraryManagementSystem.dto.MemberProfileDTO;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.model.MemberProfile;
import com.system.libraryManagementSystem.repository.MemberProfileRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class MemberProfileControllerIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    private Member member;
    private MemberProfile memberProfile;

    @BeforeEach
    void setUp() {
        memberProfileRepository.deleteAll();
        memberRepository.deleteAll();

        member = Member.builder()
                .name("Member")
                .email("member@gmail.com")
                .password(passwordEncoder.encode("12345member"))
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        memberRepository.save(member);

        memberProfile = MemberProfile.builder()
                .member(member)
                .address("address")
                .dateOfBirth(LocalDate.of(2006,2,13))
                .phoneNumber("0123456789")
                .build();
        memberProfileRepository.save(memberProfile);
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testGetAllMemberProfiles_WhenProfilesExist_ShouldReturnPageOfProfiles() throws Exception {
        mockMvc.perform(get("/member-profile")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].memberName").value(memberProfile.getMember().getName()))
                .andExpect(jsonPath("$.content[0].memberId").value(memberProfile.getMember().getId()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testGetALlMemberProfiles_WhenProfilesDoesNotExist_ShouldReturnEmptyPage() throws Exception {
        memberRepository.deleteAll();

        mockMvc.perform(get("/member-profile")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testGetMemberProfileById_WhenMemberProfileExist_ShouldReturnProfile() throws Exception {
        mockMvc.perform(get("/member-profile/{id}", memberProfile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberProfileId").value(memberProfile.getId()))
                .andExpect(jsonPath("$.memberId").value(memberProfile.getMember().getId()))
                .andExpect(jsonPath("$.memberName").value(memberProfile.getMember().getName()))
                .andExpect(jsonPath("$.phoneNumber").value(memberProfile.getPhoneNumber()));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testGetMemberProfileById_WhenMemberProfileDoesNotExist_ShouldThrowException() throws Exception {
        mockMvc.perform(get("/member-profile/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("MEMBER PROFILE NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Member profile not found with the id: 99"));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testUpdateMemberProfile_ShouldReturnUpdatedMemberProfile() throws Exception {
        MemberProfileDTO updatedMemberProfileDTO = MemberProfileDTO.builder()
                .memberProfileId(memberProfile.getId())
                .memberId(member.getId())
                .memberName(member.getName())
                .address("updated address")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456788")
                .build();

        mockMvc.perform(put("/member-profile/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMemberProfileDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberProfileId").value(updatedMemberProfileDTO.getMemberProfileId()))
                .andExpect(jsonPath("$.memberId").value(updatedMemberProfileDTO.getMemberId()))
                .andExpect(jsonPath("$.memberName").value(updatedMemberProfileDTO.getMemberName()))
                .andExpect(jsonPath("$.phoneNumber").value(updatedMemberProfileDTO.getPhoneNumber()));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void testSaveNewMemberProfile_ShouldReturnCreatedMemberProfile() throws Exception {
        member.setMemberProfile(null); // This will trigger orphan removal
        memberRepository.save(member); // Save the member to apply changes
        memberProfileRepository.deleteAll();


        MemberProfileDTO newProfile = MemberProfileDTO.builder()
                .memberId(member.getId())
                .memberName(member.getName())
                .address("address must be 10 to 50 characters")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456789")
                .build();

        mockMvc.perform(post("/member-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProfile)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberName").value(newProfile.getMemberName()))
                .andExpect(jsonPath("$.memberId").value(newProfile.getMemberId()))
                .andExpect(jsonPath("$.phoneNumber").value(newProfile.getPhoneNumber()));
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    void deleteMemberProfileById_ShouldReturnStatusNoContent() throws Exception {
        mockMvc.perform(delete("/member-profile/{id}", memberProfile.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/member-profile/{id}", memberProfile.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("MEMBER PROFILE NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Member profile not found with the id: " + member.getId()));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void testSaveOwnMemberProfile_ShouldReturnCreatedMemberProfile() throws Exception {
        member.setMemberProfile(null); // This will trigger orphan removal
        memberRepository.save(member); // Save the member to apply changes
        memberProfileRepository.deleteAll();

        MemberProfileDTO newProfile = MemberProfileDTO.builder()
                .memberId(member.getId())
                .memberName(member.getName())
                .address("address must be 10 to 50 characters")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456789")
                .build();

        mockMvc.perform(post("/member-profile/own")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProfile)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberName").value(newProfile.getMemberName()))
                .andExpect(jsonPath("$.memberId").value(newProfile.getMemberId()))
                .andExpect(jsonPath("$.phoneNumber").value(newProfile.getPhoneNumber()));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void testGetOwnMemberProfileById_WhenMemberProfileExist_ShouldReturnProfile() throws Exception {
        mockMvc.perform(get("/member-profile/own/{id}", memberProfile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberProfileId").value(memberProfile.getId()))
                .andExpect(jsonPath("$.memberId").value(memberProfile.getMember().getId()))
                .andExpect(jsonPath("$.memberName").value(memberProfile.getMember().getName()))
                .andExpect(jsonPath("$.phoneNumber").value(memberProfile.getPhoneNumber()));
    }

    @WithMockUser(username = "member@gmail.com", roles = "MEMBER")
    @Test
    void testUpdateOwnMemberProfile_ShouldReturnUpdatedMemberProfile() throws Exception {
        MemberProfileDTO updatedMemberProfileDTO = MemberProfileDTO.builder()
                .memberProfileId(memberProfile.getId())
                .memberId(member.getId())
                .memberName(member.getName())
                .address("updated address")
                .dateOfBirth(LocalDate.now().minusDays(3))
                .phoneNumber("0123456788")
                .build();

        mockMvc.perform(put("/member-profile/update/own")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedMemberProfileDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberProfileId").value(updatedMemberProfileDTO.getMemberProfileId()))
                .andExpect(jsonPath("$.memberId").value(updatedMemberProfileDTO.getMemberId()))
                .andExpect(jsonPath("$.memberName").value(updatedMemberProfileDTO.getMemberName()))
                .andExpect(jsonPath("$.phoneNumber").value(updatedMemberProfileDTO.getPhoneNumber()));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "member",
            "MEMBER",
            "Member",
            "MeMbEr"
    })
    void testGetMemberProfileByMemberName_WhenMemberNameIsFullyProvidedIgnoringCase_ShouldReturnPageOfProfiles(String name) throws Exception {
        mockMvc.perform(get("/member-profile/member-name")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[*].memberName", everyItem(equalToIgnoringCase(name))))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "mem",
            "MEM",
            "bEr",
            "emB"
    })
    void testGetMemberProfileByMemberName_WhenMemberNameIsPartiallyProvided_ShouldReturnPageOfProfiles(String name) throws Exception {
        mockMvc.perform(get("/member-profile/member-name")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].memberName", everyItem(containsStringIgnoringCase(name))))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"nonmatchingname"})
    void testGetMemberProfileByMemberName_WhenMemberNameDoesNotMatch_ShouldReturnEmptyPage(String name) throws Exception {
        mockMvc.perform(get("/member-profile/member-name")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
        "0123456789"
    })
    void testGetMemberProfileByPhoneNumber_WhenPhoneNumberIsFullyProvided_ShouldReturnPageOfProfiles(String phoneNumber) throws Exception {
        mockMvc.perform(get("/member-profile/contact")
                        .param("phoneNumber", phoneNumber)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[*].phoneNumber").value(phoneNumber))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "0123",
            "456",
            "789"
    })
    void testGetMemberProfileByPhoneNumber_WhenPhoneNumberIsPartiallyProvided_ShouldReturnPageOfProfiles(String phoneNumber) throws Exception {
        mockMvc.perform(get("/member-profile/contact")
                        .param("phoneNumber", phoneNumber)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].phoneNumber").value(containsString(phoneNumber)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"0000000000"})
    void testGetMemberProfileByPhoneNumber_WhenPhoneNumberDoesNotMatch_ShouldReturnEmptyPage(String phoneNumber) throws Exception {
        mockMvc.perform(get("/member-profile/contact")
                        .param("phoneNumber", phoneNumber)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "address",
            "ADDRESS",
            "Address",
            "AdDrEsS"
    })
    void testGetMemberProfileByAddress_WhenAddressIsFullyProvidedIgnoringCase_ShouldReturnPageOfProfiles(String address) throws Exception {
        mockMvc.perform(get("/member-profile/address")
                        .param("address", address)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[*].address", everyItem(equalToIgnoringCase(address))))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "add",
            "ADD",
            "dRe",
            "eSs"
    })
    void testGetMemberProfileByAddress_WhenAddressIsPartiallyProvided_ShouldReturnPageOfProfiles(String address) throws Exception {
        mockMvc.perform(get("/member-profile/address")
                        .param("address", address)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].address", everyItem(containsStringIgnoringCase(address))))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"non-matching address"})
    void testGetMemberProfileByAddress_WhenAddressDoesNotMatch_ShouldReturnEmptyPage(String address) throws Exception {
        mockMvc.perform(get("/member-profile/address")
                        .param("address", address)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @CsvSource({
            "member@gmail.com, 200",
            "Member@gmail.com, 200",
            "MEMBER@gmail.com, 200",
            "mEmBeR@gmail.com, 200"
    })
    void testGetMemberProfileByMemberEmail_WhenEmailIsFullyProvidedIgnoringCase_ShouldReturnProfile(String email, int statusCode) throws Exception {
        mockMvc.perform(get("/member-profile/email")
                        .param("email", email))
                .andExpect(status().is(statusCode))
                .andExpect(jsonPath("$.memberEmail", equalToIgnoringCase(email)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "member",
            "ber@gmai.com",
            "@gmail.com",
    })
    void testGetMemberProfileByMemberEmail_WhenEmailIsPartiallyProvided_ShouldReturnEmptyPage(String email) throws Exception {
        mockMvc.perform(get("/member-profile/email")
                        .param("email", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("MEMBER PROFILE NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Member profile not found with the email: " + email));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"nonmatchingemail@gmail.com"})
    void testGetMemberProfileByMemberEmail_WhenEmailDoesNotMatch_ShouldReturnEmptyPage(String email) throws Exception {
        mockMvc.perform(get("/member-profile/email")
                        .param("email", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorResponse").value("MEMBER PROFILE NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Member profile not found with the email: " + email));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
        "2006"
    })
    void testGetMemberProfilesByDateOfBirth_WhenDateIsOnlyYear_ShouldReturnPageOfProfile(String dateOfBirth) throws Exception {
        mockMvc.perform(get("/member-profile/birth-date")
                        .param("dateOfBirth", dateOfBirth)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].dateOfBirth", everyItem(startsWith(dateOfBirth))))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "2006-02"
    })
    void testGetMemberProfilesByDateOfBirth_WhenDateIsOnlyMonthAndYear_ShouldReturnPageOfProfile(String dateOfBirth) throws Exception {
        mockMvc.perform(get("/member-profile/birth-date")
                        .param("dateOfBirth", dateOfBirth)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].dateOfBirth", everyItem(startsWith(dateOfBirth))))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "2006-02-13"
    })
    void testGetMemberProfilesByDateOfBirth_WhenDateIsFullProvided_ShouldReturnPageOfProfile(String dateOfBirth) throws Exception {
        mockMvc.perform(get("/member-profile/birth-date")
                        .param("dateOfBirth", dateOfBirth)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].dateOfBirth").value(dateOfBirth))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {
            "0001-01-01"
    })
    void testGetMemberProfilesByDateOfBirth_WhenDateIsDoesNotMatch_ShouldReturnEmptyPage(String dateOfBirth) throws Exception {
        mockMvc.perform(get("/member-profile/birth-date")
                        .param("dateOfBirth", dateOfBirth)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "ASC")
                        .param("sortField", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

}