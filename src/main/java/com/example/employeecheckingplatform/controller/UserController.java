package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.user.UserAddWithImage;
import com.example.employeecheckingplatform.dto.user.UserCreateDto;
import com.example.employeecheckingplatform.dto.user.UserUpdateDto;
import com.example.employeecheckingplatform.entity.User;
import com.example.employeecheckingplatform.repository.UserRepository;
import com.example.employeecheckingplatform.service.RefreshFaceService;
import com.example.employeecheckingplatform.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final RefreshFaceService refreshFace;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserCreateDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }
    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> setPassword(@PathVariable Long id, @RequestBody String password) {
        return ResponseEntity.ok(service.setPassword(id, password));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @PostMapping(value = "/verify", consumes = "multipart/form-data")

    public ResponseEntity<?> verifyUserByImage(@RequestParam("file") MultipartFile request) throws JsonProcessingException {
        return service.verifyUserByImage(request);
    }


    @PostMapping(value = "/add/byImage/two")
    public ResponseEntity<?> addPersonWithImageTwo( @RequestBody UserAddWithImage dto, @AuthenticationPrincipal User user) {
        return service.addPersonWithImageTwo(user, dto);
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(@AuthenticationPrincipal User user){
        return refreshFace.refreshFace(user);
    }
}
