package com.system.libraryManagementSystem.dto;

import com.system.libraryManagementSystem.dto.format.BookTitleAuthorDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

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

    private List<BookTitleAuthorDTO> borrowedBooks;
}
