// src/main/java/com/example/employeecheckingplatform/repository/projection/AttemptHeaderProjection.java
package com.example.employeecheckingplatform.dto.projection;

import java.time.Instant;

public interface AttemptHeaderProjection {
    Long getUrinishId();
    Long getFoydalanuvchiId();
    String getFoydalanuvchiIsm();
    Long getImtihonId();
    String getImtihonNomi();
    String getHolati();
    Integer getBall();
    Instant getBoshlandi();
    Instant getTugadi();
    String getBaho();
}
