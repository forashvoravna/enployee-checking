package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.config.FaceBridgeClient;
import com.example.employeecheckingplatform.dto.BranchDto;
import com.example.employeecheckingplatform.dto.PersonRawDto;
import com.example.employeecheckingplatform.dto.ShootingGradeDto;
import com.example.employeecheckingplatform.dto.otishga.FacePersonGradeAndShootResponse;
import com.example.employeecheckingplatform.dto.otishga.FacePersonGradeResponse;
import com.example.employeecheckingplatform.dto.otishga.GradeDto;
import com.example.employeecheckingplatform.dto.otishga.LastGradeProjection;
import com.example.employeecheckingplatform.dto.user.Person;
import com.example.employeecheckingplatform.dto.user.PersonForFaceResult;
import com.example.employeecheckingplatform.entity.Unit;
import com.example.employeecheckingplatform.entity.User;
import com.example.employeecheckingplatform.entity.UserRole;
import com.example.employeecheckingplatform.exception.BusinessException;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.ShootingResultRepository;
import com.example.employeecheckingplatform.repository.UnitRepository;
import com.example.employeecheckingplatform.repository.UrinishRepository;
import com.example.employeecheckingplatform.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FaceResultService {

    private final FaceBridgeClient bridge;
    private final UserRepository userRepo;
    private final UrinishRepository urinishRepo;
    private final PasswordEncoder passwordEncoder;
    private final ShootingResultRepository shootingRepo;

    @Transactional
    public ResponseEntity<?> recognizeAndGetLastGrade(String base64Image) throws JsonProcessingException {
        // 1) Python backend’ga rasm yuboramiz → PersonDto
        PersonForFaceResult person = bridge.sendAndReturnPerson(base64Image);
        if (person == null) {
            return ResponseEntity.ok("Qushmiyyalaar");
        }
        String username = person.getJshshir();   // jshshirni username sifatida ishlatyapmiz
        // 2) Foydalanuvchini topamiz yoki yaratamiz
        User user = userRepo.findByUsername(username)
                .orElseGet(() -> createUserFromPython(person));

        // 3) Shu foydalanuvchining oxirgi yakunlangan urinishini olamiz
        LastGradeProjection p = urinishRepo.findLastFinishedByUser(user.getId());

        GradeDto lastGrade = null;
        if (p != null) {
            lastGrade = new GradeDto(
                    p.getUrinishId(),
                    p.getImtihonId(),
                    p.getImtihonNomi(),
                    p.getBall(),
                    p.getBaho(),
                    p.getTugadi()
            );
        }

        // 4) Person ma’lumoti + bahoni qaytaramiz
        return ResponseEntity.ok(new FacePersonGradeResponse(person, lastGrade));
    }

    @Transactional
    public ResponseEntity<?> recognizeAndGetLastGradeAndShoot(String base64Image) throws JsonProcessingException {
        // 1) Python backend’ga rasm yuboramiz → PersonDto
        PersonForFaceResult person = bridge.sendAndReturnPerson(base64Image);
        if (person == null) {
            return ResponseEntity.ok("Qushmiyyalaar");
        }
        String username = person.getJshshir();   // jshshirni username sifatida ishlatyapmiz
        // 2) Foydalanuvchini topamiz yoki yaratamiz
        User user = userRepo.findByUsername(username)
                .orElseGet(() -> createUserFromPython(person));

        // 3) Shu foydalanuvchining oxirgi yakunlangan urinishini olamiz
        LastGradeProjection p = urinishRepo.findLastFinishedByUser(user.getId());

        GradeDto lastGrade = null;
        if (p != null) {
            lastGrade = new GradeDto(
                    p.getUrinishId(),
                    p.getImtihonId(),
                    p.getImtihonNomi(),
                    p.getBall(),
                    p.getBaho(),
                    p.getTugadi()
            );
        }

        ShootingGradeDto lastShooting = shootingRepo.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .map(s -> new ShootingGradeDto(
                        s.getId(),
                        s.getWeaponName(),
                        s.getShots(),
                        s.getHits(),
                        s.getScore()
                )).orElse(null);

        // 4) Person ma’lumoti + bahoni qaytaramiz
        return ResponseEntity.ok(new FacePersonGradeAndShootResponse(person, lastGrade, lastShooting));
    }

    /**
     * Python AI'dan kelgan ma'lumotlar asosida bazada foydalanuvchi yaratish
     */
    private User createUserFromPython(PersonForFaceResult dto) {
        // 1. Yangi User obyekti ochamiz
        User user = new User();

        user.setUsername(dto.getJshshir()); // JSHSHIR login sifatida
        user.setFirstName(dto.getFirst_name());
        user.setLastName(dto.getLast_name());
        user.setMiddleName(dto.getMiddle_name() != null ? dto.getMiddle_name() : " ");
        user.setJshshir(dto.getJshshir());
        user.setGender(dto.getGender());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setUserRole(UserRole.USER);
        user.setBranchId(dto.getBranch_id());
        user.setRankId(dto.getRank_id());
        user.setDeleted(false);

        return userRepo.save(user);
    }

    @Transactional
    public ResponseEntity<?> recognizeByJshshir(String jshshir) {

        PersonForFaceResult person = bridge.getPersonRawByJshshir(jshshir);
        return handlePerson(person);
    }

    private ResponseEntity<?> handlePerson(PersonForFaceResult person) {
        // 1. Foydalanuvchini bazadan topish yoki yaratish
        User user = userRepo.findByUsername(person.getJshshir())
                .orElseGet(() -> createUserFromPython(person));

        // 2. Oxirgi test natijasini olish (Mavjud logika)
        LastGradeProjection p = urinishRepo.findLastFinishedByUser(user.getId());
        GradeDto lastGrade = (p == null) ? null :
                new GradeDto(
                        p.getUrinishId(), p.getImtihonId(), p.getImtihonNomi(),
                        p.getBall(), p.getBaho(), p.getTugadi()
                );


        // 4. Hammasini bitta javobda qaytarish
        return ResponseEntity.ok(
                new FacePersonGradeResponse(person, lastGrade)
        );
    }

    @Transactional
    public ResponseEntity<?> recognizeByJshshirWithShoot(String jshshir) {

        PersonForFaceResult person = bridge.getPersonRawByJshshir(jshshir);
        return handlePersonWithShoots(person);
    }

    private ResponseEntity<?> handlePersonWithShoots(PersonForFaceResult person) {
        // 1. Foydalanuvchini bazadan topish yoki yaratish
        User user = userRepo.findByUsername(person.getJshshir())
                .orElseGet(() -> createUserFromPython(person));

        // 2. Oxirgi test natijasini olish (Mavjud logika)
        LastGradeProjection p = urinishRepo.findLastFinishedByUser(user.getId());
        GradeDto lastGrade = (p == null) ? null :
                new GradeDto(
                        p.getUrinishId(), p.getImtihonId(), p.getImtihonNomi(),
                        p.getBall(), p.getBaho(), p.getTugadi()
                );

        // 3. Oxirgi o'q otish natijasini olish (Yangi logika)
        // findFirstByUserIdOrderByCreatedAtDesc metodidan foydalanamiz
        ShootingGradeDto lastShooting = shootingRepo.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .map(s -> new ShootingGradeDto(
                        s.getId(),
                        s.getWeaponName(),
                        s.getShots(),
                        s.getHits(),
                        s.getScore()
                )).orElse(null);

        // 4. Hammasini bitta javobda qaytarish
        return ResponseEntity.ok(
                new FacePersonGradeAndShootResponse(person, lastGrade, lastShooting)
        );
    }
}