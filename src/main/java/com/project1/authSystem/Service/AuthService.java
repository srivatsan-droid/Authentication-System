package com.project1.authSystem.Service;

import com.project1.authSystem.DTO.request.AuthRequest;
import com.project1.authSystem.DTO.response.AuthResponse;
import com.project1.authSystem.DTO.response.UserResponse;
import com.project1.authSystem.Entity.User;
import com.project1.authSystem.Exception.UserNotFoundException;
import com.project1.authSystem.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(AuthRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        // 1. Authenticate user credentials via Spring Security AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.getPassword()
                )
        );

        // 2. Fetch user from database
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("Invalid email or password"));

        // 3. Generate JWT Token
        String accessToken = jwtService.generateToken(user);
        long expirationTime = jwtService.getExpirationTime();

        // 4. Build and return the response containing the token and user details
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(expirationTime);
        response.setUser(mapToResponse(user));

        return response;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setUserStatus(user.getUserStatus());
        return response;
    }
}