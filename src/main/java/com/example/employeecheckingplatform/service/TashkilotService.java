package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.dto.OrgExamResultDto;
import com.example.employeecheckingplatform.dto.OrgExamSummaryDto;
import com.example.employeecheckingplatform.dto.projection.OrgExamParticipantProjection;
import com.example.employeecheckingplatform.dto.projection.OrgExamResultProjection;
import com.example.employeecheckingplatform.dto.projection.OrgResultProjection;
import com.example.employeecheckingplatform.dto.tashkilot.TashkilotCreateDto;
import com.example.employeecheckingplatform.dto.tashkilot.TashkilotResponseDto;
import com.example.employeecheckingplatform.dto.tashkilot.TashkilotUpdateDto;
import com.example.employeecheckingplatform.entity.Tashkilot;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.TashkilotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TashkilotService {
    private final TashkilotRepository repo;

    @Transactional
    public TashkilotResponseDto create(TashkilotCreateDto dto) {
        Tashkilot parent = (dto.parentId() != null) ? repo.findById(dto.parentId())
                .orElseThrow(() -> new NotFoundException("Parent topilmadi")) : null;
        var t = Tashkilot.builder()
                .nomi(dto.nomi())
                .turi(dto.turi())
                .parent(parent)
                .faol(true)
                .build();
        return toDto(repo.save(t));
    }

    @Transactional(readOnly = true)
    public List<TashkilotResponseDto> listRoots() {
        return repo.findByParentIsNullAndFaolTrue().stream().map(TashkilotService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<TashkilotResponseDto> children(Long parentId) {
        return repo.findByParent_Id(parentId).stream().map(TashkilotService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<TashkilotResponseDto> turi(String turi) {
        return repo.findByTuri(turi).stream().map(TashkilotService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TashkilotResponseDto get(Long id) {
        return toDto(repo.findById(id).orElseThrow(() -> new NotFoundException("Topilmadi")));
    }

    @Transactional
    public TashkilotResponseDto update(Long id, TashkilotUpdateDto dto) {
        var t = repo.findById(id).orElseThrow(() -> new NotFoundException("Topilmadi"));
        t.setNomi(dto.nomi());
        t.setTuri(dto.turi());
        t.setFaol(true);
        Tashkilot parent = (dto.parentId() != null) ? repo.findById(dto.parentId())
                .orElseThrow(() -> new NotFoundException("Parent topilmadi")) : null;
        t.setParent(parent);
        return toDto(repo.save(t));
    }

    @Transactional
    public String delete(Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Tashkilot topilmadi");
        repo.deleteById(id);
        return "Tashkilot muvafaqiyatli o'chirildi";
    }

    public List<TashkilotResponseDto> all() {
        return repo.findAll().stream().map(TashkilotService::toDto).toList();
    }


    public static TashkilotResponseDto toDto(Tashkilot entity) {
        if (entity == null) return null;

        return new TashkilotResponseDto(
                entity.getId(),
                entity.getNomi(),
                entity.getParent() != null ? entity.getParent().getId() : null,
                entity.getParent() != null ? entity.getParent().getNomi() : null,
                entity.getTuri() != null ? entity.getTuri() : null // agar enum bo‘lsa
        );
    }

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
                        r.getFanId(),
                        r.getFanNomi(),
                        r.getAttempts(),
                        r.getAvgScore(),
                        r.getAlo(),
                        r.getYaxshi(),
                        r.getQoniqarli(),
                        r.getQoniqarsiz(),
                        r.getDistinctUsers(),
                        r.getPassRate()
                ))
                .toList();

        return new OrgExamSummaryDto(
                orgId,
                items.size(),         // nechta imtihon
                items
        );
    }

    @Transactional(readOnly = true)
    public List<OrgExamParticipantProjection> findParticipants(Long orgId, Long examId) {
        return repo.findParticipantsByOrgAndExam(orgId, examId);
    }
}
