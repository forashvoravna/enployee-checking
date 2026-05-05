package com.example.employeecheckingplatform.repository;


import com.example.employeecheckingplatform.dto.projection.OrgExamParticipantProjection;
import com.example.employeecheckingplatform.dto.projection.OrgExamResultProjection;
import com.example.employeecheckingplatform.dto.projection.OrgResultProjection;
import com.example.employeecheckingplatform.dto.unit.UnitListDTO;
import com.example.employeecheckingplatform.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    Optional<Unit> findByIdAndDeleted(Long id, boolean deleted);
    List<Unit> findAllByDeleted(boolean deleted);

    @Query(value = """
                  WITH RECURSIVE sections AS (
                 	SELECT id, nomi, parent_id FROM	unit WHERE id =?1 and deleted = false
                 	UNION
                 	SELECT	u.id, u.nomi, u.parent_id FROM unit u
                 	INNER JOIN sections s ON s.id = u.parent_id and deleted= false
                 )
                 select
                 u.id,
                 u.nomi as nomi
                 from unit u
                 join sections s on s.id = u.id
            """, nativeQuery = true)
    List<UnitListDTO> getAllUnitWithUnitAndSon(Long id);

    List<Unit> findByParentIdIsNull();

    List<Unit> findAllByParentId(Long parentId);

    @Query(value = """
            SELECT
                f.branch_id AS tashkilotId,
                t.nomi AS tashkilotNomi,
                COUNT(u.id) AS jamiUrinish,
                COUNT(CASE WHEN u.baho = 'ALO' THEN 1 END) AS aloSoni,
                COUNT(CASE WHEN u.baho = 'YAXSHI' THEN 1 END) AS yaxshiSoni,
                COUNT(CASE WHEN u.baho = 'QONIQARLI' THEN 1 END) AS qoniqarliSoni,
                COUNT(CASE WHEN u.baho = 'QONIQARSIZ' THEN 1 END) AS qoniqarsizSoni
            FROM urinish u
            JOIN users f ON f.id = u.user_id
            JOIN unit t ON t.id = f.branch_id
            WHERE t.id  = :orgId
              AND u.holati = 'YAKUNLANGAN'
            GROUP BY f.branch_id, t.nomi
            ORDER BY t.nomi
            """, nativeQuery = true)
    List<OrgResultProjection> findOrgResults(@Param("orgId") Long orgId);

    @Query(value = """
    WITH RECURSIVE okrug_tree AS (
        SELECT u.id AS okrug_id,
               u.id AS node_id,
               u.nomi AS okrug_nomi
        FROM unit u
        WHERE u.parent_id = 1
    
        UNION ALL
        SELECT ot.okrug_id,
               u.id AS node_id,
               ot.okrug_nomi
        FROM unit u
        JOIN okrug_tree ot ON u.parent_id = ot.node_id
    )
    SELECT
        ot.okrug_id                           AS unitId,
        ot.okrug_nomi                         AS unitNomi,
        COUNT(ur.id)                          AS jamiUrinish,
        ROUND(AVG(ur.ball)::numeric, 2)       AS ortachaBall,
        COUNT(*) FILTER (WHERE ur.baho = 'ALO')        AS aloSoni,
        COUNT(*) FILTER (WHERE ur.baho = 'YAXSHI')     AS yaxshiSoni,
        COUNT(*) FILTER (WHERE ur.baho = 'QONIQARLI')  AS qoniqarliSoni,
        COUNT(*) FILTER (WHERE ur.baho = 'QONIQARSIZ') AS qoniqarsizSoni
    FROM urinish ur
    JOIN users u ON u.id = ur.user_id
    JOIN okrug_tree ot ON ot.node_id = u.branch_id
    WHERE ur.holati = 'YAKUNLANGAN'
    GROUP BY ot.okrug_id, ot.okrug_nomi
    ORDER BY ot.okrug_nomi;
    """, nativeQuery = true)
    List<OrgResultProjection> findOkrugResults();

    @Query(value = """
            SELECT
              i.id   AS imtihonId,
              i.nomi AS imtihonNomi,
              i.savol_soni AS savolSoni,
              f2.id  AS fanId,
              f2.nomi AS fanNomi,

              COUNT(u.id) AS attempts,
              COUNT(CASE WHEN u.baho = 'ALO' THEN 1 END)        AS alo,
              COUNT(CASE WHEN u.baho = 'YAXSHI' THEN 1 END)     AS yaxshi,
              COUNT(CASE WHEN u.baho = 'QONIQARLI' THEN 1 END)  AS qoniqarli,
              COUNT(CASE WHEN u.baho = 'QONIQARSIZ' THEN 1 END) AS qoniqarsiz,

              COUNT(DISTINCT u.user_id) AS distinctUsers,

              CASE
                WHEN COUNT(u.id) = 0 THEN 0
                ELSE ROUND(
                  (SUM(CASE WHEN u.baho IN ('ALO','YAXSHI','QONIQARLI') THEN 1 ELSE 0 END) * 100.0)
                  / COUNT(u.id)
                , 2)
              END AS passRate

            FROM urinish u
            JOIN users fu ON fu.id = u.user_id
            JOIN imtihon i ON i.id = u.imtihon_id
            JOIN fan f2 ON f2.id = i.fan_id

            WHERE fu.branch_id = :orgId
              AND u.holati = 'YAKUNLANGAN'

            GROUP BY i.id, i.nomi, f2.id, f2.nomi
            ORDER BY attempts DESC NULLS LAST
            """, nativeQuery = true)
    List<OrgExamResultProjection> findOrgExamResults(@Param("orgId") Long orgId);


    @Query(value = """
        WITH u_in_org AS (
          SELECT fu.id AS user_id
          FROM users fu
          WHERE fu.branch_id = :orgId
        ),
        user_attempts AS (
          SELECT
            u.user_id                                        AS user_id,
            COUNT(*)                                                  AS attempts,
            COUNT(*) FILTER (WHERE u.holati='YAKUNLANGAN')            AS finished_attempts,
            MAX(u.ball) FILTER (WHERE u.holati='YAKUNLANGAN')         AS best_ball
          FROM urinish u
          WHERE u.imtihon_id = :examId
            AND u.user_id IN (SELECT user_id FROM u_in_org)
          GROUP BY u.user_id
        ),
        last_attempt AS (
          SELECT DISTINCT ON (u.user_id)
                 u.user_id              AS user_id,
                 u.tugadi               AS last_attempt_at,
                 u.ball                 AS last_ball,
                 u.baho                 AS last_baho,
                 i.savol_soni           AS savol_soni,
                 u.ball || '/' || i.savol_soni AS lastNatijaStat
          FROM urinish u
          JOIN imtihon i ON u.imtihon_id = i.id
          WHERE u.imtihon_id = :examId
            AND u.user_id IN (SELECT user_id FROM u_in_org)
            AND u.holati='YAKUNLANGAN'
          ORDER BY u.user_id, u.tugadi DESC NULLS LAST
        )
        SELECT
          f.id                                   AS userId,
          f.username                             AS username,
          f.first_name                           AS firstName,
          f.last_name                            AS lastName,
          COALESCE(f.middle_name, ' ')           AS middleName,
          f.branch_id                            AS tashkilotId,
          t.nomi                                 AS tashkilotNomi,
          ua.attempts                            AS attempts,
          ua.finished_attempts                   AS finishedAttempts,
          la.last_attempt_at                     AS lastAttemptAt,
          la.last_ball                           AS lastBall,
          la.lastNatijaStat                      AS lastNatijaStat,
          COALESCE(ua.best_ball, 0)              AS bestBall,
          la.last_baho                           AS lastBaho,
          la.savol_soni                          AS savolSoni
        FROM u_in_org x
        JOIN users f ON f.id = x.user_id
        JOIN unit t ON t.id = f.branch_id
        JOIN user_attempts ua ON ua.user_id = f.id
        LEFT JOIN last_attempt  la ON la.user_id = f.id
        ORDER BY la.last_attempt_at DESC NULLS LAST, ua.best_ball DESC NULLS LAST;
        """, nativeQuery = true)
    List<OrgExamParticipantProjection> findParticipantsByOrgAndExam(@Param("orgId") Long orgId,
                                                                    @Param("examId") Long examId);

    List<Unit> findAllByBranchTypeId(Long branchTypeId);}
