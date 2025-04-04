package com.system.libraryManagementSystem.model;

//import com.system.libraryManagementSystem.security.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
@Table(indexes = @Index(columnList = "email", unique = true))
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isEnabled;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "role")
    private Set<String> roles;

    private boolean isAccountNonExpired;

    private boolean isAccountNonLocked;

    private boolean isCredentialsNonExpired;

    @ManyToMany
    @JoinTable(
            name = "member_book_borrowed",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private List<Book> borrowedBooks = new ArrayList<>();

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
