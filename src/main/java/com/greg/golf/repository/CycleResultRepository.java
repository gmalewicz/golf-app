package com.greg.golf.repository;

import com.greg.golf.entity.Cycle;
import com.greg.golf.entity.CycleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CycleResultRepository extends JpaRepository<CycleResult, Long> {

    List<CycleResult> findByCycle(Cycle cycle);

    List<CycleResult> findByCycleOrderByTotalDesc(Cycle cycle);

}
