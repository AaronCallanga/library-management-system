package com.system.libraryManagementSystem.dto;

import com.system.libraryManagementSystem.dto.format.BookTitleAuthorDTO;
import com.system.libraryManagementSystem.security.Role;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class MemberDTO {

    private Long id;

    @NotBlank(message = "Member's name must not be blank")
    @Size(min = 2, max = 50, message = "Member's name must be between 2 to 50 characters")
    private String name;

    @NotBlank(message = "Member's email must not be blank")
    @Email(message = "Member's email must be valid and proper format: example@gmail.com")
    private String email;

    @NotNull(message = "Member's enabled status must not be null")
    private boolean isEnabled;

    private Set<Role> roles = new HashSet<>();

    private List<BookTitleAuthorDTO> borrowedBooks;
}
