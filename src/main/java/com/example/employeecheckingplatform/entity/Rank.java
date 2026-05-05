package com.example.employeecheckingplatform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;


@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Rank extends AbstractEntity1 {

    @Column(nullable = false, unique = true, length = 500)
    private String nomi;

    private String qisqartmasi;

    private Integer darajasi;

}
