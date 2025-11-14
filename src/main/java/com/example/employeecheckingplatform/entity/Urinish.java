package com.example.employeecheckingplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "urinish")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Urinish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imtihon_id", nullable = false)
    private Imtihon imtihon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foydalanuvchi_id", nullable = false)
    private Foydalanuvchi foydalanuvchi;

    @Column(nullable = false)
    private Instant boshladi;
    private Instant tugadi;

    @Column(nullable = false)
    private String holati; // JARAYONDA / YAKUNLANGAN
    @Column(nullable = false)
    private Integer ball = 0;

    @Enumerated(EnumType.STRING)
    private Baho baho; // ALO/YAXSHI/QONIQARLI/QONIQARSIZ

    public enum Baho {ALO, YAXSHI, QONIQARLI, QONIQARSIZ}
}
