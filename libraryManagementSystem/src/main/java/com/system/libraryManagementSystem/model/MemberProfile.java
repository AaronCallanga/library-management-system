package com.system.libraryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class MemberProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;

//    @Column(unique = true, nullable = false)
//    private String email;

    private String address;

    private LocalDate dateOfBirth;

    @OneToOne //if memberProfile is deleted, member too. Member deleted, memberProfile too. that is why they both have cascadeType = ALL
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

}
