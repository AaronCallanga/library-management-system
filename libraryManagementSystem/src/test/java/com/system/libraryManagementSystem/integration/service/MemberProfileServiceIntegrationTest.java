package com.system.libraryManagementSystem.integration.service;

import com.system.libraryManagementSystem.exception.MemberProfileNotFoundException;
import com.system.libraryManagementSystem.model.BorrowingRecord;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.model.MemberProfile;
import com.system.libraryManagementSystem.repository.MemberProfileRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import com.system.libraryManagementSystem.service.MemberProfileService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class MemberProfileServiceIntegrationTest {

    @Autowired
    private MemberProfileService memberProfileService;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member member;
    private MemberProfile memberProfile;
    private Member member2;
    private MemberProfile memberProfile2;

    @BeforeEach
    void setUp() {
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
        member2 = Member.builder()
                .name("test")
                .email("test@gmail.com")
                .password(passwordEncoder.encode("12345test"))
                .roles(Set.of("ROLE_MEMBER"))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        memberRepository.saveAll(List.of(member, member2));

        memberProfile = MemberProfile.builder()
                .member(member)
                .phoneNumber("0123456789")
                .dateOfBirth(LocalDate.of(2006, 2, 13))
                .address("My example address")
                .build();
        memberProfile2 = MemberProfile.builder()
                .member(member2)
                .phoneNumber("9876543210")
                .dateOfBirth(LocalDate.of(2006, 2, 15))
                .address("test another location")
                .build();
        memberProfileRepository.saveAll(List.of(memberProfile, memberProfile2));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetAllMemberProfiles() {
        Page<MemberProfile> result = memberProfileService.getAllMemberProfiles(0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"Member", "mEmBeR", "MEMBER", "member"})
    void testGetMemberProfileByMemberName_WhenMemberNameFullyMatchIgnoringCase_ShouldReturnPageOfRecords(String name) {
        Page<MemberProfile> result = memberProfileService.getMemberProfileByMemberName(name, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(mp -> mp.getMember().getName().equalsIgnoreCase(name)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"Mem", "emB", "er"})
    void testGetMemberProfileByMemberName_WhenMemberNamePartiallyMatchIgnoringCase_ShouldReturnPageOfRecords(String name) {
        Page<MemberProfile> result = memberProfileService.getMemberProfileByMemberName(name, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(mp -> mp.getMember().getName().toLowerCase().contains(name.toLowerCase())));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"non-matching name"})
    void testGetMemberProfileByMemberName_WhenMemberNameDoesNotMatch_ShouldReturnEmptyPage(String name) {
        Page<MemberProfile> result = memberProfileService.getMemberProfileByMemberName(name, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"0123456789"})
    void testGetMemberProfileByPhoneNumber_WhenPhoneNumberFullyMatch_ShouldReturnPageOfRecords(String contact) {
        Page<MemberProfile> result = memberProfileService.getMemberProfileByPhoneNumber(contact, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(mp -> mp.getPhoneNumber().equals(contact)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"0123", "456", "789"})
    void testGetMemberProfileByPhoneNumber_WhenPhoneNumberPartiallyMatch_ShouldReturnPageOfRecords(String contact) {
        Page<MemberProfile> result = memberProfileService.getMemberProfileByPhoneNumber(contact, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(mp -> mp.getPhoneNumber().contains(contact)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"000000000"})
    void testGetMemberProfileByPhoneNumber_WhenPhoneNumberDoesNotMatch_ShouldReturnEmptyPage(String contact) {
        Page<MemberProfile> result = memberProfileService.getMemberProfileByPhoneNumber(contact, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"My example address", "My eXaMplE adDrEsS", "MY EXAMPLE ADDRESS", "my example address"})
    void testGetMemberProfileByAddress_WhenAddressFullyMatchIgnoringCase_ShouldReturnPageOfRecords(String address) {
        Page<MemberProfile> result = memberProfileService.getMemberProfileByAddress(address, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(mp -> mp.getAddress().equalsIgnoreCase(address)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"aDdreSs", "EXAMPLE", "my"})
    void testGetMemberProfileByAddress_WhenAddressPartiallyMatchIgnoringCase_ShouldReturnPageOfRecords(String address) {
        Page<MemberProfile> result = memberProfileService.getMemberProfileByAddress(address, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertTrue(result.stream().allMatch(mp -> mp.getAddress().toLowerCase().contains(address.toLowerCase())));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"non-matching address"})
    void testGetMemberProfileByAddress_WhenAddressDoesNotMatch_ShouldReturnEmptyPage(String address) {
        Page<MemberProfile> result = memberProfileService.getMemberProfileByAddress(address, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"member@gmail.com", "mEmBeR@gMaiL.cOm", "MEMBER@GMAIL.COM"})
    void testGetMemberProfileByMemberEmail_WhenMemberEmailFullyMatchIgnoringCase_ShouldReturnPageOfRecords(String email) {
        MemberProfile result = memberProfileService.getMemberProfileByMemberEmail(email);

        assertNotNull(result);
        assertTrue(result.getMember().getEmail().equalsIgnoreCase(email));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"member", "ber@gmail", "MEMBER"})
    void testGetMemberProfileByMemberEmail_WhenMemberEmailNotFullyMatchIgnoringCase_ShouldThrowException(String email) {
        MemberProfileNotFoundException result = assertThrows(MemberProfileNotFoundException.class, () -> memberProfileService.getMemberProfileByMemberEmail(email));

        assertNotNull(result);
        assertEquals("Member profile not found with the email: " + email, result.getMessage());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @ParameterizedTest
    @ValueSource(strings = {"nonmatchingemail@gmail.com"})
    void testGetMemberProfileByMemberEmail_WhenMemberEmailDoesNotMatch_ShouldThrowException(String email) {
        MemberProfileNotFoundException result = assertThrows(MemberProfileNotFoundException.class, () -> memberProfileService.getMemberProfileByMemberEmail(email));

        assertNotNull(result);
        assertEquals("Member profile not found with the email: nonmatchingemail@gmail.com", result.getMessage());
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetMemberProfileByDateOfBirth_WhenFullDateIsProvided_ShouldReturnPageOfRecords() {
        LocalDate startDate = LocalDate.of(2006, 2, 13); // "13-Feb-2006
        LocalDate endDate = LocalDate.of(2006, 2, 13); // "13-Feb-2006
        Page<MemberProfile> result = memberProfileService.getMemberProfileByDateOfBirth(startDate, endDate, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(1, result.getTotalElements()); // Only exact match should return
        assertTrue(result.stream().allMatch(mp -> mp.getDateOfBirth().equals(startDate)));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetMemberProfileByDateOfBirth_WhenMonthAndYearIsProvided_ShouldReturnPageOfRecords() {
        LocalDate startDate = LocalDate.of(2006, 2, 1); // "1-Feb-2006
        LocalDate endDate = LocalDate.of(2006, 2, 28); // "28-Feb-2006
        Page<MemberProfile> result = memberProfileService.getMemberProfileByDateOfBirth(startDate, endDate, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(2, result.getTotalElements()); //Both match should return
        assertTrue(result.stream().allMatch(mp ->                                           // birth date must be in the range
                mp.getDateOfBirth().isAfter(startDate.minusDays(1)) &&
                        mp.getDateOfBirth().isBefore(endDate.plusDays(1))
        ));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetMemberProfileByDateOfBirth_WhenOnlyYearIsProvided_ShouldReturnPageOfRecords() {
        LocalDate startDate = LocalDate.of(2006, 1, 1); // "1-Jan-2006
        LocalDate endDate = LocalDate.of(2006, 12, 31); // "31-Dec-2006
        Page<MemberProfile> result = memberProfileService.getMemberProfileByDateOfBirth(startDate, endDate, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(2, result.getTotalElements()); //Both match should return
        assertTrue(result.stream().allMatch(mp ->
                mp.getDateOfBirth().isAfter(startDate.minusDays(1)) &&
                        mp.getDateOfBirth().isBefore(endDate.plusDays(1))
        ));
    }

    @WithMockUser(username = "Admin", roles = "ADMIN")
    @Test
    void testGetMemberProfileByDateOfBirth_WhenDateDoesNotMatch_ShouldReturnPageOfRecords() {
        LocalDate startDate = LocalDate.of(1999, 2, 13); // "13-Feb-1999
        LocalDate endDate = LocalDate.of(1999, 2, 13); // "13-Feb-1999
        Page<MemberProfile> result = memberProfileService.getMemberProfileByDateOfBirth(startDate, endDate, 0, 10, "ASC", "id");

        assertEquals("id: ASC", result.getPageable().getSort().toString());
        assertEquals(0, result.getPageable().getPageNumber());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(0, result.getTotalElements());
    }
}