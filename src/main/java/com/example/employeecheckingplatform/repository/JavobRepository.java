package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.dto.javob.JavobRow;
import com.example.employeecheckingplatform.entity.Javob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JavobRepository extends JpaRepository<Javob, Long> {
    Javob findByUrinish_IdAndSavol_Id(Long urinishId, Long savolId);
    List<Javob> findByUrinish_Id(Long urinishId);

    @Modifying
    @Query(value = """
        UPDATE javob j
           SET togri = v.togri
          FROM variant v
         WHERE j.variant_id = v.id
           AND j.urinish_id = :urinishId
    """, nativeQuery = true)
    int markCorrectByUrinish(@Param("urinishId") Long urinishId);

    @Query(value = """
        SELECT s.id   AS savolId,
               s.matn AS savolMatn,
               v.id   AS variantId,
               v.matn AS variantMatn,
               j.togri AS togri
          FROM javob j
          JOIN savol s   ON s.id = j.savol_id
          JOIN variant v ON v.id = j.variant_id
         WHERE j.urinish_id = :urinishId
         ORDER BY s.id, v.id
    """, nativeQuery = true)
    List<JavobRow> findAnswersForUrinish(@Param("urinishId") Long urinishId);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO javob(urinish_id, savol_id, variant_id, togri)
        VALUES (:urinishId, :savolId, :variantId, NULL)
        ON CONFLICT (urinish_id, savol_id)
        DO UPDATE SET
            variant_id = EXCLUDED.variant_id,
            togri = NULL
        """, nativeQuery = true)
    int upsertAnswer(@Param("urinishId") Long urinishId,
                     @Param("savolId") Long savolId,
                     @Param("variantId") Long variantId);

    // Qayta o‘qish uchun bitta aniq yozuvni olamiz
    @Query(value = """
        SELECT *
        FROM javob
        WHERE urinish_id = :urinishId
          AND savol_id   = :savolId
        """, nativeQuery = true)
    Javob findOneByUrinishIdAndSavolId(@Param("urinishId") Long urinishId,
                                       @Param("savolId") Long savolId);

}