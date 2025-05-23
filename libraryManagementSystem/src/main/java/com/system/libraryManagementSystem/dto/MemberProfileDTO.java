package com.system.libraryManagementSystem.dto;

import com.system.libraryManagementSystem.dto.validation.annotation.ContactNumber;
import com.system.libraryManagementSystem.dto.validation.annotation.MemberId;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class MemberProfileDTO {

    private Long memberProfileId;

    @Min(value = 0, message = "Member's id must be valid")
    @MemberId
    private Long memberId;

    @NotBlank(message = "Member's name must not be blank")
    @Size(min = 2, max = 50, message = "Member's name must be between 2 to 50 characters")
    private String memberName;

    @ContactNumber  //custom
    private String phoneNumber;

    @NotBlank(message = "Member's address must not be blank")
    @Size(min = 10, max = 50, message = "Member's address must between 10 to 50 characters")
    private String address;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Email
    private String memberEmail;
}
