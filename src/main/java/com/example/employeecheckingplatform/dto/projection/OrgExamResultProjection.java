package com.example.employeecheckingplatform.dto.projection;

public interface OrgExamResultProjection {
    Long   getImtihonId();
    String getImtihonNomi();
    Long   getFanId();
    String getFanNomi();

    Long   getAttempts();       // jami urinishlar
    Double getAvgScore();       // o‘rtacha ball (round 2)
    Long   getAlo();            // ALO soni
    Long   getYaxshi();         // YAXSHI soni
    Long   getQoniqarli();      // QONIQARLI soni
    Long   getQoniqarsiz();     // QONIQARSIZ soni
    Long   getDistinctUsers();  // necha xil foydalanuvchi topshirgan
    Double getPassRate();       // (ALO+YAXSHI+QONIQARLI)/attempts * 100
}
