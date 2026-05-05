package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.config.PythonAuthClient;
import com.example.employeecheckingplatform.dto.ApiResponseForFace;
import com.example.employeecheckingplatform.dto.user.*;
import com.example.employeecheckingplatform.entity.User;
import com.example.employeecheckingplatform.entity.UserRole;
import com.example.employeecheckingplatform.exception.BusinessException;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService; // DTO mapping mantiqi uchun (getCurrentUserDetail)
    private final PythonAuthClient pythonAuthClient;
    private final UserRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final AttachmentService attachmentService;


    @Value("${app.face.upload.url1}")
    private String ip;

    @Transactional
    public UserResponseDTO create(UserCreateDto dto) {
        // Username va JSHSHIR bandligini tekshirish
        if (repo.existsByUsername(dto.username())) {
            throw new BusinessException("Ushbu username band");
        }
        if (repo.existsByJshshir(dto.jshshir())) {
            throw new BusinessException("Ushbu JSHSHIR bilan foydalanuvchi allaqachon mavjud");
        }

        User user = new User();
        Long minId = repo.findMinId().orElse(0L);
        user.setId(minId > 0 ? -1L : minId - 1L);
        
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setMiddleName(dto.middleName() != null ? dto.middleName() : " ");
        user.setGender(dto.gender());
        user.setJshshir(dto.jshshir());
        user.setUserRole(dto.userRole());
        user.setBranchId(dto.branchId());
        user.setRankId(dto.rankId());
        user.setDeleted(false); // Yangi foydalanuvchi o'chirilmagan holatda bo'ladi

        User saved = repo.save(user);
        return authService.getCurrentUserDetail(saved);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO get(Long id) {
        // Faqat o'chirilmagan foydalanuvchini topish
        User user = repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi yoki o'chirilgan"));
        return authService.getCurrentUserDetail(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> list() {
        // Soft delete qilinganlarni filtrlab olish
        return repo.findAllByDeletedFalse().stream()
                .map(authService::getCurrentUserDetail)
                .toList();
    }

    @Transactional
    public UserResponseDTO update(Long id, UserUpdateDto dto) {
        User user = repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Yangilanayotgan foydalanuvchi topilmadi"));

        // Agar username o'zgarayotgan bo'lsa, yangisi band emasligini tekshirish
        if (!user.getUsername().equals(dto.username()) && repo.existsByUsername(dto.username())) {
            throw new BusinessException("Bu username allaqachon band");
        }

        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setMiddleName(dto.middleName() != null ?  dto.middleName() : " ");
        user.setUsername(dto.username());
        user.setJshshir(dto.jshshir());
        user.setUserRole(dto.userRole());
        user.setBranchId(dto.branchId());
        user.setRankId(dto.rankId());
        user.setGender(dto.gender());

        User savedUser = repo.save(user);
        return authService.getCurrentUserDetail(savedUser);
    }

    @Transactional
    public String setPassword(Long id, String newPassword) {
        User user = repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));

        user.setPassword(passwordEncoder.encode(newPassword));
        repo.save(user);
        return "Parol muvaffaqiyatli o'zgartirildi";
    }

    @Transactional
    public String delete(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));

        // Soft delete - ma'lumotni saqlab qolgan holda o'chirilgan deb belgilash
        user.setDeleted(true);
        repo.save(user);
        return "Foydalanuvchi muvaffaqiyatli o'chirildi";
    }

    public ResponseEntity<?> verifyUserByImage(MultipartFile request) throws JsonProcessingException {

        String base64 = convertImageToBase63(request);
        if (base64 == null) {
            return ResponseEntity.badRequest().body("Xatolik, qaytadan urinib ko'ring!!!");
        }
        FaceDTO faceDTO = new FaceDTO(base64, "Y", UUID.randomUUID().toString());
        String token = pythonAuthClient.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON); // SHUNI QO'SHING

        HttpEntity<FaceDTO> entity = new HttpEntity<>(faceDTO, headers);
        RestTemplate restTemplate = new RestTemplate();
        String url = ip + "/api/v1/faceid/recognize";
        ResponseEntity<String> result = restTemplate.postForEntity(url, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(result.getBody());
        if (root == null) return ResponseEntity.badRequest().body("Xatolik, qaytadan urinib ko'ring !!!");
        String status = root.get("status").asText();
        String message = root.get("message").asText();
        boolean success = root.get("success").asBoolean();

        if (success) {
            JsonNode data = root.get("data");
            JsonNode person = data.get("person");
            if (person.isMissingNode()) {
                return ResponseEntity.badRequest().body("Person ma'lumotlari topilmadi!");
            }
            String jshshir = person.hasNonNull("jshshir") ? person.get("jshshir").asText() : null;
            Person res = new Person();
            res.setId(person.get("id").asLong(0L));
            res.setFirst_name(person.hasNonNull("first_name") ? person.get("first_name").asText() : null);
            res.setLast_name(person.hasNonNull("last_name") ? person.get("last_name").asText() : null);
            res.setMiddle_name(person.hasNonNull("middle_name") ? person.get("middle_name").asText() : null);
            res.setGender(person.hasNonNull("gender") ? person.get("gender").asText() : null);
            res.setJshshir(jshshir);
            res.setBranch_id(person.get("branch_id").asLong(0L));
            res.setBranch_nomi(person.hasNonNull("branch_nomi") ? person.get("branch_nomi").asText() : null);
            res.setBranch_path(person.hasNonNull("branch_path") ? person.get("branch_path").asText() : null);
            res.setRank_id(person.get("rank_id").asLong(0L));
            res.setRank_nomi(person.hasNonNull("rank_nomi") ? person.get("rank_nomi").asText() : null);
            res.setAvatar(person.hasNonNull("avatar") ? person.get("avatar").asText() : null);

            User personUser = repository.findByJshshirAndDeleted(jshshir, false).orElse(null);
            if (personUser == null) {
                User newUser = new User();
                newUser.setId(res.getId());
                newUser.setUsername(jshshir);
                newUser.setPassword(encoder.encode("123"));
                newUser.setUserRole(UserRole.USER);
                newUser.setFirstName(res.getFirst_name());
                newUser.setLastName(res.getLast_name());
                newUser.setMiddleName(res.getMiddle_name() != null ? res.getMiddle_name() : " ");
                newUser.setGender(res.getGender());
                newUser.setJshshir(jshshir);
                newUser.setBranchId(res.getBranch_id());
                newUser.setRankId(res.getRank_id());
                newUser.setAttachmentId(attachmentService.saveImageBase64(res.getAvatar()));
                User user1 = repository.save(newUser);
                return ResponseEntity.ok(user1);
            } else if (personUser.getAttachmentId() == null) {
                personUser.setAttachmentId(attachmentService.saveImageBase64(res.getAvatar()));
                repository.save(personUser);
            }
            return ResponseEntity.ok(personUser);
        } else {
            if (status.equalsIgnoreCase("LOW_CONFIDENCE"))
                return ResponseEntity.ok(". Rasmni sifatli olib, qaytadan urinib ko'ring!!!");
            if (status.equalsIgnoreCase("NOT_FOUND")) return ResponseEntity.ok(status);
            return ResponseEntity.ok(message);
        }
    }


    public ResponseEntity<?> addPersonWithImageTwo(User user, UserAddWithImage dto) {
        if (UserRole.ADMIN != user.getUserRole()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Foydalanuvchi roli mos emas!!!");
        }

        if (dto.getBranch_id() == null || dto.getBranch_id() <= 0 || dto.getFirst_name() == null ||
                dto.getJshshir() == null || dto.getJshshir().length() != 14 || dto.getAvatar() == null) {
            return ResponseEntity.badRequest().body("Kerakli maydonlarni to'ldiring yoki JSHSHIR xato!!!");
        }

        String face_image_base64 = dto.getAvatar();
        if (face_image_base64.contains(",")) {
            face_image_base64 = face_image_base64.split(",")[1];
        }

        UserResponseWithImage payload = new UserResponseWithImage(
                dto.getBranch_id(),
                face_image_base64,
                dto.getFirst_name(),
                dto.getGender() != null ? dto.getGender().toLowerCase() : "male",
                dto.getJshshir(),
                dto.getLast_name(),
                dto.getMiddle_name() != null ? dto.getMiddle_name(): " ",
                dto.getRank_id()
        );

        try {
            String token = pythonAuthClient.getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(token);

            HttpEntity<UserResponseWithImage> entity = new HttpEntity<>(payload, headers);

            RestTemplate restTemplate = new RestTemplate();
            String url = ip + "/api/v1/persons";

            ResponseEntity<ApiResponseForFace> result = restTemplate.postForEntity(url, entity, ApiResponseForFace.class);
            ApiResponseForFace apiResponse = result.getBody();

            if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                User pythonUser = mapper.convertValue(apiResponse.getData(), User.class);

                User person = new User();
                if (repository.existsByJshshir(dto.getJshshir())) {
                    return ResponseEntity.badRequest().body("Ushbu JSHSHIR bazada mavjud!");
                }

                person.setUsername(dto.getJshshir()); // JSHSHIR login bo'ladi
                person.setPassword(encoder.encode("123456")); // Standart parol
                person.setUserRole(UserRole.USER);
                person.setFirstName(dto.getFirst_name());
                person.setLastName(dto.getLast_name());
                person.setMiddleName(dto.getMiddle_name() != null ? dto.getMiddle_name() : " ");
                person.setGender(dto.getGender());
                person.setJshshir(dto.getJshshir());
                person.setBranchId(dto.getBranch_id());
                person.setRankId(dto.getRank_id());
                person.setId(pythonUser.getId()); // Python-dagi ID-ni saqlab qo'yish
                person.setDeleted(false);

                User savedUser = repository.save(person);
                return ResponseEntity.ok(savedUser);

            } else {
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Python API-dan bo'sh javob qaytdi";
                return ResponseEntity.badRequest().body(msg);
            }

        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Python API xatosi: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Tizimda kutilmagan xato: " + e.getMessage());
        }
    }

    private String convertImageToBase63(MultipartFile request) {
        try {
            byte[] bytes = request.getBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            return null;
        }
    }
}