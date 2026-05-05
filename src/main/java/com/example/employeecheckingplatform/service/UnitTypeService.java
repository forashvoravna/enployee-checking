package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.dto.OrgExamResultDto;
import com.example.employeecheckingplatform.dto.OrgExamSummaryDto;
import com.example.employeecheckingplatform.dto.projection.OrgExamParticipantProjection;
import com.example.employeecheckingplatform.dto.projection.OrgExamResultProjection;
import com.example.employeecheckingplatform.dto.projection.OrgResultProjection;
import com.example.employeecheckingplatform.dto.unit.*;
import com.example.employeecheckingplatform.entity.Unit;
import com.example.employeecheckingplatform.entity.UnitType;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.UnitRepository;
import com.example.employeecheckingplatform.repository.UnitTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitTypeService {

    private final UnitTypeRepository repo;


    @Transactional(readOnly = true)
    public UnitTypeResponseDto get(Long id) {
        return toDto(repo.findById(id).orElseThrow(() -> new NotFoundException("Bo'linma topilmadi")));
    }

    @Transactional
    public String delete(Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Bo'linma topilmadi");
        repo.deleteById(id);
        return "Bo'linma muvaffaqiyatli o'chirildi";
    }

    @Transactional(readOnly = true)
    public List<UnitTypeResponseDto> all() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    // --- Mapper metod ---
    public UnitTypeResponseDto toDto(UnitType entity) {
        if (entity == null) return null;
        return new UnitTypeResponseDto(
                entity.getId(),
                entity.getNomi(),
                entity.getDarajasi()
        );
    }


}