package com.example.employeecheckingplatform.dto.projection;

import java.time.Instant;

public interface OrgExamParticipantProjection {
    Long   getUserId();
    String getUsername();
    String getFirstName();
    String getLastName();
    String getMiddleName();

    Long   getTashkilotId();
    String getTashkilotNomi();
    String getLastNatijaStat();
    Integer getSavolSoni();
    Integer   getAttempts();          // jami urinishlar (finished+unfinished)
    Integer   getFinishedAttempts();  // yakunlangan urinishlar
    Instant getLastAttemptAt();    // so‘nggi yakunlangan urinish vaqti (NULL bo‘lishi mumkin)
    Integer getLastBall();         // so‘nggi yakunlangan urinish balli (NULL bo‘lishi mumkin)
    Integer getBestBall();         // eng yaxshi ball
    String  getLastBaho();         // so‘nggi yakunlangan urinish bahosi (ALO/YAXSHI/...)
}
