package com.example.employeecheckingplatform.dto.projection;

public interface OrgExamResultProjection {
    Long   getImtihonId();
    String getImtihonNomi();
    Integer getSavolSoni();
    Long   getFanId();
    String getFanNomi();

    Integer   getAttempts();       // jami urinishlar
    Integer   getAlo();            // ALO soni
    Integer   getYaxshi();         // YAXSHI soni
    Integer   getQoniqarli();      // QONIQARLI soni
    Integer   getQoniqarsiz();     // QONIQARSIZ soni
    Integer   getDistinctUsers();  // necha xil foydalanuvchi topshirgan
}
