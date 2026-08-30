package com.example.skillswap.dto.response;

import com.example.skillswap.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String city;

    private String bio;

    private String photo;

    private Double rating;

    private LocalDateTime createdAt;

    private Role role;
}
