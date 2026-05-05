package com.example.employeecheckingplatform.dto.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersonForFaceResult {
    Long id;
    String first_name;
    String last_name;
    String middle_name;
    String gender;
    String jshshir;
    Long branch_id;
    Long rank_id;
}