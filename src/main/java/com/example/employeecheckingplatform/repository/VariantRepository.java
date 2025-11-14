package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariantRepository extends JpaRepository<Variant, Long> {
    List<Variant> findBySavol_Id(Long savolId);
}