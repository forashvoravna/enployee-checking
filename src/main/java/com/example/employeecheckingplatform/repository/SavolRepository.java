package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.entity.Savol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SavolRepository extends JpaRepository<Savol, Long> {
    List<Savol> findAllByFan_Id(Long fanId);
    long countByFan_Id(Long fanId);

    List<Savol> findAllByIdIn(Iterable<Long> ids);
    @Query(value = """
    SELECT DISTINCT ON (s.id) s.id
    FROM savol s
    WHERE s.fan_id = :fanId
    ORDER BY s.id, random()
    LIMIT :limit
    """, nativeQuery = true)
    List<Long> findRandomIdsByFanId(@Param("fanId") Long fanId, @Param("limit") int limit);



    @Query(value = """
        SELECT DISTINCT s.id
        FROM savol s
        WHERE s.fan_id = :fanId
        """, nativeQuery = true)
    List<Long> findAllActiveIdsByFanId(@Param("fanId") Long fanId);

    @Query(value = """
        SELECT COUNT(*) 
        FROM savol s
        WHERE s.fan_id = :fanId
        """, nativeQuery = true)
    int countActiveByFanId(@Param("fanId") Long fanId);

    // Agar kerak bo'lsa:
    List<Savol> findAllByIdIn(Collection<Long> ids);

}