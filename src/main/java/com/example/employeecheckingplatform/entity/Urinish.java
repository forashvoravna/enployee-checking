package com.example.employeecheckingplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "urinish")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Urinish extends AbstractEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imtihon_id", nullable = false)
    private Imtihon imtihon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "urinish", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UrinishSavol> urinishSavollari = new ArrayList<>();

    @OneToMany(mappedBy = "urinish", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Javob> javoblar = new ArrayList<>();

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
