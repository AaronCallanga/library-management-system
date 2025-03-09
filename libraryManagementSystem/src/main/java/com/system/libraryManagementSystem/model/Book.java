package com.system.libraryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String genre;

    private int publicationYear;

    @ManyToOne
    @JoinColumn(name = "author_id")         //when you post data to postman, only the field with @JoinColumn must have the data from its parent
    private Author author;

    @ManyToMany(mappedBy = "borrowedBooks") //no need in post when you create a new book
    private List<Member> members;
}

