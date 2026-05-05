package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.entity.Fan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FanRepository extends JpaRepository<Fan, Long> {

    List<Fan> findAllByDeletedFalse();
}

