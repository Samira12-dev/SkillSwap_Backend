package com.example.skillswap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
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

    @PrePersist
    protected  void onCreated(){
        this.createdAt= LocalDateTime.now();
    }
}
