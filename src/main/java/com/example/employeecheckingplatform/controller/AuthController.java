package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.config.FaceBridgeClient;
import com.example.employeecheckingplatform.dto.*;
import com.example.employeecheckingplatform.dto.sec.AuthResponse;
import com.example.employeecheckingplatform.dto.sec.LoginRequest;
import com.example.employeecheckingplatform.dto.sec.RegisterRequest;
import com.example.employeecheckingplatform.entity.Foydalanuvchi;
import com.example.employeecheckingplatform.exception.BusinessException;
import com.example.employeecheckingplatform.repository.FoydalanuvchiRepository;
import com.example.employeecheckingplatform.service.AuthService;
import com.example.employeecheckingplatform.service.FaceLoginService;
import com.example.employeecheckingplatform.service.FoydalanuvchiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    private final FoydalanuvchiRepository foydalanuvchiRepository;

    private final FaceLoginService faceLogin;
    private final FaceBridgeClient client;

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
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails user) {
        Foydalanuvchi foydalanuvchi =  foydalanuvchiRepository.findByUsername(user.getUsername()).orElseThrow(() -> new BusinessException("Bunday foydalanuvchi topilmadi"));
        Map<String, Object> resp = Map.of(
                "toliqIsmi", foydalanuvchi.getToliqIsm(),
                "username", user.getUsername(),
                "roles", user.getAuthorities()
        );
        return ResponseEntity.ok(resp);
    }
}
