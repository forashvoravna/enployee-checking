package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.dto.vedemost.VedemostProjection;
import com.example.employeecheckingplatform.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query(value = "SELECT MIN(id) FROM users", nativeQuery = true)
    Optional<Long> findMinId();

    Boolean existsByUsername(String username);

    boolean existsByJshshir(@NotBlank(message = "JSHSHIR kiritilishi shart") @Pattern(regexp = "\\d{14}", message = "JSHSHIR 14 ta raqamdan iborat bo'lishi kerak") String jshshir);

    Optional<User> findByIdAndDeletedFalse(Long id);

    List<User> findAllByDeletedFalse();

    Optional<User> findByJshshirAndDeleted(String jshshir, boolean b);

    @Query(value = """

            SELECT DISTINCT ON (u.id, ur.imtihon_id)
    u.id as userId,
    u.first_name || ' ' || u.last_name as fullname,
    un.nomi as unitName,
    r.nomi as rankName,
    ur.ball as ball,
    ur.baho as baho,
    ur.created_at as sana,
    t.nomi as testNomi,
    t.savol_soni as savolSoni,
    ur.ball || '/' || t.savol_soni as natijaStat,
    t.alo_pct as aloPct,
    t.yaxshi_pct as yaxshiPct,
    t.qoniqarli_pct as qoniqarliPct
FROM urinish ur
JOIN users u ON ur.user_id = u.id
JOIN unit un ON u.branch_id = un.id
JOIN rank r ON u.rank_id = r.id
JOIN imtihon t ON ur.imtihon_id = t.id
WHERE ur.holati = 'YAKUNLANGAN'
  AND ur.created_at BETWEEN :startDate AND :endDate
  AND (:isAllUnits = true OR u.branch_id IN (:unitIds))
  AND (:isAllTests = true OR ur.imtihon_id IN (:testIds))
ORDER BY u.id, ur.imtihon_id, ur.ball DESC, ur.created_at ASC
""", nativeQuery = true)
    List<VedemostProjection> findVedemostData(
            @Param("unitIds") List<Long> unitIds,
            @Param("isAllUnits") boolean isAllUnits,
            @Param("testIds") List<Long> testIds,
            @Param("isAllTests") boolean isAllTests,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
