package com.system.libraryManagementSystem.dto.format;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class BookTitleAuthorDTO {

    private String bookTitle;

    private String authorName;
}
