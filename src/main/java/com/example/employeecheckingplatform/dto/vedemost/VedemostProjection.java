package com.example.employeecheckingplatform.dto.vedemost;

import java.time.LocalDateTime;

public interface VedemostProjection {
    Long getUserId();
    String getFullname();
    String getUnitName();
    String getRankName();
    Double getBall();
    String getBaho();
    LocalDateTime getSana();
    String getTestNomi();
    Integer getSavolSoni();
    String getNatijaStat(); // "15/20" ko'rinishidagi natija uchun
}