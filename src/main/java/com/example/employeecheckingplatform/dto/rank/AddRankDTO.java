package com.example.employeecheckingplatform.dto.rank;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddRankDTO {
    String nomi;
    String qisqartmasi;
    Integer darajasi;
    Long id;
}
