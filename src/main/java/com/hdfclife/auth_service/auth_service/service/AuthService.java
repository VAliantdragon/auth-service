package com.hdfclife.auth_service.auth_service.service;

import com.hdfclife.auth_service.auth_service.exception.InvalidCredentialsException;
import com.hdfclife.auth_service.auth_service.exception.UserNotFoundException;
import com.hdfclife.auth_service.auth_service.model.User;
import com.hdfclife.auth_service.auth_service.repository.UserRepository;
import com.hdfclife.auth_service.auth_service.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final JwtUtil jwtUtil;
    private final InMemoryTokenStore tokenStore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtUtil jwtUtil, InMemoryTokenStore tokenStore, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.tokenStore = tokenStore;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(String username, String rawPassword) {
        logger.debug("Login attempt for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Invalid username or password"));

        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            String token = jwtUtil.generateToken(username);
            tokenStore.addToken(token);
            logger.info("User {} logged in successfully. Token generated.", username);
            return token;
        } else {
            logger.warn("Login failed for user {}: Incorrect password.", username);
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    public void logout(String token) {
        if (token != null && tokenStore.isTokenValid(token)) {
            tokenStore.invalidateToken(token);
            String username = jwtUtil.extractUsername(token);
            logger.info("User {} logged out successfully. Token invalidated.", username);
        } else {
            logger.warn("Logout attempted with an invalid or already invalidated token.");
        }
    }

    public String validateTokenAndGetUserInfo(String token) {
        if (token == null || token.isEmpty() || !tokenStore.isTokenValid(token)) {
            return null;
        }
        try {
            String username = jwtUtil.extractUsername(token);
            if (username != null && jwtUtil.validateToken(token, username)) {
                return username;
            }
        } catch (Exception e) {
            logger.warn("Token validation failed with exception: {}", e.getMessage());
        }
        return null;
    }
}