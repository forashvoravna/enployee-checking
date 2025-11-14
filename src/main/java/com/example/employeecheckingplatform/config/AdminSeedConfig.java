package com.example.employeecheckingplatform.config;

import com.example.employeecheckingplatform.entity.Foydalanuvchi;
import com.example.employeecheckingplatform.entity.Role;
import com.example.employeecheckingplatform.entity.Tashkilot;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.FoydalanuvchiRepository;
import com.example.employeecheckingplatform.repository.TashkilotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminSeedConfig {

    private final FoydalanuvchiRepository userRepo;
    private final TashkilotRepository tashkilotRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.name:}")
    private String adminName;

    // ixtiyoriy: to'g'ridan-to'g'ri ID berish
    @Value("${app.admin.tashkilot-id:}")
    private String adminTashkilotIdStr;

    // ixtiyoriy: nom bo'yicha topish/yaratish
    @Value("${app.tashkilot.name:}")
    private String adminTashkilotName;

    @Value("${app.tashkilot.turi:}")
    private String adminTashkilotTuri;

    @Bean
    public CommandLineRunner seedAdmin() {
        return args -> doSeed();
    }

    @Transactional
    public void doSeed() {
        if (adminUsername == null || adminUsername.isBlank()
                || adminPassword == null || adminPassword.isBlank()
                || adminName == null || adminName.isBlank()) {
            log.warn("Admin seed SKIPPED: app.admin.username/password/name berilmagan");
            return;
        }

        if (userRepo.existsByUsername(adminUsername)) {
            log.info("Admin allaqachon mavjud: {}", adminUsername);
            return;
        }

        // 1) Tashkilotni aniqlash (id -> name -> default Root)
        Tashkilot org = resolveAdminOrg();

        var admin = Foydalanuvchi.builder()
                .toliqIsm(adminName)
                .username(adminUsername)
                .parol(passwordEncoder.encode(adminPassword))
                .roli(Role.ADMIN)
                .tashkilot(org)
                .faol(true)
                .build();

        userRepo.save(admin);
        log.info("Admin yaratildi: {} (org='{}', id={})",
                adminUsername, org.getNomi(), org.getId());
    }

    private Tashkilot resolveAdminOrg() {
        if (adminTashkilotIdStr != null && !adminTashkilotIdStr.isBlank()) {
            try {
                Long id = Long.parseLong(adminTashkilotIdStr.trim());
                return tashkilotRepo.findById(id)
                        .orElseThrow(() -> new NotFoundException("Admin org ID topilmadi: " + id));
            } catch (NumberFormatException e) {
                log.warn("app.admin.tashkilot-id noto'g'ri format: '{}'", adminTashkilotIdStr);
            }
        }

        Optional<Tashkilot> byName = tashkilotRepo.findAll().stream()
                .filter(t -> t.getNomi().equalsIgnoreCase(adminTashkilotName))
                .findFirst();

        if (byName.isPresent()) return byName.get();

        Tashkilot root = Tashkilot.builder()
                .nomi(adminTashkilotName)
                .turi(adminTashkilotTuri)
                .parent(null)
                .faol(true)
                .build();
        return tashkilotRepo.save(root);
    }
}
