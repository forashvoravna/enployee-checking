package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.entity.ShootingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShootingResultRepository extends JpaRepository<ShootingResult, Long> {

    // 1. Barchasini teskari tartibda (eng yangisi birinchi)
    List<ShootingResult> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // 2. Eng yaxshi natija (Score bo'yicha eng kattasi, agar ballar teng bo'lsa yangirog'i)
    Optional<ShootingResult> findFirstByUserIdOrderByScoreDescCreatedAtDesc(Long userId);

    // Har bir user uchun eng yuqori ballni va o'sha qatorni to'liq qaytaradi
    @Query(value = """
        SELECT DISTINCT ON (user_id) *
        FROM shooting_results
        ORDER BY user_id, score DESC
        """, nativeQuery = true)
    List<ShootingResult> findAllBestResultsForEachUser();

    Optional<ShootingResult> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}