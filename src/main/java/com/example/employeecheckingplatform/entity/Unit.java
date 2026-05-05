package com.example.employeecheckingplatform.entity;

import com.example.employeecheckingplatform.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Entity(name = "unit")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Unit extends AbstractEntity1 {

    @Column(nullable = false, length = 500)
    String nomi;
    Long branchTypeId; // branch_type_id
    Long parentId;
    Long boshliqPersonId;
    String path;
    Integer level;
}