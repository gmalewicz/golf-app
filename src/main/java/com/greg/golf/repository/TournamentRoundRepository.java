package com.greg.golf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.greg.golf.entity.TournamentResult;
import com.greg.golf.entity.TournamentRound;

@Repository
public interface TournamentRoundRepository extends JpaRepository<TournamentRound, Long> {
	
	List<TournamentRound> findByTournamentResultOrderByIdAsc(TournamentResult tournamentResult);

}
