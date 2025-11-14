package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.entity.Fan;
import com.example.employeecheckingplatform.entity.Foydalanuvchi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoydalanuvchiRepository extends JpaRepository<Foydalanuvchi, Long> {
    Optional<Foydalanuvchi> findByUsername(String username);
    Boolean existsByUsername(String username);

    List<Foydalanuvchi> findAllByFaolIs(Boolean faol);

    // Agar to'liq native bo'lsin desangiz, username -> id olishni ham native qilamiz:
    @Query(value = "SELECT id FROM foydalanuvchi WHERE username = :username LIMIT 1", nativeQuery = true)
    Long findIdByUsernameNative(@Param("username") String username);

    // Foydalanuvchi FIO (detail yig'ishda kerak bo'ladi)
    @Query(value = "SELECT toliq_ism FROM foydalanuvchi WHERE id = :id", nativeQuery = true)
    String findFullNameByIdNative(@Param("id") Long id);
}