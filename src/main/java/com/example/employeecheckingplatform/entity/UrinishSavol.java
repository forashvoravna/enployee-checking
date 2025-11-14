package com.example.employeecheckingplatform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "urinish_savol", uniqueConstraints = @UniqueConstraint(columnNames = {"urinish_id", "savol_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UrinishSavol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "urinish_id", nullable = false)
    private Urinish urinish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savol_id", nullable = false)
    private Savol savol;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "variant_order_json", columnDefinition = "text")
    private String variantOrderJson;
}
