package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.dto.LoginRequestDto;
import com.iskcondhanbad.donordash.dto.LoginResponseDto;
import com.iskcondhanbad.donordash.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("user-login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            LoginResponseDto response = authService.loginUser(loginRequestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}