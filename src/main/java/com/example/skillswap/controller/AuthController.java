package com.example.skillswap.controller;


import com.example.skillswap.dto.request.LoginRequestDTO;
import com.example.skillswap.dto.request.RegisterRequestDTO;
import com.example.skillswap.dto.request.ResendVerificationRequest;
import com.example.skillswap.dto.request.VerifyEmailRequest;
import com.example.skillswap.dto.response.AuthResponse;
import com.example.skillswap.dto.response.RegistrationResponse;
import com.example.skillswap.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequestDTO request){
        return ResponseEntity.ok( authService.register(request) );
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequestDTO request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<RegistrationResponse> logout(Authentication authentication) {
        authService.logoutAll(authentication.getName());
        return ResponseEntity.ok(
                new RegistrationResponse("Signed out of all sessions.")
        );
    }

    @PostMapping("/verify-email")
    public ResponseEntity<RegistrationResponse> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        return ResponseEntity.ok(authService.verifyEmail(request.getToken()));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<RegistrationResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request
    ) {
        return ResponseEntity.ok(
                authService.resendVerificationEmail(request.getEmail())
        );
    }

}