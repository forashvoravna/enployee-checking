package com.example.employeecheckingplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "variant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Variant extends AbstractEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savol_id", nullable = false)
    private Savol savol;

    @Column(nullable = false, columnDefinition = "text")
    private String matn;

    @Column(nullable = false)
    private Boolean togri; // faqat BITTA true bo‘ladi (multiple-choice single-correct)
}
