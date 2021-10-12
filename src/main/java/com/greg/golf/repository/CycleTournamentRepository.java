package com.greg.golf.repository;

import com.greg.golf.entity.CycleTournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleTournamentRepository extends JpaRepository<CycleTournament, Long> {
}
