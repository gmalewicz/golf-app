package com.greg.golf.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.Tournament;
import com.greg.golf.entity.TournamentResult;

@Repository
public interface TournamentResultRepository extends JpaRepository<TournamentResult, Long> {

	Optional<TournamentResult> findByPlayerAndTournament(Player player, Tournament tournament);
	
	//test required !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	Optional<TournamentResult> findByTournament(Tournament tournament);
	
	List<TournamentResult> findByTournamentOrderByPlayedRoundsDescStbNetDesc(Tournament tournament);
	
}
