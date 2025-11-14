package com.example.employeecheckingplatform.dto.urinish;

import java.time.Instant;

public interface UrinishHeaderRow {
    Long getId();

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