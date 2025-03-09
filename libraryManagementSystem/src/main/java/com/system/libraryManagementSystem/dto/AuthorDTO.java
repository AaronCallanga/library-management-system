package com.system.libraryManagementSystem.dto;

import com.system.libraryManagementSystem.dto.format.BookTitleYearDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class AuthorDTO {

    private Long id;

    @NotBlank( message = "Author's name must not be blank")
    @Size(min = 2, max = 50, message = "Author's name must be between 2 to 50 characters")
    private String name;

    @Size(min = 20, max = 200, message = "Biography must be between 20 to 200 characters")
    @NotBlank(message = "Author's biography must not be blank")
    private String biography;

    //i think it is not necessary because when we create authors, books are not defined
    private List<BookTitleYearDTO> publishedBooks;
}
