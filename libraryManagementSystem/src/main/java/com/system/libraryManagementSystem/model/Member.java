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
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(
            name = "member_book_borrowed",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private List<Book> borrowedBooks;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)     //cascade = all operations must be cascaded to the memberProfile(child)
    private MemberProfile memberProfile;

    public void setMemberProfile(MemberProfile memberProfile) {
        if (memberProfile == null) {
            if (this.memberProfile != null) {
                this.memberProfile.setMember(null);
            }
        } else {
            memberProfile.setMember(this);
        }
        this.memberProfile = memberProfile;
    }
}
