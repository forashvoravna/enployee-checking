package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.dto.fan.FanCreateDto;
import com.example.employeecheckingplatform.dto.fan.FanResponseDto;
import com.example.employeecheckingplatform.dto.fan.FanUpdateDto;
import com.example.employeecheckingplatform.entity.Fan;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.FanRepository;
import com.example.employeecheckingplatform.repository.SavolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FanService {
    private final FanRepository repo;
    private final SavolRepository savolRepository;

    @Transactional
    public FanResponseDto create(FanCreateDto dto) {
        var f = Fan.builder().nomi(dto.nomi()).tavsif(dto.tavsif()).faol(dto.faol() != null ? dto.faol() : true).build();
        return toDto(repo.save(f));
    }

    @Transactional(readOnly = true)
    public List<FanResponseDto> faol() {
        return repo.findAllByFaolIs(true).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<FanResponseDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public FanResponseDto get(Long id) {
        return toDto(repo.findById(id).orElseThrow(() -> new NotFoundException("Fan topilmadi")));
    }

    @Transactional
    public FanResponseDto update(Long id, FanUpdateDto dto) {
        var f = repo.findById(id).orElseThrow(() -> new NotFoundException("Fan topilmadi"));
        f.setNomi(dto.nomi());
        f.setTavsif(dto.tavsif());
        f.setFaol(dto.faol());
        return toDto(f);
    }

    @Transactional
    public String delete(Long id) {
        repo.deleteById(id);
        return "Fan holati muvaffaqiyatli yangilandi";
    }


    public FanResponseDto toDto(Fan fan) {
        Long savolSoni = savolRepository.countByFan_Id(fan.getId());
        return new FanResponseDto(
                fan.getId(),
                fan.getNomi(),
                fan.getTavsif(),
                fan.getFaol(),
                savolSoni
        );
    }
}
