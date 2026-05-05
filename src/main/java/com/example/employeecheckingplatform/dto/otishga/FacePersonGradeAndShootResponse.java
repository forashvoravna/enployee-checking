package com.example.employeecheckingplatform.dto.otishga;

import com.example.employeecheckingplatform.dto.ShootingGradeDto;
import com.example.employeecheckingplatform.dto.user.Person;
import com.example.employeecheckingplatform.dto.user.PersonForFaceResult;

public record FacePersonGradeAndShootResponse(
        PersonForFaceResult person,
        GradeDto lastGrade,        // Test natijasi
        ShootingGradeDto lastShooting // O'q otish natijasi (yangi)
) {}


