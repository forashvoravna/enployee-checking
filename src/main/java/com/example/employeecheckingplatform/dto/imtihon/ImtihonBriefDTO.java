// src/main/java/com/example/employeecheckingplatform/dto/imtihon/ImtihonDTOBrief.java
package com.example.employeecheckingplatform.dto.imtihon;

public interface ImtihonBriefDTO {
    Long getId();
    String getNomi();
    Integer getDavomiylikDaqiqa();
    Integer getMaxUrinish();
    Integer getSavolSoni();
    Long getFanId();
    String getFanNomi();
    Integer getTekshirishSoni();
    Integer getTekshirishVaqti();

    Integer getAloPct();
    Integer getYaxshiPct();
    Integer getQoniqarliPct();

    Integer getAttemptsUsed();
    Integer getAttemptsLeft();
    Boolean getCanStart();
    Long getUrinishId();
}
