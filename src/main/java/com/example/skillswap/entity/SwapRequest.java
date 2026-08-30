package com.example.skillswap.entity;

import com.example.skillswap.enums.SwapStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "swap_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SwapStatus swapStatus;

    private String message;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_offered_id")
    private Skill skillOffered;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_wanted_id")
    private Skill skillWanted;

    @OneToOne(mappedBy = "swapRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Conversation conversation;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();

        if (swapStatus == null) {
            swapStatus = SwapStatus.PENDING;
        }
    }
}