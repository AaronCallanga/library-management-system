package com.system.libraryManagementSystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.system.libraryManagementSystem.dto.validation.annotation.BookId;
import com.system.libraryManagementSystem.dto.validation.annotation.MemberId;
import com.system.libraryManagementSystem.dto.validation.groups.OnCreate;
import com.system.libraryManagementSystem.dto.validation.groups.OnUpdate;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.Member;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class BorrowingRecordDTO {

    private Long recordId;

    @Min(value = 0, message = "Book's id must be valid")
    @BookId // must be between 0 and current count, maybe findAll().count
    private Long bookId;

    @NotBlank(message = "Book's title must not be blank")
    private String bookTitle;

    @Min(value = 0, message = "Member's id must be valid")
    @MemberId //must be between 0 and current count, maybe findAll().count, memberId must be existing
    private Long memberId;

    @NotBlank(message = "Member's name must not be blank")
    @Size(min = 2, max = 50, message = "Member's name must be between 2 to 50 characters")
    private String memberName;

    @NotBlank(message = "Member's email must not be blank")
    @Email(message = "Member's email must be valid and proper format: example@gmail.com")
    private String memberEmail;

    @NotNull(message = "Date must not be blank to borrow")   //create a group, this should be inCreation group,  optional
    @PastOrPresent(message = "Date must be between now or in the past to borrow")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMM-yyyy HH:mm:ss")
    private LocalDateTime borrowDate;

//    @NotNull(message = "Return date must not be blank") //create a group, this should be inUpdate group, optional
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMM-yyyy HH:mm:ss")
    private LocalDateTime returnDate;


    private boolean isApproved;
}
