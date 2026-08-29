package com.example.skillswap.entity;

import com.example.skillswap.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    private String city;

    @Column(length = 1000)
    private String bio;

    private String photo;

    private Double rating;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    private List<SkillDetails> skillDetails;

    @OneToMany(mappedBy = "sender")
    private List<SwapRequest> sentSwapRequests;

    @OneToMany(mappedBy = "receiver")
     private List<SwapRequest> receivedSwapRequests;

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;

    @OneToMany(mappedBy = "reviewer")
    private List<Review> givenReviews;

    @OneToMany(mappedBy = "reviewee")
    private List<Review> receivedReviews;

    @OneToMany(mappedBy = "sender")
    private List<Message> sentMessages;
    @Enumerated(EnumType.STRING)
    private Role role;

    @PrePersist
    protected  void onCreated(){
        this.createdAt= LocalDateTime.now();
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name().toUpperCase()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
