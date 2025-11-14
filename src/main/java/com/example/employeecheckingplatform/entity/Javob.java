package com.example.employeecheckingplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "javob", uniqueConstraints = @UniqueConstraint(columnNames = {"urinish_id", "savol_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Javob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "urinish_id", nullable = false)
    private Urinish urinish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savol_id", nullable = false)
    private Savol savol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant selectedVariant;

    private Boolean togri; // finishda belgilanadi
}
