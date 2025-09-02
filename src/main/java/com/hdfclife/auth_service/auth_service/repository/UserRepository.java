package com.hdfclife.auth_service.auth_service.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdfclife.auth_service.auth_service.model.User;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private final String userDataFile;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public UserRepository(@Value("${user.data.file}") String userDataFile, ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.userDataFile = userDataFile;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + userDataFile);
            if (!resource.exists()) {
                logger.error("User data file '{}' not found on the classpath. Authentication will fail.", userDataFile);
                return;
            }
            try (InputStream is = resource.getInputStream()) {
                List<User> userList = objectMapper.readValue(is, new TypeReference<List<User>>() {});
                userList.forEach(user -> users.put(user.getUsername(), user));
                logger.info("Successfully loaded {} users from {}", users.size(), resource.getFilename());
            }
        } catch (IOException e) {
            logger.error("Failed to load user data from {}: {}", userDataFile, e.getMessage(), e);
        }
    }

    public Optional<User> findByUsername(String username) {
        logger.debug("Attempting to find user by username: {}", username);
        return Optional.ofNullable(users.get(username));
    }
}