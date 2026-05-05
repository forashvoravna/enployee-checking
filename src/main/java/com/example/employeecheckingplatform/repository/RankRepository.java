package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {

    List<Rank> findAll();
    List<Rank> findAllByDeleted(boolean deleted);
    Optional<Rank> findByIdAndDeleted(Long id, boolean deleted);
}
