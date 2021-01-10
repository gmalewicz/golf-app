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
	
	//@Query("SELECT tr FROM TournamentResult tr where tr.player = :player AND tr.tournament = :tournament") 
	//Optional<TournamentResult> findByPlayerAndTournament(@Param("player") Player player, @Param("tournament") Tournament tournament);

	Optional<TournamentResult> findByPlayerAndTournament(Player player, Tournament tournament);
	
	//test required !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	Optional<TournamentResult> findByTournament(Tournament tournament);
	
	List<TournamentResult> findByTournamentOrderByPlayedRoundsDescStbNetDesc(Tournament tournament);
	
}
