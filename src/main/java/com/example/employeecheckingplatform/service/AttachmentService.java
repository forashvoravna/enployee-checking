package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.entity.Attachment;
import com.example.employeecheckingplatform.repository.AttachmentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;

    @Value("${upload.folder}")
    String uploadFolder;

    @Value("${upload.image-folder}")
    String images;




    public ResponseEntity<?> save(MultipartFile request) {
        try {
            if (
                    (Objects.equals(request.getContentType(), MediaType.IMAGE_PNG_VALUE))
            ) {
                Attachment attachment = new Attachment();
                attachment.setContentType(request.getContentType());
                attachment.setFileName(request.getOriginalFilename());
                attachment.setFileSize(request.getSize() / 1024);
                attachment.setHashId(UUID.randomUUID().toString());
                attachment.setExtension(getExtension(Objects.requireNonNull(request.getOriginalFilename())).toLowerCase());
                attachment.setUploadPath(getUploadPath(attachment.getExtension()));
                attachment.setLink(getLink(attachment));
                request.transferTo(new File(attachment.getLink()));
                attachmentRepository.save(attachment);
                return ResponseEntity.ok(attachment.getHashId());
            }
            return ResponseEntity.ok("Fayl kengaytmasi mos emas!!!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String saveImage(MultipartFile request) {
        try {
            if (request.isEmpty()) return null;
            if (
                    (Objects.equals(request.getContentType(), MediaType.IMAGE_PNG_VALUE))
            ) {
                Attachment attachment = new Attachment();
                attachment.setContentType(request.getContentType());
                attachment.setFileName(request.getOriginalFilename());
                attachment.setFileSize(request.getSize() / 1024);
                attachment.setHashId(UUID.randomUUID().toString());
                attachment.setExtension(getExtension(Objects.requireNonNull(request.getOriginalFilename())).toLowerCase());
                attachment.setUploadPath(getUploadPath(attachment.getExtension()));
                attachment.setLink(getLink(attachment));
                request.transferTo(new File(attachment.getLink()));
                attachmentRepository.save(attachment);
                return attachment.getHashId();
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public String saveImageBase64(String base64Image) {

        if (base64Image.isEmpty()) return null;
        if (base64Image.contains(",")) {
            base64Image = base64Image.split(",")[1];
        }
        Attachment attachment = new Attachment();
        attachment.setContentType("image/png");
        attachment.setFileName("Avatar");
        attachment.setHashId(UUID.randomUUID().toString());
        attachment.setExtension("png");
        attachment.setUploadPath(getUploadPath(attachment.getExtension()));
        attachment.setLink(getLink(attachment));

        try (OutputStream outputStream = new FileOutputStream(attachment.getLink())) {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            outputStream.write(imageBytes);
            attachment.setFileSize(imageBytes.length / 1024L);
        } catch (Exception e) {
            return null;
        }
        attachmentRepository.save(attachment);
        return attachment.getHashId();
    }


    public ResponseEntity<?> getAttachmentByHashId(String hashId) throws MalformedURLException {
        Optional<Attachment> attachmentOptional = attachmentRepository.findByHashIdAndDeleted(hashId, false);
        if (attachmentOptional.isPresent()) {
            Attachment attachment = attachmentOptional.get();
            return ResponseEntity.ok()
                    .header(HttpHeaders.EXPIRES, "inline; fileName=" + URLEncoder.encode(attachment.getFileName()))
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .body(new FileUrlResource(attachment.getLink()));
        }
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<?> downloadAttachment(String hashId) throws MalformedURLException {
        Optional<Attachment> attachmentOptional = attachmentRepository.findByHashIdAndDeleted(hashId, false);

        if (attachmentOptional.isPresent()) {
            Attachment attachment = attachmentOptional.get();

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + URLEncoder.encode(attachment.getFileName() + ".png", StandardCharsets.UTF_8) + "\""
                    )
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .body(new FileUrlResource(attachment.getLink()));
        }

        return ResponseEntity.badRequest().body("Bunday hashId qiymatli fayl topilmadi: " + hashId);
    }


    private boolean saveFileWithStream(byte[] fileBytes, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(fileBytes);
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String getUploadPath(String extension) {
        LocalDate localDate = LocalDate.now();
        String upload = String.format("%s/%d/%d/%d/%s",
                images,
                localDate.getYear(),
                localDate.getMonthValue(),
                localDate.getDayOfMonth(),
                extension
        );
        File file = new File(upload);
        if (!file.exists()) file.mkdirs();
        return upload;
    }

    private String getUploadPath() {
        LocalDate localDate = LocalDate.now();
        String upload = String.format("%s/%d/%d/%d/%s",
                uploadFolder,
                localDate.getYear(),
                localDate.getMonthValue(),
                localDate.getDayOfMonth(),
                "png"
        );
        File file = new File(upload);
        if (!file.exists()) file.mkdirs();
        return upload;
    }

    private String getLink(Attachment attachment) {
        File file = new File(attachment.getUploadPath());
        return String.format("%s/%s.%s",
                file.getAbsolutePath(),
                attachment.getHashId(),
                attachment.getExtension()
        );
    }



}
