package com.greg.golf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;

@Repository
public interface ScoreCardRepository extends JpaRepository<ScoreCard, Integer> {
	
	//results need to be sorted by id
	@Query("SELECT sc FROM ScoreCard sc where sc.round = :round ORDER BY sc.player, sc.id") 
	List<ScoreCard> findByRound(@Param("round") Round round);
	
	List<ScoreCard> findByRoundAndPlayer(Round round, Player player);

}
