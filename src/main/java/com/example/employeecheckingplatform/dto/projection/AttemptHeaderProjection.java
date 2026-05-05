// src/main/java/com/example/employeecheckingplatform/repository/projection/AttemptHeaderProjection.java
package com.example.employeecheckingplatform.dto.projection;

import java.time.Instant;

public interface AttemptHeaderProjection {
    Long getUrinishId();
    Long getUserId();
    String getUserIsm();
    Long getImtihonId();
    String getImtihonNomi();
    String getHolati();
    Integer getBall();
    Integer getSavolSoni();
    Instant getBoshlandi();
    Instant getTugadi();
    String getBaho();
}
