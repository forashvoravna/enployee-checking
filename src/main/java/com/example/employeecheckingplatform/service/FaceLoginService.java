package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.config.FaceBridgeClient;
import com.example.employeecheckingplatform.dto.sec.AuthResponse;
import com.example.employeecheckingplatform.entity.Foydalanuvchi;
import com.example.employeecheckingplatform.entity.Role;
import com.example.employeecheckingplatform.repository.FoydalanuvchiRepository;
import com.example.employeecheckingplatform.config.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FaceLoginService {
    private final FaceBridgeClient bridge;
    private final FoydalanuvchiRepository userRepo;
    private final JwtService jwt;


    @Transactional
    public AuthResponse loginByBase64(String base64Image) {
        String username = bridge.sendBase64AndGetUsername(base64Image); // endi bu yangi metodni chaqiradi

        Foydalanuvchi user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi: " + username));

        if (!user.getFaol())
            throw new RuntimeException("Foydalanuvchi faol emas");

        var token = jwt.generateForSubject(
                username,
                Map.of(
                        "role", user.getRoli() != null ? user.getRoli().name() : Role.USER,
                        "name", user.getToliqIsm()
                )
        );

        return new AuthResponse(token, "Bearer", 60L * 60L);
    }

}
