package com.project1.authSystem.Service;

import com.project1.authSystem.DTO.request.ChangePasswordRequest;
import com.project1.authSystem.DTO.request.RegisterRequest;
import com.project1.authSystem.DTO.request.UpdateUserRequest;
import com.project1.authSystem.DTO.response.UserResponse;
import com.project1.authSystem.Entity.Role;
import com.project1.authSystem.Entity.User;
import com.project1.authSystem.Exception.EmailAlreadyExistsException;
import com.project1.authSystem.Exception.UserNotFoundException;
import com.project1.authSystem.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email is already registered for another user");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Recommended: encode password here using BCrypt
        user.setUserStatus(Role.USER);

        try {
            User saved = userRepository.saveAndFlush(user);
            return mapToResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // Concurrent requests caught by DB unique constraint
            throw new EmailAlreadyExistsException("Email is already registered for another user");
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + email));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUsers(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = findUserOrThrow(id);

        // Recommended: check if old password matches before setting the new one
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUsers(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
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