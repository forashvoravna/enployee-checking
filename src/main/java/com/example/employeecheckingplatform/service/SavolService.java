package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.dto.savol.SavolCreateDto;
import com.example.employeecheckingplatform.dto.savol.SavolResponseDto;
import com.example.employeecheckingplatform.dto.savol.SavolUpdateDto;
import com.example.employeecheckingplatform.dto.variant.VariantCreateDto;
import com.example.employeecheckingplatform.dto.variant.VariantDto;
import com.example.employeecheckingplatform.dto.variant.VariantResponseDto;
import com.example.employeecheckingplatform.entity.*;
import com.example.employeecheckingplatform.exception.BusinessException;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavolService {
    private final SavolRepository savolRepo;
    private final VariantRepository variantRepo;
    private final FanRepository fanRepo;

    @Transactional
    public SavolResponseDto create(SavolCreateDto dto) {
        var fan = fanRepo.findById(dto.fanId()).orElseThrow(() -> new NotFoundException("Fan topilmadi"));
        var s = Savol.builder().fan(fan).matn(dto.matn()).build();
        s = savolRepo.save(s);

        long trueCount = dto.variantlar().stream().filter(VariantCreateDto::togri).count();
        if (trueCount != 1) throw new BusinessException("Variantlarda aniq 1 ta to‘g‘ri bo‘lishi shart");

        for (var vdto : dto.variantlar()) {
            variantRepo.save(Variant.builder()
                    .savol(s)
                    .matn(vdto.matn())
                    .togri(vdto.togri())
                    .build());
        }
        return toDto(s);
    }

    @Transactional(readOnly = true)
    public List<SavolResponseDto> listByFan(Long fanId) {
        return savolRepo.findAllByFan_Id(fanId).stream().map(SavolService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SavolResponseDto get(Long id) {
        return toDto(savolRepo.findById(id).orElseThrow(() -> new NotFoundException("Savol topilmadi")));
    }

    @Transactional
    public SavolResponseDto updateOnlyExistingVariants(Long savolId, SavolUpdateDto dto) {
        // 1) Savolni va mavjud variantlarni yuklash
        Savol s = savolRepo.findById(savolId)
                .orElseThrow(() -> new NotFoundException("Savol topilmadi"));

        if (dto.matn() != null) s.setMatn(dto.matn());

        var existing = variantRepo.findBySavol_Id(savolId);
        if (existing.isEmpty()) throw new BusinessException("Savol uchun variantlar topilmadi");

        var byId = existing.stream().collect(Collectors.toMap(Variant::getId, v -> v));

        if (dto.variantlar() == null || dto.variantlar().isEmpty())
            throw new BusinessException("Kamida bitta variantni yuboring");

        var dtoIds = dto.variantlar().stream().map(VariantDto::id).collect(Collectors.toSet());
        var existingIds = byId.keySet();

        // Talabga mos qat’iy tekshiruv: DTO’dagi id seti == mavjud id seti
        if (!dtoIds.equals(existingIds)) {
            throw new BusinessException("Variantlar to‘plami mos emas: faqat mavjud variantlarni id bo‘yicha yuboring, yangi qo‘shish yoki o‘chirish mumkin emas");
        }

        // 3) Yangilash (faqat mavjudlarini)
        for (VariantDto variant : dto.variantlar()) {
            Variant v = byId.get(variant.id());
            if (v == null) {
                throw new BusinessException("Variant savolga tegishli emas: id=" + variant.id());
            }
            v.setMatn(variant.matn());
            v.setTogri(Boolean.TRUE.equals(variant.togri()));
        }

        // 4) Validatsiya: faqat bitta to‘g‘ri javob bo‘lsin (yangilangandan keyin tekshiramiz)
        long correct = existing.stream().filter(Variant::getTogri).count();
        if (correct != 1) {
            throw new BusinessException("Variantlar ichida faqat bitta 'togri=true' bo‘lishi kerak");
        }

        // 5) Saqlash
        variantRepo.saveAll(existing); // yoki cascade bo‘lsa, s’ni saqlash ham yetadi
        return  toDto(savolRepo.save(s));
    }


    @Transactional
    public String delete(Long id) {
        savolRepo.deleteById(id);
        return "Savol  muvaffaqiyatli o'chirildi";
    }

    public static SavolResponseDto toDto(Savol entity) {
        return new SavolResponseDto(
                entity.getId(),
                entity.getMatn(),
                entity.getFan() != null ? entity.getFan().getId() : null,
                entity.getFan() != null ? entity.getFan().getNomi() : null,
                entity.getVariantlar() == null ? List.of() :
                        entity.getVariantlar().stream()
                                .map(v -> new VariantResponseDto(v.getId(), v.getMatn(), v.getTogri()))
                                .toList()
        );
    }

}
