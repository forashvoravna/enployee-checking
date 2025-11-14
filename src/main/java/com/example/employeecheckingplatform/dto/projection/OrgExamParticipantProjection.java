package com.example.employeecheckingplatform.dto.projection;

import java.time.Instant;

public interface OrgExamParticipantProjection {
    Long   getUserId();
    String getUsername();
    String getFullName();

    Long   getTashkilotId();
    String getTashkilotNomi();

    Long   getAttempts();          // jami urinishlar (finished+unfinished)
    Long   getFinishedAttempts();  // yakunlangan urinishlar
    Instant getLastAttemptAt();    // so‘nggi yakunlangan urinish vaqti (NULL bo‘lishi mumkin)
    Integer getLastBall();         // so‘nggi yakunlangan urinish balli (NULL bo‘lishi mumkin)
    Integer getBestBall();         // eng yaxshi ball
    Double  getAvgBall();          // o‘rtacha ball (round 2, yakunlangan urinishlardan)
    String  getLastBaho();         // so‘nggi yakunlangan urinish bahosi (ALO/YAXSHI/...)
}
