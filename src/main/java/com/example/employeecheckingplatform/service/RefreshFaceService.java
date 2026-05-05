package com.example.employeecheckingplatform.service;


import com.example.employeecheckingplatform.config.PythonAuthClient;
import com.example.employeecheckingplatform.dto.rank.AddRankDTO;
import com.example.employeecheckingplatform.dto.unit.UnitDTO;
import com.example.employeecheckingplatform.dto.unit.UnitTypeDTO;
import com.example.employeecheckingplatform.dto.user.Person;
import com.example.employeecheckingplatform.entity.*;
import com.example.employeecheckingplatform.repository.RankRepository;
import com.example.employeecheckingplatform.repository.UnitRepository;
import com.example.employeecheckingplatform.repository.UnitTypeRepository;
import com.example.employeecheckingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefreshFaceService {

    private final RankRepository rankRepository;
    private final UnitRepository unitRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final RestTemplate restTemplate;
    private final PythonAuthClient authClient;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttachmentService attachmentService;



    @Value("${app.face.upload.url1}")
    private String ip;

    public ResponseEntity<?> refreshFace(User user) {
        if (user.getUserRole() == UserRole.USER)
            return ResponseEntity.ok( "Foydalanuvchi roli mos emas !!!");

        try {
            String token = authClient.getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            generateRank(entity);
            generatedBranchType(entity);
            generatedBranch(entity);
            syncPersonsFromApi(entity);
        } catch (Exception e) {
            return ResponseEntity.ok(e.getMessage());
        }
        return ResponseEntity.ok("Yangilanish muvaffaqiyatli amalga oshirildi");
    }

    private void generatedBranchType(HttpEntity<String> entity) {

        String url = ip + "/api/v1/branch-types/";
        ResponseEntity<List<UnitTypeDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        List<UnitTypeDTO> unitTypeDTOS = response.getBody();
        if (unitTypeDTOS == null || unitTypeDTOS.isEmpty()) return;

        Set<Long> existingOriginalIds = unitTypeRepository
                .findAllByDeleted(false)
                .stream()
                .map(UnitType::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<UnitType> result = unitTypeDTOS.stream()
                .filter(dto -> dto.getId() != null)
                .filter(dto -> !existingOriginalIds.contains(dto.getId()))
                .map(dto -> {
                    UnitType unitType = new UnitType();
                    unitType.setNomi(dto.getNomi());
                    unitType.setId(dto.getId());
                    unitType.setDarajasi(dto.getDarajasi());
                    unitType.setDeleted(false);
                    unitType.setCreatedBy(-1L);
                    unitType.setUpdatedBy(-1L);
                    return unitType;
                })
                .toList();

        if (!result.isEmpty()) {
            unitTypeRepository.saveAll(result);
        }
    }

    private void generateRank(HttpEntity<String> entity) {


        String url = ip + "/api/v1/ranks/";
        ResponseEntity<List<AddRankDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        List<AddRankDTO> ranks = response.getBody();
        if (ranks == null || ranks.isEmpty()) return;

        Set<Long> existingOriginalIds = rankRepository.findAllByDeleted(false).stream()
                .map(Rank::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Rank> result = ranks.stream()
                .filter(rank -> rank.getId() != null)
                .filter(rank -> !existingOriginalIds.contains(rank.getId()))
                .map(rank -> {
                    Rank rank1 = new Rank();
                    rank1.setId(rank.getId());
                    rank1.setNomi(rank.getNomi());
                    rank1.setQisqartmasi(rank.getQisqartmasi());
                    rank1.setDarajasi(rank.getDarajasi());
                    rank1.setDeleted(false);
                    rank1.setCreatedBy(-1L);
                    rank1.setUpdatedBy(-1L);
                    return rank1;
                }).toList();

        if (!result.isEmpty()) {
            rankRepository.saveAll(result);
        }
    }

    private void generatedBranch(HttpEntity<String> entity) {

        String url = ip + "/api/v1/branches/";
        ResponseEntity<List<UnitDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        List<UnitDTO> units = response.getBody();
        if (units == null || units.isEmpty()) return;

        Set<Long> existingOriginalIds = unitRepository
                .findAllByDeleted(false)
                .stream()
                .map(Unit::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Unit> result = units
                .stream()
                .filter(unit -> unit.getId() != null)
                .filter(unit -> !existingOriginalIds.contains(unit.getId()))
                .map(unit -> {
                    Unit unit1 = new Unit();
                    unit1.setNomi(unit.getNomi());
                    unit1.setBranchTypeId(unit.getBranch_type_id());
                    unit1.setParentId(unit.getParent_id());
                    unit1.setBoshliqPersonId(unit.getBoshliq_person_id());
                    unit1.setId(unit.getId());
                    unit1.setPath(unit.getPath());
                    unit1.setLevel(unit.getLevel());
                    unit1.setDeleted(false);
                    unit1.setCreatedBy(-1L);
                    unit1.setUpdatedBy(-1L);
                    return unit1;
                }).toList();

        if (!result.isEmpty()) {
            unitRepository.saveAll(result);
        }
    }
    private void syncPersonsFromApi(HttpEntity<String> entity) {
        String url = ip + "/api/v1/persons/";

        // 1. Tashqi API'dan ma'lumotlarni olish
        ResponseEntity<List<Person>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Person>>() {}
        );

        List<Person> personDtos = response.getBody();
        if (personDtos == null || personDtos.isEmpty()) return;

        // 2. Bazadagi mavjud JSHSHIR'larni yig'ib olamiz (Dublikat bo'lmasligi uchun)
        Set<String> existingJshshirs = userRepository.findAll()
                .stream()
                .map(User::getJshshir)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. Yangi xodimlarni filtrlash va Entity'ga o'girish
        List<User> newUsers = personDtos.stream()
                .filter(dto -> dto.getJshshir() != null)
                .filter(dto -> !existingJshshirs.contains(dto.getJshshir())) // Faqat bazada yo'qlari
                .map(dto -> {
                    User user = new User();
                    user.setFirstName(dto.getFirst_name());
                    user.setLastName(dto.getLast_name());
                    user.setMiddleName(dto.getMiddle_name() != null ? dto.getMiddle_name() : " ");
                    user.setGender(dto.getGender());
                    user.setJshshir(dto.getJshshir());
                    user.setUsername(dto.getJshshir()); // JSHSHIR login sifatida
                    user.setBranchId(dto.getBranch_id() != null ? dto.getBranch_id() : null);
                    user.setRankId(dto.getRank_id() != null ? dto.getRank_id() : null);
                    user.setId(dto.getId() != null ? dto.getId() : null);
                    user.setAttachmentId(attachmentService.saveImageBase64(dto.getAvatar()));

                    // Majburiy xavfsizlik sozlamalari
                    user.setPassword(passwordEncoder.encode("123456")); // Default parol
                    user.setUserRole(UserRole.USER);
                    user.setDeleted(false);

                    return user;
                })
                .toList();

        // 4. Bazaga ommaviy saqlash
        if (!newUsers.isEmpty()) {
            userRepository.saveAll(newUsers);
        }
    }

}
