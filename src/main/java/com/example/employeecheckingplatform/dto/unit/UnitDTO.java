package com.example.employeecheckingplatform.dto.unit;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnitDTO {
    String nomi;
    Long branch_type_id;
    Long parent_id;
    Long boshliq_person_id;
    Long id;
    String path;
    Integer level;
}
