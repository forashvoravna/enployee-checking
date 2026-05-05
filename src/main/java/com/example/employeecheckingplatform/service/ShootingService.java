package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.dto.ShootingResultDto;
import com.example.employeecheckingplatform.entity.ShootingResult;
import com.example.employeecheckingplatform.entity.User;
import com.example.employeecheckingplatform.exception.NotFoundException;
import com.example.employeecheckingplatform.repository.ShootingResultRepository;
import com.example.employeecheckingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShootingService {

    private final ShootingResultRepository shootingRepo;
    private final UserRepository userRepo;

    @Transactional
    public void saveResults(List<ShootingResultDto> dtoList) {
        for (ShootingResultDto dto : dtoList) {
            // 1. Odam topilmasa Exception qaytarish
            User user = userRepo.findByUsername(dto.jshshir())
                    .orElseThrow(() -> new NotFoundException("Xodim topilmadi: JSHSHIR " + dto.jshshir()));

            ShootingResult result = new ShootingResult();
            result.setUser(user);
            result.setWeaponName(dto.weaponName());
            result.setShots(dto.shots());
            result.setHits(dto.hits());
            result.setScore(dto.score());
            shootingRepo.save(result);
        }
    }

    // Barcha natijalarni teskari tartibda olish
    public List<ShootingResult> getAllByUserId(Long userId) {
        return shootingRepo.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    // Eng yaxshi natijani olish (score bo'yicha eng yuqorisi)
    public ShootingResult getBestResultByUserId(Long userId) {
        return shootingRepo.findFirstByUserIdOrderByScoreDescCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("Ushbu xodimda hali natijalar mavjud emas"));
    }
    public List<ShootingResult> getBestResultsForAllUsers() {
        return shootingRepo.findAllBestResultsForEachUser();
    }

}