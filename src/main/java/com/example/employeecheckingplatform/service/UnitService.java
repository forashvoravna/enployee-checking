package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.dto.OrgExamResultDto;
import com.example.employeecheckingplatform.dto.OrgExamSummaryDto;
import com.example.employeecheckingplatform.dto.projection.OrgExamParticipantProjection;
import com.example.employeecheckingplatform.dto.projection.OrgExamResultProjection;
import com.example.employeecheckingplatform.dto.projection.OrgResultProjection;
import com.example.employeecheckingplatform.dto.unit.UnitCreateDto;
import com.example.employeecheckingplatform.dto.unit.UnitResponseDto;
import com.example.employeecheckingplatform.dto.unit.UnitUpdateDto;
import com.example.employeecheckingplatform.entity.Unit;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository repo;

    @Transactional(readOnly = true)
    public List<UnitResponseDto> listRoots() {
        // Asosiy organlar (parent null bo'lganlar)
        return repo.findByParentIdIsNull().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UnitResponseDto> children(Long parentId) {
        // Bo'linma ichidagi bo'linmalar
        return repo.findAllByParentId(parentId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UnitResponseDto get(Long id) {
        return toDto(repo.findById(id).orElseThrow(() -> new NotFoundException("Bo'linma topilmadi")));
    }

    @Transactional
    public UnitResponseDto update(Long id, UnitUpdateDto dto) {
        Unit unit = repo.findById(id).orElseThrow(() -> new NotFoundException("Bo'linma topilmadi"));

        unit.setNomi(dto.nomi());
        unit.setBranchTypeId(dto.branchTypeId());

        if (dto.parentId() != null) {
            Unit parent = repo.findById(dto.parentId())
                    .orElseThrow(() -> new NotFoundException("Yangi yuqori bo'linma topilmadi"));
            unit.setParentId(parent.getId());
            unit.setLevel(parent.getLevel() + 1);
        } else {
            unit.setParentId(null);
            unit.setLevel(1);
        }

        return toDto(repo.save(unit));
    }

    @Transactional
    public String delete(Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Bo'linma topilmadi");
        repo.deleteById(id);
        return "Bo'linma muvaffaqiyatli o'chirildi";
    }

    @Transactional(readOnly = true)
    public List<UnitResponseDto> all() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    // --- Mapper metod ---
    public UnitResponseDto toDto(Unit entity) {
        if (entity == null) return null;
        return new UnitResponseDto(
                entity.getId(),
                entity.getNomi(),
                entity.getParentId() != null ? entity.getParentId() : null,
                entity.getBranchTypeId(),
                entity.getLevel()
        );
    }

    // --- Statistika va Proeksiyalar ---

    @Transactional(readOnly = true)
    public List<OrgResultProjection> getOrganizationResults(Long orgId) {
        return repo.findOrgResults(orgId);
    }

    @Transactional(readOnly = true)
    public List<OrgResultProjection> getOkrugResults() {
        return repo.findOkrugResults();
    }

    @Transactional(readOnly = true)
    public OrgExamSummaryDto examsOfOrg(Long orgId) {
        List<OrgExamResultProjection> rows = repo.findOrgExamResults(orgId);

        List<OrgExamResultDto> items = rows.stream()
                .map(r -> new OrgExamResultDto(
                        r.getImtihonId(),
                        r.getImtihonNomi(),
                        r.getSavolSoni(),
                        r.getFanId(),
                        r.getFanNomi(),
                        r.getAttempts(),
                        r.getAlo(),
                        r.getYaxshi(),
                        r.getQoniqarli(),
                        r.getQoniqarsiz(),
                        r.getDistinctUsers()
                ))
                .toList();

        return new OrgExamSummaryDto(orgId, items.size(), items);
    }

    @Transactional(readOnly = true)
    public List<OrgExamParticipantProjection> findParticipants(Long orgId, Long examId) {
        return repo.findParticipantsByOrgAndExam(orgId, examId);
    }

    @Transactional(readOnly = true)
    public List<UnitResponseDto> turi(Long branchTypeId) {
        // Berilgan turdagi barcha bo'linmalarni olish (masalan, barcha tumanlar yoki barcha bo'limlar)
        return repo.findAllByBranchTypeId(branchTypeId).stream()
                .map(this::toDto)
                .toList();
    }
}