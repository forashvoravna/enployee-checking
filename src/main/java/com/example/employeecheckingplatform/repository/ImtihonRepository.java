package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.dto.imtihon.ImtihonBriefDTO;
import com.example.employeecheckingplatform.entity.Imtihon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImtihonRepository extends JpaRepository<Imtihon, Long> {

    @Query(value = """
            SELECT
              i.id                AS id,
              i.nomi              AS nomi,
              i.davomiylik_daqiqa AS davomiylikDaqiqa,
              i.max_urinish       AS maxUrinish,
              i.savol_soni        AS savolSoni,
              f.id                AS fanId,
              f.nomi              AS fanNomi,

              (
                SELECT u.id
                FROM urinish u
                WHERE u.imtihon_id = i.id
                  AND u.foydalanuvchi_id = :userId
                  AND u.holati = 'JARAYONDA'
                ORDER BY u.boshladi DESC
                LIMIT 1
              ) AS urinishId,

              COALESCE((
                SELECT COUNT(u1.id)
                FROM urinish u1
                WHERE u1.imtihon_id = i.id
                  AND u1.foydalanuvchi_id = :userId
                  AND u1.holati = 'YAKUNLANGAN'
              ), 0) AS attemptsUsed,

              GREATEST(
                i.max_urinish - COALESCE((
                  SELECT COUNT(u2.id)
                  FROM urinish u2
                  WHERE u2.imtihon_id = i.id
                    AND u2.foydalanuvchi_id = :userId
                    AND u2.holati = 'YAKUNLANGAN'
                ), 0),
                0
              ) AS attemptsLeft,

              CASE
                WHEN EXISTS (
                  SELECT 1 FROM urinish u3
                  WHERE u3.imtihon_id = i.id
                    AND u3.foydalanuvchi_id = :userId
                    AND u3.holati = 'JARAYONDA'
                ) THEN FALSE
                WHEN (i.max_urinish - COALESCE((
                  SELECT COUNT(u4.id)
                  FROM urinish u4
                  WHERE u4.imtihon_id = i.id
                    AND u4.foydalanuvchi_id = :userId
                    AND u4.holati = 'YAKUNLANGAN'
                ), 0)) <= 0 THEN FALSE
                WHEN (
                  SELECT COUNT(sv.id)
                  FROM savol sv
                  WHERE sv.fan_id = i.fan_id
                ) < i.savol_soni THEN FALSE
                ELSE TRUE
              END AS canStart

            FROM imtihon i
            JOIN fan f ON f.id = i.fan_id
            WHERE i.faol = TRUE
              AND EXISTS (
                SELECT 1
                FROM imtihon_user iu
                WHERE iu.imtihon_id = i.id
                  AND iu.foydalanuvchi_id = :userId
              )
            """, nativeQuery = true)
    List<ImtihonBriefDTO> findMyExamsBriefNative(@Param("userId") Long userId);


}
