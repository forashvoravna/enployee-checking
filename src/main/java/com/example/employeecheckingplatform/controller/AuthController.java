package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.*;
import com.example.employeecheckingplatform.dto.sec.AuthResponse;
import com.example.employeecheckingplatform.dto.sec.LoginRequest;
import com.example.employeecheckingplatform.dto.sec.RegisterRequest;
import com.example.employeecheckingplatform.entity.User;
import com.example.employeecheckingplatform.service.AuthService;
import com.example.employeecheckingplatform.service.FaceLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;


    private final FaceLoginService faceLogin;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req){
        return ResponseEntity.ok(service.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req){
        return ResponseEntity.ok(service.login(req));
    }
    @PostMapping("/validation")
    public ResponseEntity<?> validation(Principal principal, @RequestBody String base64){
        return ResponseEntity.ok(service.validation(principal, base64));
    }


    @PostMapping(path = "/face-login-base64", consumes = "application/json")
    public ResponseEntity<AuthResponse> faceLoginBase64(@Valid @RequestBody FaceLoginBase64Request req) {
        return ResponseEntity.ok(faceLogin.loginByBase64(req.imageBase64()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) throws MalformedURLException {
        return ResponseEntity.ok(service.getCurrentUserDetail(user));
    }
}
