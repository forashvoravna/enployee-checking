package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;


    @GetMapping("/preview/{hashId}")
    @Operation(
            summary = "Hujjatni ko'rish"
    )
    public ResponseEntity<?> preview(@PathVariable String hashId) throws MalformedURLException {
        return attachmentService.getAttachmentByHashId(hashId);
    }

}
