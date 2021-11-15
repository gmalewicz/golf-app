package com.greg.golf.repository;

import com.greg.golf.entity.Cycle;
import com.greg.golf.entity.CycleTournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CycleTournamentRepository extends JpaRepository<CycleTournament, Long> {

    List<CycleTournament> findByCycleOrderById(Cycle cycle);
}
