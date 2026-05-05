package com.example.employeecheckingplatform.dto.otishga;

import com.example.employeecheckingplatform.dto.user.Person;
import com.example.employeecheckingplatform.dto.user.PersonForFaceResult;

public record FacePersonGradeResponse(
        PersonForFaceResult person,
        GradeDto lastGrade   // null bo'lishi mumkin (agar imtihoni bo'lmasa)
) {}


