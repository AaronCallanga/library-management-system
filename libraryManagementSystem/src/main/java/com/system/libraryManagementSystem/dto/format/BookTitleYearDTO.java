package com.system.libraryManagementSystem.dto.format;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class BookTitleYearDTO {

    @NotBlank(message = "Book's title must not be blank")
    private String title;

    private int publicationYear;
}
