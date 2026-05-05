package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.dto.*;
import com.example.employeecheckingplatform.dto.vedemost.VedemostProjection;
import com.example.employeecheckingplatform.dto.javob.JavobDto;
import com.example.employeecheckingplatform.dto.projection.AnswerRowProjection;
import com.example.employeecheckingplatform.dto.projection.AttemptHeaderProjection;
import com.example.employeecheckingplatform.dto.savol.SavolPassDto;
import com.example.employeecheckingplatform.dto.urinish.UrinishDetailDto;
import com.example.employeecheckingplatform.dto.urinish.UrinishResponseDto;
import com.example.employeecheckingplatform.dto.variant.VariantPassDto;
import com.example.employeecheckingplatform.entity.*;
import com.example.employeecheckingplatform.exception.BusinessException;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrinishService {

    private final UrinishRepository urinishRepo;
    private final UserRepository userRepository;
    private final ImtihonRepository imtihonRepo;
    private final SavolRepository savolRepo;
    private final VariantRepository variantRepo;
    private final JavobRepository javobRepo;
    private final ObjectMapper objectMapper;
    private final UrinishSavolRepository urinishSavolRepository;

    // Status constants
    private static final String STATUS_IN_PROGRESS = "JARAYONDA";
    private static final String STATUS_FINISHED = "YAKUNLANGAN";

    // --- READ helpers ---
    @Transactional(readOnly = true)
    public List<UrinishResponseDto> list() {
        return urinishRepo.findAllWithUserAndExam().stream().map(UrinishService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UrinishResponseDto get(Long id) {
        return toResponse(urinishRepo.findDetailById(id)
                .orElseThrow(() -> new NotFoundException("Urinish topilmadi")));
    }

    // --- Unified start method ---

    /**
     * Start urinish; if questionsCount != null && > 0 then also pick that many questions randomly
     * and bind them to the created Urinish (UrinishSavol).
     * <p>
     * Returns created Urinish entity (saved).
     */
    @Transactional
    public UrinishResponseDto start(Long imtihonId, Principal principal) {
        String username = principal.getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        Imtihon imtihon = imtihonRepo.findById(imtihonId)
                .orElseThrow(() -> new NotFoundException("Imtihon topilmadi"));

        if (!imtihon.getRuxsatEtilganlar().isEmpty() &&
                imtihon.getRuxsatEtilganlar().stream().noneMatch(u -> u.getId().equals(user.getId()))) {
            throw new BusinessException("Sizga bu imtihon tayinlanmagan");
        }

        // Attempt limit (count only finished attempts)
        var finished = java.util.List.of(STATUS_FINISHED);
        int attempts = urinishRepo.countByImtihon_IdAndUser_IdAndHolatiIn(
                imtihonId, user.getId(), finished);
        if (attempts >= imtihon.getMaxUrinish()) {
            throw new BusinessException("Urinishlar limiti tugagan");
        }

        // Parallel in-progress check
        int inProgress = urinishRepo.countByImtihon_IdAndUser_IdAndHolati(
                imtihonId, user.getId(), STATUS_IN_PROGRESS);
        if (inProgress > 0) {
            throw new BusinessException("Sizda davom etayotgan urinish bor");
        }

        // Create Urinish
        Urinish u = Urinish.builder()
                .imtihon(imtihon)
                .user(userRepository.getReferenceById(user.getId()))
                .boshladi(Instant.now())
                .holati(STATUS_IN_PROGRESS)
                .ball(0)
                .build();
        u = urinishRepo.save(u);

        int questionsCount = Math.max(1, imtihon.getSavolSoni());
        assignRandomQuestionsToUrinish(u, questionsCount);
        return toResponse(u);
    }

    /**
     * Internal helper: chooses questionsCount many question IDs randomly from the exam's fan bank,
     * then loads Savol entities, and creates UrinishSavol entries (preserving chosen order).
     */
    private void assignRandomQuestionsToUrinish(Urinish u, int questionsCount) {
        Long fanId = u.getImtihon().getFan().getId();

        // 1️⃣ Bazadan random tanlash
        List<Long> randomIds = savolRepo.findRandomIdsByFanId(fanId, questionsCount);
        if (randomIds.isEmpty()) {
            throw new BusinessException("Savollar banki bo'sh");
        }

        // 2️⃣ Dublikatlarni olib tashlash
        LinkedHashSet<Long> uniq = new LinkedHashSet<>(randomIds);

        int activeCount = savolRepo.countActiveByFanId(fanId);
        int finalNeeded = Math.min(questionsCount, activeCount);

        if (uniq.size() < finalNeeded) {
            List<Long> allActive = savolRepo.findAllActiveIdsByFanId(fanId);
            allActive.removeAll(uniq);
            Collections.shuffle(allActive);
            for (Long id : allActive) {
                if (uniq.size() == finalNeeded) break;
                uniq.add(id);
            }
        }

        // 3️⃣ Shuffle qilish – bu joyda haqiqiy aralashtirish amalga oshadi
        List<Long> selectedIds = new ArrayList<>(uniq);
        Collections.shuffle(selectedIds); // bu muhim — orderni randomlashtiradi!

        // 4️⃣ Shu tartibda bazadan olish
        List<Savol> selectedSavollar = savolRepo.findAllByIdIn(selectedIds);
        Map<Long, Savol> map = selectedSavollar.stream()
                .collect(Collectors.toMap(Savol::getId, s -> s));

        List<UrinishSavol> toSave = new ArrayList<>();
        int pos = 0;
        for (Long sid : selectedIds) {
            Savol s = map.get(sid);
            if (s == null) continue;
            UrinishSavol us = UrinishSavol.builder()
                    .urinish(u)
                    .savol(s)
                    .position(pos++)
                    .build();

            List<Long> variantIds = s.getVariantlar().stream()
                    .map(Variant::getId)
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(variantIds);
            try {
                us.setVariantOrderJson(objectMapper.writeValueAsString(variantIds));
            } catch (Exception ignored) {}

            toSave.add(us);
        }

        urinishSavolRepository.saveAll(toSave);
    }



    @Transactional
    public Javob answer(Long urinishId, AnswerDto dto, User currentUser) {
        var u = urinishRepo.findById(urinishId)
                .orElseThrow(() -> new NotFoundException("Urinish topilmadi"));
        if (!STATUS_IN_PROGRESS.equals(u.getHolati()))
            throw new BusinessException("Urinish yopilgan");

        var s = savolRepo.findById(dto.savolId())
                .orElseThrow(() -> new NotFoundException("Savol topilmadi"));

        var imtihonFanId = u.getImtihon().getFan().getId();
        if (!s.getFan().getId().equals(imtihonFanId))
            throw new BusinessException("Savol bu imtihon faniga tegishli emas");

        var v = variantRepo.findById(dto.variantId())
                .orElseThrow(() -> new NotFoundException("Variant topilmadi"));

        if (!v.getSavol().getId().equals(s.getId()))
            throw new BusinessException("Variant savolga tegishli emas");

        javobRepo.upsertAnswer(urinishId, dto.savolId(), dto.variantId(), currentUser.getId());

        return javobRepo.findOneByUrinishIdAndSavolId(urinishId, dto.savolId());
    }


    @Transactional(readOnly = true)
    public Map getQuestionsForUrinish(Long urinishId) {

        Map data = new HashMap<>();

        List<UrinishSavol> list = urinishSavolRepository.findByUrinishIdOrderByPositionAsc(urinishId);

        List<SavolPassDto> result = new ArrayList<>(list.size());
        for (UrinishSavol us : list) {
            Savol s = us.getSavol();
            List<Variant> variants = s.getVariantlar();

            List<VariantPassDto> varDtos;
            if (us.getVariantOrderJson() != null && !us.getVariantOrderJson().isBlank()) {
                try {
                    List<Long> order = objectMapper.readValue(us.getVariantOrderJson(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
                    Map<Long, Variant> vmap = new HashMap<>();
                    for (Variant v : variants) vmap.put(v.getId(), v);
                    List<VariantPassDto> ordered = new ArrayList<>();
                    for (Long vid : order) {
                        Variant v = vmap.get(vid);
                        if (v != null) ordered.add(new VariantPassDto(v.getId(), v.getMatn()));
                    }
                    varDtos = ordered;
                } catch (Exception ex) {
                    varDtos = variants.stream().map(v -> new VariantPassDto(v.getId(), v.getMatn())).toList();
                }
            } else {
                varDtos = variants.stream().map(v -> new VariantPassDto(v.getId(), v.getMatn())).toList();
            }

            var mut = new ArrayList<>(varDtos);
            Collections.shuffle(mut);
            varDtos = mut;

            result.add(new SavolPassDto(s.getId(), s.getMatn(), varDtos));
        }
        Urinish urinish = urinishRepo.findById(urinishId).orElseThrow(() -> new BusinessException("Bunday urinish toopilmadi"));
        Imtihon imtihon = imtihonRepo.findById(urinish.getImtihon().getId()).orElseThrow(() -> new BusinessException("Bunday imtihon toopilmadi"));

        data.put("boshlanish vaqi", urinish.getBoshladi());
        data.put("davomiyligi", imtihon.getDavomiylikDaqiqa());
        data.put("questions", result);

        return data;
    }

    // --- delete ---
    @Transactional
    public void delete(Long id) {
        urinishRepo.deleteById(id);
    }

    public static UrinishResponseDto toResponse(Urinish e) {
        return new UrinishResponseDto(
                e.getId(),
                e.getImtihon() != null ? e.getImtihon().getId() : null,
                e.getImtihon() != null ? e.getImtihon().getNomi() : null,
                e.getUser() != null ? e.getUser().getId() : null,
                e.getUser() != null ? e.getUser().getUsername() : null,
                e.getBoshladi(),
                e.getTugadi(),
                e.getHolati(),
                e.getBall(),
                e.getBaho() != null ? e.getBaho().name() : null
        );
    }

    @Transactional
    public UrinishDetailDto finish(Long urinishId) {
        // 1) Javoblarni to‘g‘ri/ noto‘g‘ri bilan belgilab chiqamiz (native)
        javobRepo.markCorrectByUrinish(urinishId);

        // 2) Urinishni yakunlaymiz: ball, baho, tugadi, holati (native)
        urinishRepo.finishAttempt(urinishId);

        // 3) Header va Javoblar ro‘yxatini olib, DTO yasaymiz
        var h = urinishRepo.findHeader(urinishId);
        if (h == null) {
            throw new NotFoundException("Urinish topilmadi");
        }

        var rows = javobRepo.findAnswersForUrinish(urinishId);
        var javoblar = rows.stream()
                .map(r -> new JavobDto(
                        r.getSavolId(),
                        r.getSavolMatn(),
                        r.getVariantId(),
                        r.getVariantMatn(),
                        r.getTogri()
                ))
                .toList();

        return new UrinishDetailDto(
                h.getId(),
                h.getUserId(),
                h.getUserIsm(),
                h.getImtihonId(),
                h.getImtihonNomi(),
                h.getHolati(),
                h.getBall(),
                h.getSavolSoni(),
                h.getBoshlandi(),
                h.getTugadi(),
                javoblar,
                h.getBaho()
        );
    }

    private Long currentUserId(Principal principal) {
        var u = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        return u.getId();
    }

    /* --- 1) Foydalanuvchining barcha urinishlari (detail – javoblari bilan) --- */
    @Transactional(readOnly = true)
    public List<UrinishDetailDto> myResults(Principal principal) {
        Long userId = currentUserId(principal);

        var headers = urinishRepo.findAttemptHeaders(userId);
        if (headers.isEmpty()) return List.of();

        var attemptIds = headers.stream().map(AttemptHeaderProjection::getUrinishId).toList();

        var answersByAttempt = new HashMap<Long, List<JavobDto>>();
        if (!attemptIds.isEmpty()) {
            var rows = urinishRepo.findAnswersByAttemptIds(attemptIds);
            answersByAttempt.putAll(
                    rows.stream().collect(Collectors.groupingBy(
                            AnswerRowProjection::getUrinishId,
                            Collectors.mapping(r -> new JavobDto(
                                    r.getSavolId(),
                                    r.getSavolMatn(),
                                    r.getVariantId(),
                                    r.getVariantMatn(),
                                    r.getTogri()
                            ), Collectors.toList())
                    ))
            );
        }

        List<UrinishDetailDto> out = new ArrayList<>(headers.size());
        for (var h : headers) {
            out.add(new UrinishDetailDto(
                    h.getUrinishId(),
                    h.getUserId(),
                    h.getUserIsm(),
                    h.getImtihonId(),
                    h.getImtihonNomi(),
                    h.getHolati(),
                    h.getBall(),
                    h.getSavolSoni(),
                    h.getBoshlandi(),
                    h.getTugadi(),
                    answersByAttempt.getOrDefault(h.getUrinishId(), List.of()),
                    h.getBaho()
            ));
        }
        return out;
    }


}