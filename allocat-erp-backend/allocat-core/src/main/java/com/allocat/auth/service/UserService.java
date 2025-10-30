package com.allocat.auth.service;

import com.allocat.auth.entity.Role;
import com.allocat.auth.entity.User;
import com.allocat.auth.repository.RoleRepository;
import com.allocat.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }
    
    public List<User> getUsersByStoreId(Long storeId) {
        log.info("Fetching users for store ID: {}", storeId);
        return userRepository.findByStoreId(storeId);
    }
    
    public User getUserByUsername(String username) {
        log.info("Fetching user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
    
    public User createUser(User user) {
        log.info("Creating user: {}", user.getUsername());
        return userRepository.save(user);
    }
    
    public Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
    }
    
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
