package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.dto.urinish.UrinishHeaderRow;
import com.example.employeecheckingplatform.entity.UrinishSavol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UrinishSavolRepository extends JpaRepository<UrinishSavol, Long> {
    List<UrinishSavol> findByUrinishIdOrderByPositionAsc(Long urinishId);

    long countByUrinishId(Long urinishId);
}
