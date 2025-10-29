package com.web3.web3j.controller;

import com.web3.web3j.DTO.CreateUser;
import com.web3.web3j.model.UserAccount;
import com.web3.web3j.service.UserService;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/userAccount")
public class UserAccountController {
    private final UserService userService;
    public UserAccountController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<UserAccount> createUser(@RequestBody CreateUser request) {
        if (request.getEmail() == null || request.getUsername() == null) {
            return ResponseEntity.badRequest().build();
        }

        UserAccount user = userService.createUser(request.getUsername(), request.getEmail());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/find/{userId}")
    public ResponseEntity<UserAccount> getUser(@PathVariable Long userId) {
        Optional<UserAccount> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/find/username/{username}")
    public ResponseEntity<UserAccount> getUserByUsername(@PathVariable String username) {
        Optional<UserAccount> user = userService.getUserByUsername(username);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<UserAccount> updateUser(@PathVariable Long userId, @RequestBody UserAccount request) {
        Optional<UserAccount> updatedUser = userService.updateUser(userId, request);
        return updatedUser.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}
