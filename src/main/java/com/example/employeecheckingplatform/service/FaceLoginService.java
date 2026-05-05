package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.config.FaceBridgeClient;
import com.example.employeecheckingplatform.dto.sec.AuthResponse;
import com.example.employeecheckingplatform.entity.User;
import com.example.employeecheckingplatform.entity.UserRole;
import com.example.employeecheckingplatform.repository.UserRepository;
import com.example.employeecheckingplatform.config.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FaceLoginService {
    private final FaceBridgeClient bridge;
    private final UserRepository userRepo;
    private final JwtService jwt;


    @Transactional
    public AuthResponse loginByBase64(String base64Image) {
        String username = bridge.sendBase64AndGetUsername(base64Image); // endi bu yangi metodni chaqiradi

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi: " + username));

        var token = jwt.generateForSubject(
                username,
                Map.of(
                        "role", user.getUserRole() != null ? user.getUserRole() : UserRole.USER,
                        "name", user.getLastName()+ " " + user.getFirstName().substring(0, 1).toUpperCase() + "."
                )
        );

        return new AuthResponse(token, "Bearer", 60L * 60L);
    }

}
