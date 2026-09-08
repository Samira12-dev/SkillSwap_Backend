package com.example.skillswap.dto.response;

import com.example.skillswap.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String city;

    private String photo;

    private Role role;
}