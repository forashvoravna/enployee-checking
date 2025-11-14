package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.dto.RuxsatFoydalanuvchiDto;
import com.example.employeecheckingplatform.dto.imtihon.*;
import com.example.employeecheckingplatform.entity.Imtihon;
import com.example.employeecheckingplatform.entity.Tashkilot;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.FanRepository;
import com.example.employeecheckingplatform.repository.FoydalanuvchiRepository;
import com.example.employeecheckingplatform.repository.ImtihonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImtihonService {

    private final ImtihonRepository imtihonRepo;
    private final FanRepository fanRepo;
    private final FoydalanuvchiRepository foydalanuvchiRepo;

    @Transactional
    public ImtihonResponseDto create(ImtihonCreateDto dto) {
        var imtihon = Imtihon.builder()
                .fan(fanRepo.getReferenceById(dto.fanId()))
                .nomi(dto.nomi())
                .davomiylikDaqiqa(dto.davomiylikDaqiqa())
                .maxUrinish(dto.maxUrinish())
                .savolSoni(dto.savolSoni())
                .aloPct(dto.aloPct())
                .yaxshiPct(dto.yaxshiPct())
                .qoniqarliPct(dto.qoniqarliPct())
                .faol(true)
                .build();
        return ImtihonService.toDto(imtihonRepo.save(imtihon));
    }

    @Transactional
    public ImtihonResponseDto update(Long id, ImtihonUpdateDto dto) {
        var e = imtihonRepo.findById(id).orElseThrow(() -> new NotFoundException("Imtihon topilmadi"));

        if (dto.fanId() != null) e.setFan(fanRepo.getReferenceById(dto.fanId()));
        if (dto.nomi() != null) e.setNomi(dto.nomi());
        if (dto.davomiylikDaqiqa() != null) e.setDavomiylikDaqiqa(dto.davomiylikDaqiqa());
        if (dto.maxUrinish() != null) e.setMaxUrinish(dto.maxUrinish());
        if (dto.savolSoni() != null) e.setSavolSoni(dto.savolSoni());
        if (dto.aloPct() != null) e.setAloPct(dto.aloPct());
        if (dto.yaxshiPct() != null) e.setYaxshiPct(dto.yaxshiPct());
        if (dto.qoniqarliPct() != null) e.setQoniqarliPct(dto.qoniqarliPct());
        if (dto.faol() != null) e.setFaol(dto.faol());
        return ImtihonService.toDto(imtihonRepo.save(e));
    }

    @Transactional(readOnly = true)
    public ImtihonResponseDto get(Long id) {
        return imtihonRepo.findById(id)
                .map(ImtihonService::toDto)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi"));
    }

    @Transactional(readOnly = true)
    public List<ImtihonResponseDto> list() {
        return ImtihonService.toDtoList(imtihonRepo.findAll());
    }

    @Transactional
    public String delete(Long id) {
        if (!imtihonRepo.existsById(id)) throw new NotFoundException("Imtihon topilmadi");
        imtihonRepo.deleteById(id);
        return "Imtihon muvafaqiyatli o'chirildi";
    }

    private static ImtihonResponseDto toDto(Imtihon e) {
        return new ImtihonResponseDto(
                e.getId(),
                e.getNomi(),
                e.getDavomiylikDaqiqa(),
                e.getMaxUrinish(),
                e.getSavolSoni(),
                e.getAloPct(),
                e.getYaxshiPct(),
                e.getQoniqarliPct(),
                e.getFaol(),
                e.getFan() != null ? e.getFan().getId() : null,
                e.getFan() != null ? e.getFan().getNomi() : null,
                e.getRuxsatEtilganlar() == null ? List.of()
                        : e.getRuxsatEtilganlar().stream()
                        .map(u -> new RuxsatFoydalanuvchiDto(u.getId(), u.getToliqIsm(), u.getUsername()))
                        .toList()
        );
    }

    public static List<ImtihonResponseDto> toDtoList(List<Imtihon> list) {
        return list.stream().map(ImtihonService::toDto).toList();
    }

    /**
     * Ruxsat etilgan foydalanuvchilarni qo'shish (mavjud setga qo‘shadi, overwrite emas)
     */
    @Transactional
    public ImtihonResponseDto grantUsers(Long imtihonId, java.util.Set<Long> userIds) {
        Imtihon imtihon = imtihonRepo.findById(imtihonId)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi"));

        var users = new java.util.HashSet<>(foydalanuvchiRepo.findAllById(userIds));
        if (users.isEmpty()) {
            throw new NotFoundException("Berilgan foydalanuvchi ID’laridan hech biri topilmadi");
        }

        // append semantics
        imtihon.getRuxsatEtilganlar().addAll(users);

        return toDto(imtihonRepo.save(imtihon));
    }

    /**
     * Ixtiyoriy: ruxsatlar to‘plamini to‘liq almashtirish (overwrite)
     */
    @Transactional
    public ImtihonResponseDto setUsers(Long imtihonId, java.util.Set<Long> userIds) {
        Imtihon imtihon = imtihonRepo.findById(imtihonId)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi"));
        var users = new java.util.HashSet<>(foydalanuvchiRepo.findAllById(userIds));
        imtihon.setRuxsatEtilganlar(users);
        return toDto(imtihonRepo.save(imtihon));
    }

    /**
     * Ixtiyoriy: ayrim foydalanuvchilarni ruxsatdan olib tashlash
     */
    @Transactional
    public ImtihonResponseDto revokeUsers(Long imtihonId, java.util.Set<Long> userIds) {
        Imtihon imtihon = imtihonRepo.findById(imtihonId)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi"));
        imtihon.getRuxsatEtilganlar().removeIf(u -> userIds.contains(u.getId()));
        return toDto(imtihonRepo.save(imtihon));
    }

    @Transactional(readOnly = true)
    public List<ImtihonBriefDTO1> myExams(String username) {
        var user = foydalanuvchiRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        return imtihonRepo.findMyExamsBriefNative(user.getId()).stream().map(ImtihonService::toDto).toList();
    }

    private static ImtihonBriefDTO1 toDto(ImtihonBriefDTO e) {
        return new ImtihonBriefDTO1(
                e.getId(),
                e.getNomi(),
                e.getDavomiylikDaqiqa(),
                e.getMaxUrinish(),
                e.getSavolSoni(),
                e.getFanId(),
                e.getFanNomi(),
                e.getAttemptsUsed(),
                e.getAttemptsLeft(),
                e.getCanStart(),
                e.getUrinishId()
        );
    }
}
