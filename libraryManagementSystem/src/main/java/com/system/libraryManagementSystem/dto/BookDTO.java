package com.system.libraryManagementSystem.dto;

import com.system.libraryManagementSystem.dto.validation.annotation.AuthorId;
import com.system.libraryManagementSystem.dto.validation.annotation.PublicationYear;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class BookDTO {

    private Long id;

    @NotBlank(message = "Book's title must not be blank")
    private String title;

    @NotBlank(message = "Book's genre must not be blank")
    private String genre;

    @Positive(message = "Book's publication year must be a positive number")
    @PublicationYear //value <= present year && value >= 0
    private int publicationYear;

//    @NotBlank(message = "Author's id must not be blank") cant be used for Long, only for strings, @NotEmpty for collections and strings
//    @NotNull(message = "Author's id must not be blank) i think 0 == null
    @Min(value = 0, message = "Book's author id must be valid")
    @AuthorId  //must be between 0 and current count, maybe findAll().count
    private Long authorId;

    @NotBlank(message = "Author's name must not be blank")
    @Size(min = 2, max = 50, message = "Author's name must be between 2 to 50 characters")
    private String authorName;
}
