package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.dto.vedemost.VedemostProjection;
import com.example.employeecheckingplatform.dto.otishga.LastGradeProjection;
import com.example.employeecheckingplatform.dto.projection.AnswerRowProjection;
import com.example.employeecheckingplatform.dto.projection.AttemptHeaderProjection;
import com.example.employeecheckingplatform.dto.urinish.UrinishHeaderRow;
import com.example.employeecheckingplatform.entity.Urinish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UrinishRepository extends JpaRepository<Urinish, Long> {

    // 1. countBy... metodlarida foydalanuvchi maydoni entityda 'user' deb nomlangan bo'lsa:
    int countByImtihon_IdAndUser_IdAndHolatiIn(Long imtihonId, Long userId, Collection<String> holatlar);

    int countByImtihon_IdAndUser_IdAndHolati(Long imtihonId, Long userId, String holat);

    // 2. SQL querylarda 'users' jadvaliga va 'user_id' ustuniga murojaat qilish kerak
    @Query(value = """
                SELECT u.*
                  FROM urinish u
                  JOIN users f ON f.id = u.user_id
                  JOIN imtihon i       ON i.id = u.imtihon_id
                 ORDER BY u.boshladi DESC
            """, nativeQuery = true)
    List<Urinish> findAllWithUserAndExam();

    @Query(value = """
                SELECT u.*
                  FROM urinish u
                  JOIN imtihon i       ON i.id = u.imtihon_id
                  JOIN users f ON f.id = u.user_id
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

    // foydalanuvchi_id o'rniga user_id, toliq_ism o'rniga first_name/last_name ishlatildi
    @Query(value = """
                SELECT u.id                    AS id,
                       f.id                    AS userId,
                       CONCAT(f.last_name, ' ', f.first_name) AS userIsm,
                       i.id                    AS imtihonId,
                       i.nomi                  AS imtihonNomi,
                       i.savol_soni            AS savolSoni,
                       u.holati                AS holati,
                       u.ball                  AS ball,
                       u.boshladi              AS boshlandi,
                       u.tugadi                AS tugadi,
                       u.baho                  AS baho
                  FROM urinish u
                  JOIN users f ON f.id = u.user_id
                  JOIN imtihon i       ON i.id = u.imtihon_id
                 WHERE u.id = :urinishId
            """, nativeQuery = true)
    UrinishHeaderRow findHeader(@Param("urinishId") Long urinishId);

    @Query(value = """
        SELECT
          u.id                 AS urinishId,
          f.id                 AS userId,
          CONCAT(f.last_name, ' ', f.first_name) AS userIsm,
          i.id                 AS imtihonId,
          i.nomi               AS imtihonNomi,
          i.savol_soni         AS savolSoni,
          u.holati             AS holati,
          u.ball               AS ball,
          u.boshladi           AS boshlandi,
          u.tugadi             AS tugadi,
          u.baho               AS baho
        FROM urinish u
        JOIN imtihon i       ON i.id = u.imtihon_id
        JOIN users f ON f.id = u.user_id
        WHERE u.user_id = :userId
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

    @Query(value = """
        SELECT
          u.id        AS urinishId,
          i.id        AS imtihonId,
          i.nomi      AS imtihonNomi,
          u.ball      AS ball,
          u.baho      AS baho,
          u.tugadi    AS tugadi
        FROM urinish u
        JOIN imtihon i ON i.id = u.imtihon_id
        WHERE u.user_id = :userId
          AND u.holati = 'YAKUNLANGAN'
        ORDER BY u.tugadi DESC NULLS LAST
        LIMIT 1
        """, nativeQuery = true)
    LastGradeProjection findLastFinishedByUser(@Param("userId") Long userId);


}