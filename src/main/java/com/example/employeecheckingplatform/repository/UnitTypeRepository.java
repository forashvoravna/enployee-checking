package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.entity.UnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnitTypeRepository extends JpaRepository<UnitType, Long> {
    List<UnitType> findAllByDeleted(boolean deleted);
}
