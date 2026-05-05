package com.example.employeecheckingplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.math3.analysis.function.Abs;

import java.util.List;

@Entity
@Table(name = "fan", uniqueConstraints = @UniqueConstraint(columnNames = "nomi"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Fan  extends AbstractEntity {
    @Column(nullable = false)
    private String nomi;
    private String tavsif;
    @OneToMany(mappedBy = "fan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Imtihon> imtihonlar;

}
