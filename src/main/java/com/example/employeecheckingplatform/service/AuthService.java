package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.config.FaceBridgeClient;
import com.example.employeecheckingplatform.config.jwt.JwtService;
import com.example.employeecheckingplatform.dto.*;
import com.example.employeecheckingplatform.dto.sec.AuthResponse;
import com.example.employeecheckingplatform.dto.sec.LoginRequest;
import com.example.employeecheckingplatform.dto.sec.RegisterRequest;
import com.example.employeecheckingplatform.entity.Foydalanuvchi;
import com.example.employeecheckingplatform.entity.Role;
import com.example.employeecheckingplatform.repository.FoydalanuvchiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final FoydalanuvchiRepository userRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final FaceBridgeClient client;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.username()))
            throw new RuntimeException("Username band");

        var user = Foydalanuvchi.builder()
                .toliqIsm(req.toliqIsm())
                .username(req.username())
                .parol(encoder.encode(req.parol()))
                .roli(Role.USER)
                .faol(true)
                .build();
        userRepo.save(user);

        var token = jwt.generate(user, Map.of("role", user.getRoli().name(), "name", user.getToliqIsm()));
        long expires = 60L * 60L; // config bilan ham kelish mumkin
        return new AuthResponse(token, "Bearer", expires);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.parol()));
        var user = userRepo.findByUsername(req.username()   ).orElseThrow();
        var token = jwt.generate(user, Map.of("role", user.getRoli().name(), "name", user.getToliqIsm()));
        long expires = 60L * 60L;
        return new AuthResponse(token, "Bearer", expires);
    }
    public Boolean validation(Principal principal, String base64){

        if (client.sendBase64AndGetUsername(base64).equals(principal.getName())){
            return true;
        }else return false;

    }
}
