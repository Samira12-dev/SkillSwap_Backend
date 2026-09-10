package com.example.skillswap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MatchingResponseDto {

    private Long userId;
    private String firstName;
    private String lastName;
    private String photo;
    private int score;
}