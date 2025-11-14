package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.dto.projection.AnswerRowProjection;
import com.example.employeecheckingplatform.dto.projection.AttemptHeaderProjection;
import com.example.employeecheckingplatform.dto.urinish.UrinishHeaderRow;
import com.example.employeecheckingplatform.entity.Urinish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UrinishRepository extends JpaRepository<Urinish, Long> {

    int countByImtihon_IdAndFoydalanuvchi_IdAndHolatiIn(Long imtihonId, Long foydalanuvchiId, Collection<String> holatlar);

    int countByImtihon_IdAndFoydalanuvchi_IdAndHolati(Long imtihonId, Long foydalanuvchiId, String holat);

    @Query(value = """
                SELECT u.*
                  FROM urinish u
                  JOIN foydalanuvchi f ON f.id = u.foydalanuvchi_id
                  JOIN imtihon i       ON i.id = u.imtihon_id
                 ORDER BY u.boshladi DESC
            """, nativeQuery = true)
    List<Urinish> findAllWithUserAndExam();

    @Query(value = """
                SELECT u.*
                  FROM urinish u
                  JOIN imtihon i       ON i.id = u.imtihon_id
                  JOIN foydalanuvchi f ON f.id = u.foydalanuvchi_id
                 WHERE u.id = :id
                LIMIT 1
            """, nativeQuery = true)
    Optional<Urinish> findDetailById(@Param("id") Long id);

    @Modifying
    @Query(value = """
                UPDATE urinish u
                SET
                  ball   = COALESCE(c.correct_count, 0),
                  tugadi = NOW(),
                  holati = 'YAKUNLANGAN',
                  baho   = CASE
                             WHEN COALESCE(s.selected_count, 0) = 0 THEN 'QONIQARSIZ'
                             WHEN (COALESCE(c.correct_count, 0) * 100) >= (s.selected_count * i.alo_pct) THEN 'ALO'
                             WHEN (COALESCE(c.correct_count, 0) * 100) >= (s.selected_count * i.yaxshi_pct) THEN 'YAXSHI'
                             WHEN (COALESCE(c.correct_count, 0) * 100) >= (s.selected_count * i.qoniqarli_pct) THEN 'QONIQARLI'
                             ELSE 'QONIQARSIZ'
                           END
                FROM imtihon i
                LEFT JOIN (
                  SELECT j.urinish_id,
                         SUM(CASE WHEN j.togri THEN 1 ELSE 0 END) AS correct_count
                  FROM javob j
                  WHERE j.urinish_id = :urinishId
                  GROUP BY j.urinish_id
                ) c ON TRUE
                LEFT JOIN (
                  SELECT us.urinish_id,
                         COUNT(*) AS selected_count
                  FROM urinish_savol us
                  WHERE us.urinish_id = :urinishId
                  GROUP BY us.urinish_id
                ) s ON TRUE
                WHERE
                  u.id = :urinishId
                  AND i.id = u.imtihon_id
                  AND u.holati = 'JARAYONDA'
                  AND (c.urinish_id IS NULL OR c.urinish_id = u.id)
                  AND (s.urinish_id IS NULL OR s.urinish_id = u.id);
                
            """, nativeQuery = true)
    int finishAttempt(@Param("urinishId") Long urinishId);

    @Query(value = """
                SELECT u.id                    AS id,
                       f.id                    AS foydalanuvchiId,
                       f.toliq_ism             AS foydalanuvchiIsm,
                       i.id                    AS imtihonId,
                       i.nomi                  AS imtihonNomi,
                       u.holati                AS holati,
                       u.ball                  AS ball,
                       u.boshladi              AS boshlandi,
                       u.tugadi                AS tugadi,
                       u.baho                  AS baho
                  FROM urinish u
                  JOIN foydalanuvchi f ON f.id = u.foydalanuvchi_id
                  JOIN imtihon i       ON i.id = u.imtihon_id
                 WHERE u.id = :urinishId
            """, nativeQuery = true)
    UrinishHeaderRow findHeader(@Param("urinishId") Long urinishId);

    @Query(value = """
        SELECT
          u.id                 AS urinishId,
          f.id                 AS foydalanuvchiId,
          f.toliq_ism          AS foydalanuvchiIsm,
          i.id                 AS imtihonId,
          i.nomi               AS imtihonNomi,
          u.holati             AS holati,
          u.ball               AS ball,
          u.boshladi           AS boshlandi,
          u.tugadi             AS tugadi,
          u.baho               AS baho
        FROM urinish u
        JOIN imtihon i       ON i.id = u.imtihon_id
        JOIN foydalanuvchi f ON f.id = u.foydalanuvchi_id
        WHERE u.foydalanuvchi_id = :userId
        ORDER BY u.boshladi DESC
        """, nativeQuery = true)
    List<AttemptHeaderProjection> findAttemptHeaders(@Param("userId") Long userId);

    @Query(value = """
        SELECT
          u.id     AS urinishId,
          s.id     AS savolId,
          s.matn   AS savolMatn,
          v.id     AS variantId,
          v.matn   AS variantMatn,
          j.togri  AS togri
        FROM urinish u
        JOIN javob   j ON j.urinish_id = u.id
        JOIN savol   s ON s.id = j.savol_id
        JOIN variant v ON v.id = j.variant_id
        WHERE u.id IN (:attemptIds)
        ORDER BY u.id, s.id
        """, nativeQuery = true)
    List<AnswerRowProjection> findAnswersByAttemptIds(@Param("attemptIds") Collection<Long> attemptIds);

}
