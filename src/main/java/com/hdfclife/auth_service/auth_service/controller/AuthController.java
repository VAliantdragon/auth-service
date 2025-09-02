package com.hdfclife.auth_service.auth_service.controller;

import com.hdfclife.auth_service.auth_service.model.LoginRequest;
import com.hdfclife.auth_service.auth_service.model.LoginResponse;
import com.hdfclife.auth_service.auth_service.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/internal/auth")
@Tag(name = "Authentication", description = "Endpoints for login and logout")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Login with username and password",
            description = "Returns a JWT token upon successful login",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful login",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Invalid username or password\"}")))
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Auth-service processing login for user: {}", loginRequest.getUsername());
        String token = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @Operation(
            summary = "Logout from the system",
            description = "Invalidates the JWT token provided in the Authorization header",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer JWT token", required = true,
                            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully logged out",
                            content = @Content(schema = @Schema(example = "{\"message\": \"Logged out successfully\"}"))),
                    @ApiResponse(responseCode = "400", description = "Missing or invalid Authorization header",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Authorization header is missing or invalid.\"}")))
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authorization header is missing or invalid."));
        }
        String token = authorizationHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}