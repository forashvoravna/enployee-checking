package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.dto.projection.OrgExamParticipantProjection;
import com.example.employeecheckingplatform.dto.projection.OrgExamResultProjection;
import com.example.employeecheckingplatform.dto.projection.OrgResultProjection;
import com.example.employeecheckingplatform.entity.Tashkilot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TashkilotRepository extends JpaRepository<Tashkilot, Long> {
    List<Tashkilot> findByParentIsNullAndFaolTrue();

    List<Tashkilot> findByTuri(String turi);

    List<Tashkilot> findByParent_Id(Long parentId);

    @Query(value = """
            SELECT
                f.tashkilot_id AS tashkilotId,
                t.nomi AS tashkilotNomi,
                COUNT(u.id) AS jamiUrinish,
                ROUND(AVG(u.ball)::numeric, 2) AS ortachaBall,
                COUNT(CASE WHEN u.baho = 'ALO' THEN 1 END) AS aloSoni,
                COUNT(CASE WHEN u.baho = 'YAXSHI' THEN 1 END) AS yaxshiSoni,
                COUNT(CASE WHEN u.baho = 'QONIQARLI' THEN 1 END) AS qoniqarliSoni,
                COUNT(CASE WHEN u.baho = 'QONIQARSIZ' THEN 1 END) AS qoniqarsizSoni
            FROM urinish u
            JOIN foydalanuvchi f ON f.id = u.foydalanuvchi_id
            JOIN tashkilot t ON t.id = f.tashkilot_id
            WHERE t.parent_id  = :orgId
              AND u.holati = 'YAKUNLANGAN'
            GROUP BY f.tashkilot_id, t.nomi
            ORDER BY t.nomi
            """, nativeQuery = true)
    List<OrgResultProjection> findOrgResults(@Param("orgId") Long orgId);

    @Query(value = """
            WITH RECURSIVE okrug_tree AS (
                SELECT t.id AS okrug_id,
                       t.id AS node_id,
                       t.nomi AS okrug_nomi
                FROM tashkilot t
                WHERE t.turi = 'okrug'
            
                UNION ALL
            
                SELECT ot.okrug_id,
                       c.id AS node_id,
                       ot.okrug_nomi
                FROM tashkilot c
                JOIN okrug_tree ot ON c.parent_id = ot.node_id
            )
            SELECT
                ot.okrug_id                           AS tashkilotId,
                ot.okrug_nomi                         AS tashkilotNomi,
                COUNT(u.id)                           AS jamiUrinish,
                ROUND(AVG(u.ball)::numeric, 2)        AS ortachaBall,
                COUNT(*) FILTER (WHERE u.baho = 'ALO')        AS aloSoni,
                COUNT(*) FILTER (WHERE u.baho = 'YAXSHI')     AS yaxshiSoni,
                COUNT(*) FILTER (WHERE u.baho = 'QONIQARLI')  AS qoniqarliSoni,
                COUNT(*) FILTER (WHERE u.baho = 'QONIQARSIZ') AS qoniqarsizSoni
            FROM urinish u
            JOIN foydalanuvchi f ON f.id = u.foydalanuvchi_id
            JOIN okrug_tree ot   ON ot.node_id = f.tashkilot_id
            WHERE u.holati = 'YAKUNLANGAN'
            GROUP BY ot.okrug_id, ot.okrug_nomi
            ORDER BY ot.okrug_nomi;
            
            """, nativeQuery = true)
    List<OrgResultProjection> findOkrugResults();

    @Query(value = """
            SELECT
              i.id   AS imtihonId,
              i.nomi AS imtihonNomi,
              f2.id  AS fanId,
              f2.nomi AS fanNomi,

              COUNT(u.id) AS attempts,
              ROUND(AVG(u.ball)::numeric, 2) AS avgScore,

              COUNT(CASE WHEN u.baho = 'ALO' THEN 1 END)        AS alo,
              COUNT(CASE WHEN u.baho = 'YAXSHI' THEN 1 END)     AS yaxshi,
              COUNT(CASE WHEN u.baho = 'QONIQARLI' THEN 1 END)  AS qoniqarli,
              COUNT(CASE WHEN u.baho = 'QONIQARSIZ' THEN 1 END) AS qoniqarsiz,

              COUNT(DISTINCT u.foydalanuvchi_id) AS distinctUsers,

              CASE
                WHEN COUNT(u.id) = 0 THEN 0
                ELSE ROUND(
                  (SUM(CASE WHEN u.baho IN ('ALO','YAXSHI','QONIQARLI') THEN 1 ELSE 0 END) * 100.0)
                  / COUNT(u.id)
                , 2)
              END AS passRate

            FROM urinish u
            JOIN foydalanuvchi fu ON fu.id = u.foydalanuvchi_id
            JOIN imtihon i ON i.id = u.imtihon_id
            JOIN fan f2 ON f2.id = i.fan_id

            WHERE fu.tashkilot_id = :orgId
              AND u.holati = 'YAKUNLANGAN'

            GROUP BY i.id, i.nomi, f2.id, f2.nomi
            ORDER BY attempts DESC NULLS LAST, avgScore DESC NULLS LAST
            """, nativeQuery = true)
    List<OrgExamResultProjection> findOrgExamResults(@Param("orgId") Long orgId);


    @Query(value = """
            WITH u_in_org AS (
              SELECT fu.id AS user_id
              FROM foydalanuvchi fu
              WHERE fu.tashkilot_id = :orgId
            ),
            user_attempts AS (
              SELECT
                u.foydalanuvchi_id                                        AS user_id,
                COUNT(*)                                                  AS attempts,
                COUNT(*) FILTER (WHERE u.holati='YAKUNLANGAN')            AS finished_attempts,
                MAX(u.ball) FILTER (WHERE u.holati='YAKUNLANGAN')         AS best_ball,
                ROUND( (AVG(u.ball) FILTER (WHERE u.holati='YAKUNLANGAN'))::numeric, 2 ) AS avg_ball
              FROM urinish u
              WHERE u.imtihon_id = :examId
                AND u.foydalanuvchi_id IN (SELECT user_id FROM u_in_org)
              GROUP BY u.foydalanuvchi_id
            ),
            last_attempt AS (
              SELECT DISTINCT ON (u.foydalanuvchi_id)
                     u.foydalanuvchi_id              AS user_id,
                     u.tugadi                        AS last_attempt_at,
                     u.ball                          AS last_ball,
                     u.baho                          AS last_baho
              FROM urinish u
              WHERE u.imtihon_id = :examId
                AND u.foydalanuvchi_id IN (SELECT user_id FROM u_in_org)
                AND u.holati='YAKUNLANGAN'
              ORDER BY u.foydalanuvchi_id, u.tugadi DESC NULLS LAST
            )
            SELECT
              f.id                                   AS userId,
              f.username                             AS username,
              f.toliq_ism                            AS fullName,
              f.tashkilot_id                         AS tashkilotId,
              t.nomi                                 AS tashkilotNomi,
              ua.attempts                            AS attempts,
              ua.finished_attempts                   AS finishedAttempts,
              la.last_attempt_at                     AS lastAttemptAt,
              la.last_ball                           AS lastBall,
              COALESCE(ua.best_ball, 0)              AS bestBall,
              ua.avg_ball                            AS avgBall,
              la.last_baho                           AS lastBaho
            FROM u_in_org x
            JOIN foydalanuvchi f ON f.id = x.user_id
            JOIN tashkilot t ON t.id = f.tashkilot_id
            JOIN user_attempts ua ON ua.user_id = f.id
            LEFT JOIN last_attempt  la ON la.user_id = f.id
            ORDER BY la.last_attempt_at DESC NULLS LAST, ua.best_ball DESC NULLS LAST;
            """, nativeQuery = true)
    List<OrgExamParticipantProjection> findParticipantsByOrgAndExam(@Param("orgId") Long orgId,
                                                                    @Param("examId") Long examId);


}