package com.example.employeecheckingplatform.component;


import com.example.employeecheckingplatform.config.PythonAuthClient;
import com.example.employeecheckingplatform.dto.TokenDTO;
import com.example.employeecheckingplatform.dto.rank.AddRankDTO;
import com.example.employeecheckingplatform.dto.unit.UnitDTO;
import com.example.employeecheckingplatform.dto.unit.UnitTypeDTO;
import com.example.employeecheckingplatform.dto.user.Person;
import com.example.employeecheckingplatform.entity.*;
import com.example.employeecheckingplatform.repository.RankRepository;
import com.example.employeecheckingplatform.repository.UnitRepository;
import com.example.employeecheckingplatform.repository.UnitTypeRepository;
import com.example.employeecheckingplatform.repository.UserRepository;
import com.example.employeecheckingplatform.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RankRepository rankRepository;
    private final UnitRepository unitRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final RestTemplate restTemplate;
    private final PythonAuthClient pythonAuthClient;
    private final AttachmentService attachmentService;



    @Value("${app.face.upload.url1}")
    private String ip;

    @Value("${spring.sql.init.mode}")
    private String initialModeType;

    @Override
    public void run(String... args) {

        if (initialModeType.equals("always")) {
            String token = pythonAuthClient.getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            generateRank(entity);
            generatedBranchType(entity);
            generatedBranch(entity);
            generatedUser();
            syncPersonsFromApi(entity);
        }

    }

    private void generatedBranchType(HttpEntity<String> entity) {

        if (!unitTypeRepository.findAllByDeleted(false).isEmpty()) return;

        String url = ip + "/api/v1/branch-types/";
        ResponseEntity<List<UnitTypeDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        List<UnitTypeDTO> unitTypeDTOS = response.getBody();
        if (unitTypeDTOS != null) {
            List<UnitType> result = unitTypeDTOS
                    .stream().map(unit -> {
                        UnitType unitType = new UnitType();
                        unitType.setNomi(unit.getNomi());
                        unitType.setId(unit.getId());
                        unitType.setDarajasi(unit.getDarajasi());
                        return unitType;
                    }).toList();
            unitTypeRepository.saveAll(result);
        }
    }
    private void generatedUser() {

        if (!userRepository.findAllByDeletedFalse().isEmpty()) return;

        User superAdmin = new User();

        superAdmin.setUsername("admin");
        superAdmin.setPassword(passwordEncoder.encode("123456"));
        superAdmin.setUserRole(UserRole.ADMIN);
        superAdmin.setFirstName("Eshmat");
        superAdmin.setLastName("Eshmatovich");
        superAdmin.setMiddleName("Eshmat o'g'li");
        superAdmin.setGender("mujik");
        superAdmin.setJshshir("12345678912345");
        superAdmin.setBranchId(1L);
        superAdmin.setRankId(1L);
        superAdmin.setId(-1L);

        userRepository.save(superAdmin);

        superAdmin.setUsername("test-user-1");
        superAdmin.setPassword(passwordEncoder.encode("JWT_PASS_456"));
        superAdmin.setUserRole(UserRole.ADMIN);
        superAdmin.setFirstName("test-user-1");
        superAdmin.setLastName("test-user-1");
        superAdmin.setMiddleName("test-user-1");
        superAdmin.setGender("erkak");
        superAdmin.setJshshir("12345678901234");
        superAdmin.setBranchId(1L);
        superAdmin.setRankId(1L);
        superAdmin.setId(-2L);

        userRepository.save(superAdmin);



    }
    private void generateRank(HttpEntity<String> entity) {

        if (!rankRepository.findAll().isEmpty()) return;
        String url = ip + "/api/v1/ranks/";
        ResponseEntity<List<AddRankDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        List<AddRankDTO> ranks = response.getBody();
        if (ranks != null) {
            List<Rank> result = ranks
                    .stream().map(rank -> {
                        Rank rank1 = new Rank();
                        rank1.setId(rank.getId());
                        rank1.setNomi(rank.getNomi());
                        rank1.setQisqartmasi(rank.getQisqartmasi());
                        rank1.setDarajasi(rank.getDarajasi());
                        return rank1;
                    }).toList();
            rankRepository.saveAll(result);
        }
    }

    private void syncPersonsFromApi(HttpEntity<String> entity) {
        String url = ip + "/api/v1/persons/";

        // 1. Tashqi API'dan ma'lumotlarni olish
        ResponseEntity<List<Person>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
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
                    user.setAttachmentId(attachmentService.saveImageBase64(dto.getAvatar() != null ? dto.getAvatar() : "rasm"));
                    user.setPassword(passwordEncoder.encode("123456")); // Default parol
                    user.setUserRole(UserRole.USER);

                    return user;
                })
                .toList();

        // 4. Bazaga ommaviy saqlash
        if (!newUsers.isEmpty()) {
            userRepository.saveAll(newUsers);
        }
    }

    private void generatedBranch(HttpEntity<String> entity){

        if (!unitRepository.findAllByDeleted(false).isEmpty()) return;
        String url = ip + "/api/v1/branches/";
        ResponseEntity<List<UnitDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        List<UnitDTO> units = response.getBody();
        if (units != null) {
            List<Unit> result = units
                    .stream().map(unit -> {
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
            unitRepository.saveAll(result);
        }
    }

}
