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
public class UserAddWithImage {

    Long branch_id;
    String first_name;
//    String username;
    String gender;
    String jshshir;
    String last_name;
    String middle_name;
    Long rank_id;
    String avatar;

}