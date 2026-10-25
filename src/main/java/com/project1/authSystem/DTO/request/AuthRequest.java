package com.project1.authSystem.DTO.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
}
