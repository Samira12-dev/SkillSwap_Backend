package com.example.skillswap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private Integer rating;
    private String comment;
    private Long reviewerId;
    private String reviewerName;
    private Long revieweeId;
    private String revieweeName;
    private Long sessionId;
}
