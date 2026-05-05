package com.example.employeecheckingplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "imtihon", uniqueConstraints = @UniqueConstraint(columnNames = {"fan_id", "nomi"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Imtihon extends AbstractEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fan_id", nullable = false)
    private Fan fan;

    @Column(nullable = false)
    private String nomi;
    @Column(nullable = false)
    private Integer savolSoni;
    @Column(nullable = false)
    private Integer davomiylikDaqiqa = 30;
    @Column(nullable = false)
    private Integer maxUrinish = 1;

    // baholash threshold foizlarda
    @Column(nullable = false)
    private Integer aloPct = 86;
    @Column(nullable = false)
    private Integer yaxshiPct = 71;
    @Column(nullable = false)
    private Integer qoniqarliPct = 56;

    private Integer tekshirishVaqti = 10;
    private Integer tekshirishSoni = 3;

    // ixtiyoriy — faqat belgilangan userlar topshira oladi
    @ManyToMany
    @JoinTable(name = "imtihon_user",
            joinColumns = @JoinColumn(name = "imtihon_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", nullable = true))
    @Builder.Default
    private Set<User> ruxsatEtilganlar = new HashSet<>();

    @OneToMany(mappedBy = "imtihon", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Urinish> urinishlar = new ArrayList<>();

}
