package com.example.skillswap.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDTO {

    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @Email(message = "Invalid email format")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "City is required")
    private String city;
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;
    private String photo;
}
