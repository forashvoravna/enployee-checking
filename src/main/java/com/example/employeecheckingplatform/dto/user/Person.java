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
public class Person {
    Long id;
    String first_name;
    String last_name;
    String middle_name;
    String gender;
    String jshshir;
    Long branch_id;
    String branch_nomi;
    String branch_path;
    Long rank_id;
    String rank_nomi;
    String avatar;
}