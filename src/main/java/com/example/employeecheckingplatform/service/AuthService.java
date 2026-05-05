package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.config.FaceBridgeClient;
import com.example.employeecheckingplatform.config.jwt.JwtService;
import com.example.employeecheckingplatform.dto.sec.AuthResponse;
import com.example.employeecheckingplatform.dto.sec.LoginRequest;
import com.example.employeecheckingplatform.dto.sec.RegisterRequest;
import com.example.employeecheckingplatform.dto.user.UserResponseDTO;
import com.example.employeecheckingplatform.entity.Attachment;
import com.example.employeecheckingplatform.entity.Unit;
import com.example.employeecheckingplatform.entity.User;
import com.example.employeecheckingplatform.exception.BusinessException;
import com.example.employeecheckingplatform.repository.AttachmentRepository;
import com.example.employeecheckingplatform.repository.UnitRepository;
import com.example.employeecheckingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final FaceBridgeClient faceClient;
    private final UnitRepository unitRepository;
    private final AttachmentRepository attachmentRepository;

    // Token amal qilish muddati (1 soat = 3600 soniya)
    private static final long EXPIRES_IN = 3600L;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            throw new BusinessException("Username band");
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPassword(encoder.encode(req.password())); // DTO'da password deb nomlangan
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setMiddleName(req.middleName() != null ? req.middleName() : " ");
        user.setJshshir(req.jshshir());
        user.setUserRole(req.role());
        user.setBranchId(req.branchId());
        user.setRankId(req.rankId());
        user.setGender(req.gender());

        userRepo.save(user);

        return generateAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        User user = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new BusinessException("Foydalanuvchi topilmadi"));

        return generateAuthResponse(user);
    }

    public Boolean validation(Principal principal, String base64) {
        String recognizedUsername = faceClient.sendBase64AndGetUsername(base64);
        return Objects.equals(recognizedUsername, principal.getName());
    }

    public UserResponseDTO getCurrentUserDetail(User user) {
        if (user == null) {
            throw new BusinessException("Foydalanuvchi tizimga kirmagan");
        }
        Attachment attachment = attachmentRepository.findByHashIdAndDeleted(user.getAttachmentId(), false).orElse(new Attachment());

        Unit unit = unitRepository.findById(user.getBranchId()).orElseThrow();



        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName() != null ? user.getMiddleName() : " ")
                .fullName(formatFullName(user))
                .jshshir(user.getJshshir())
                .gender(user.getGender())
                .role(user.getUserRole())
                .branchId(user.getBranchId())
                .branchName(unit.getNomi())
                .rankId(user.getRankId())
                .attachmentId(attachment.getHashId() != null ? attachment.getHashId() : "rasm")
                .build();
    }

    // --- Yordamchi metodlar (Private) ---

    private AuthResponse generateAuthResponse(User user) {
        // JWT ichiga foydalanuvchi roli va ismini joylaymiz
        Map<String, Object> extraClaims = Map.of(
                "role", user.getUserRole().name(),
                "name", user.getFirstName()
        );

        String token = jwtService.generate(user, extraClaims);

        // Siz bergan AuthResponse recordiga moslab qaytarish
        return new AuthResponse(token, "Bearer", EXPIRES_IN);
    }

    private String formatFullName(User user) {
        return String.format("%s %s %s",
                Objects.toString(user.getLastName(), ""),
                Objects.toString(user.getFirstName(), ""),
                Objects.toString(user.getMiddleName(), " ")
        ).trim().replaceAll("\\s+", " ");
    }
}