package com.greg.golf.repository;

import com.greg.golf.entity.Cycle;
import com.greg.golf.entity.CycleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CycleResultRepository extends JpaRepository<CycleResult, Long> {

    List<CycleResult> findByCycle(Cycle cycle);

    List<CycleResult> findByCycleOrderBySeriesAscCycleScoreDesc(Cycle cycle);

    @Modifying
    @Query(value = "DELETE FROM cycle_result c where c.cycle_id = :cycleId", nativeQuery = true)
    void deleteForCycle(@Param("cycleId") Long cycleId);
}
