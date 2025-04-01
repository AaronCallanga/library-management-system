package com.system.libraryManagementSystem.repository;

import com.system.libraryManagementSystem.model.MemberProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {
    @Query("SELECT mp FROM MemberProfile mp JOIN mp.member m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<MemberProfile> findMemberProfileByMemberName(String name, Pageable pageable);
    @Query("SELECT mp FROM MemberProfile mp WHERE mp.phoneNumber LIKE %:phoneNumber%")
    Page<MemberProfile> findMemberProfileByPhoneNumber(String phoneNumber, Pageable pageable);
    @Query("SELECT mp FROM MemberProfile mp WHERE LOWER(mp.address) LIKE LOWER(CONCAT('%', :address, '%'))")
    Page<MemberProfile> findMemberProfileByAddress(String address, Pageable pageable);
    @Query("SELECT mp FROM MemberProfile mp WHERE mp.dateOfBirth BETWEEN :startDate AND :endDate")
    Page<MemberProfile> findMemberProfileByDateOfBirth(LocalDate startDate, LocalDate endDate, Pageable pageable); //by year and by year-month
    @Query("SELECT mp FROM MemberProfile mp JOIN mp.member m WHERE m.email = :email")
    Optional<MemberProfile> findMemberProfileByEmail(String email);

    boolean existsByMemberId(Long memberId);
}
