package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.dto.RuxsatUserDto;
import com.example.employeecheckingplatform.dto.imtihon.*;
import com.example.employeecheckingplatform.entity.Imtihon;
import com.example.employeecheckingplatform.entity.User;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.FanRepository;
import com.example.employeecheckingplatform.repository.ImtihonRepository;
import com.example.employeecheckingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImtihonService {

    private final ImtihonRepository imtihonRepo;
    private final FanRepository fanRepo;
    private final UserRepository userRepo;

    @Transactional
    public ImtihonResponseDto create(ImtihonCreateDto dto) {
        Imtihon imtihon = Imtihon.builder()
                .fan(fanRepo.findById(dto.fanId()).orElseThrow(() -> new NotFoundException("Fan topilmadi")))
                .nomi(dto.nomi())
                .davomiylikDaqiqa(dto.davomiylikDaqiqa())
                .maxUrinish(dto.maxUrinish())
                .savolSoni(dto.savolSoni())
                .aloPct(dto.aloPct())
                .yaxshiPct(dto.yaxshiPct())
                .qoniqarliPct(dto.qoniqarliPct())
                .tekshirishVaqti(dto.tekshirishVaqti())
                .tekshirishSoni(dto.tekshirishSoni())
                .build();

        return toDto(imtihonRepo.save(imtihon));
    }

    @Transactional
    public ImtihonResponseDto update(Long id, ImtihonUpdateDto dto) {
        Imtihon e = imtihonRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi"));

        if (dto.fanId() != null) e.setFan(fanRepo.getReferenceById(dto.fanId()));
        if (dto.nomi() != null) e.setNomi(dto.nomi());
        if (dto.davomiylikDaqiqa() != null) e.setDavomiylikDaqiqa(dto.davomiylikDaqiqa());
        if (dto.maxUrinish() != null) e.setMaxUrinish(dto.maxUrinish());
        if (dto.savolSoni() != null) e.setSavolSoni(dto.savolSoni());
        if (dto.aloPct() != null) e.setAloPct(dto.aloPct());
        if (dto.yaxshiPct() != null) e.setYaxshiPct(dto.yaxshiPct());
        if (dto.qoniqarliPct() != null) e.setQoniqarliPct(dto.qoniqarliPct());
        if (dto.tekshirishVaqti() != null) e.setTekshirishVaqti(dto.tekshirishVaqti());
        if (dto.tekshirishSoni() != null) e.setTekshirishSoni(dto.tekshirishSoni());

        return toDto(imtihonRepo.save(e));
    }

    @Transactional(readOnly = true)
    public ImtihonResponseDto get(Long id) {
        return imtihonRepo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi"));
    }

    @Transactional(readOnly = true)
    public List<ImtihonResponseDto> list() {
        return imtihonRepo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public String delete(Long id) {
        if (!imtihonRepo.existsById(id)) throw new NotFoundException("Imtihon topilmadi");
        imtihonRepo.deleteById(id);
        return "Imtihon muvaffaqiyatli o'chirildi";
    }

    /**
     * Foydalanuvchilarga ruxsat berish (Append)
     */
    @Transactional
    public ImtihonResponseDto grantUsers(Long imtihonId, Set<Long> userIds) {
        Imtihon imtihon = imtihonRepo.findById(imtihonId)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi"));

        List<User> users = userRepo.findAllById(userIds);
        if (users.isEmpty()) {
            throw new NotFoundException("Tanlangan foydalanuvchilar topilmadi");
        }

        imtihon.getRuxsatEtilganlar().addAll(users);
        return toDto(imtihonRepo.save(imtihon));
    }

    /**
     * Ruxsatlarni to'liq almashtirish (Overwrite)
     */
    @Transactional
    public ImtihonResponseDto setUsers(Long imtihonId, Set<Long> userIds) {
        Imtihon imtihon = imtihonRepo.findById(imtihonId)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi"));

        Set<User> users = new HashSet<>(userRepo.findAllById(userIds));
        imtihon.setRuxsatEtilganlar(users);
        return toDto(imtihonRepo.save(imtihon));
    }

    @Transactional(readOnly = true)
    public List<ImtihonBriefDTO1> myExams(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));

        return imtihonRepo.findMyExamsBriefNative(user.getId()).stream()
                .map(this::toBriefDto)
                .toList();
    }

    // --- Mapperlar (Private) ---

    private ImtihonResponseDto toDto(Imtihon e) {
        return new ImtihonResponseDto(
                e.getId(),
                e.getNomi(),
                e.getDavomiylikDaqiqa(),
                e.getMaxUrinish(),
                e.getSavolSoni(),
                e.getAloPct(),
                e.getYaxshiPct(),
                e.getQoniqarliPct(),
                e.getTekshirishVaqti(),
                e.getTekshirishSoni(),
                e.getFan() != null ? e.getFan().getId() : null,
                e.getFan() != null ? e.getFan().getNomi() : null,
                e.getRuxsatEtilganlar() == null ? List.of()
                        : e.getRuxsatEtilganlar().stream()
                        .map(u -> new RuxsatUserDto(
                                u.getId(),
                                u.getLastName() + " " + u.getFirstName(), // Yangi User parametrlari
                                u.getUsername()))
                        .toList()
        );
    }

    private ImtihonBriefDTO1 toBriefDto(ImtihonBriefDTO e) {
        return new ImtihonBriefDTO1(
                e.getId(),
                e.getNomi(),
                e.getDavomiylikDaqiqa(),
                e.getMaxUrinish(),
                e.getSavolSoni(),
                e.getFanId(),
                e.getFanNomi(),
                e.getTekshirishSoni(),
                e.getTekshirishVaqti(),
                e.getQoniqarliPct(),
                e.getYaxshiPct(),
                e.getAloPct(),
                e.getAttemptsUsed(),
                e.getAttemptsLeft(),
                e.getCanStart(),
                e.getUrinishId()
        );
    }

    /**
     * Foydalanuvchilardan ruxsatni olib tashlash (Remove)
     */
    @Transactional
    public ImtihonResponseDto revokeUsers(Long imtihonId, Set<Long> userIds) {
        // 1. Imtihonni topamiz
        Imtihon imtihon = imtihonRepo.findById(imtihonId)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi"));

        // 2. O'chirilishi kerak bo'lgan foydalanuvchilarni bazadan topamiz
        List<User> usersToRemove = userRepo.findAllById(userIds);

        // 3. Imtihon ruxsatnomalaridan ushbu foydalanuvchilarni o'chiramiz
        // removeAll metodi Set ichidagi mos obyektlarni o'chiradi
        imtihon.getRuxsatEtilganlar().removeAll(usersToRemove);

        // 4. Saqlaymiz va yangilangan holatni qaytaramiz
        return toDto(imtihonRepo.save(imtihon));
    }
}