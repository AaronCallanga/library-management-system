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
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String biography;

    @OneToMany(mappedBy = "author", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)        //cascade = what happens to parent will also reflect to children, parent save -> child save, parent delete -> child delete, parent update -> child update etc, use cascade if you want your children to be affected too
    private List<Book> publishedBooks;
}
