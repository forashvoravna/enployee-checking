package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.dto.foydalanuvchi.FoydalanuvchiCreateDto;
import com.example.employeecheckingplatform.dto.foydalanuvchi.FoydalanuvchiResponseDto;
import com.example.employeecheckingplatform.dto.foydalanuvchi.FoydalanuvchiUpdateDto;
import com.example.employeecheckingplatform.entity.Foydalanuvchi;
import com.example.employeecheckingplatform.entity.Role;
import com.example.employeecheckingplatform.exception.BusinessException;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.FoydalanuvchiRepository;
import com.example.employeecheckingplatform.repository.TashkilotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoydalanuvchiService {
    private final FoydalanuvchiRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final TashkilotRepository tashkilotRepository;


    @Transactional
    public FoydalanuvchiResponseDto create(FoydalanuvchiCreateDto dto) {
        if (repo.existsByUsername(dto.username()))
            throw new BusinessException("Username (JShShIR) band");

        var org = tashkilotRepository.findById(dto.tashkilotId())
                .orElseThrow(() -> new NotFoundException("Tashkilot topilmadi"));

        var role = Role.valueOf(dto.roli().toUpperCase()); // ADMIN/USER

        var user = Foydalanuvchi.builder()
                .toliqIsm(dto.toliqIsm())
                .username(dto.username())
                .parol(dto.parol() == null || dto.parol().isBlank()
                        ? null : passwordEncoder.encode(dto.parol()))
                .roli(role)
                .faol(true)
                .tashkilot(org)   // 🔗 majburiy biriktirish
                .build();

        var saved = repo.save(user);
        return toDto(saved);
    }

    @Transactional
    public FoydalanuvchiResponseDto changeOrg(Long userId, Long newTashkilotId) {
        var user = repo.findById(userId).orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        var org = tashkilotRepository.findById(newTashkilotId).orElseThrow(() -> new NotFoundException("Tashkilot topilmadi"));
        user.setTashkilot(org);
        return toDto(repo.save(user));
    }

    private static FoydalanuvchiResponseDto toDto(Foydalanuvchi u) {
        return new FoydalanuvchiResponseDto(
                u.getId(), u.getToliqIsm(), u.getUsername(),
                u.getRoli().name(),
                u.getTashkilot() != null ? u.getTashkilot().getId() : null,
                u.getTashkilot().getNomi(),
                u.getTashkilot().getTuri(),
                u.getFaol()
        );
    }

    @Transactional(readOnly = true)
    public FoydalanuvchiResponseDto get(Long id) {
        return toDto(repo.findById(id).orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi")));
    }

    @Transactional(readOnly = true)
    public List<FoydalanuvchiResponseDto> list() {
        return repo.findAll().stream().map(FoydalanuvchiService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<FoydalanuvchiResponseDto> faol() {
        return repo.findAllByFaolIs(true).stream().map(FoydalanuvchiService::toDto).toList();
    }

    @Transactional
    public FoydalanuvchiResponseDto update(Long id, FoydalanuvchiUpdateDto dto) {
        Foydalanuvchi f = repo.findById(id).orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        if (dto.username() != null && !dto.username().equals(f.getUsername())) {
            if (repo.existsByUsername(dto.username())) throw new BusinessException("Username band");
            f.setUsername(dto.username());
        }
        if (dto.toliqIsm() != null) f.setToliqIsm(dto.toliqIsm() );
        if (dto.faol() != null) f.setFaol(dto.faol());
        if (dto.roli() != null) f.setRoli(Role.valueOf(dto.roli()));
        return toDto(repo.save(f));
    }

    @Transactional
    public String setPassword(Long id, String newPassword) {
        var f = repo.findById(id).orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        f.setParol(passwordEncoder.encode(newPassword));
        repo.save(f);
        return "Parol muvafaqaiyatli o'zgartirildi";
    }

    @Transactional
    public String delete(Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Foydalanuvchi topilmadi");
        repo.deleteById(id);
        return "Foydalanuvchi muvafaqaiyatli o'chirildi";
    }

}
