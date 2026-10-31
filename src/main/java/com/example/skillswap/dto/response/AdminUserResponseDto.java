package com.example.skillswap.dto.response;

import com.example.skillswap.enums.AccountStatus;
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
public class AdminUserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String city;
    private String photo;
    private Double rating;
    private Role role;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
}
