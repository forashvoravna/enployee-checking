package com.example.employeecheckingplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "savol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Savol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fan_id", nullable = false)
    private Fan fan;

    @Column(nullable = false, columnDefinition = "text")
    private String matn;

    @OneToMany(mappedBy = "savol", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Variant> variantlar = new ArrayList<>();
}
