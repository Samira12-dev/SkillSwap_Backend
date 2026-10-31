package com.example.skillswap.entity;

import com.example.skillswap.enums.SessionMode;
import com.example.skillswap.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    private Integer duration;

    @Enumerated(EnumType.STRING)
    private SessionMode mode;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;
    private String meetingUrl;

    @Column(name = "completion_requested_at")
    private LocalDateTime completionRequestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completion_requested_by")
    private User completionRequestedBy;

    @Column(name = "completion_disputed_at")
    private LocalDateTime completionDisputedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @OneToMany(mappedBy = "session")
    private List<Review> reviews;
}