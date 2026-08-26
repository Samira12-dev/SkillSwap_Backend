package com.example.skillswap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;

    @OneToMany(mappedBy = "skill")
    private List<SkillDetails> skillDetails;

    @OneToMany(mappedBy = "skillOffered")
    private List<SwapRequest> offeredSwapRequests;

    @OneToMany(mappedBy = "skillWanted")
    private List<SwapRequest> wantedSwapRequests;
}

