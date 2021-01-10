package com.greg.golf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Game;
import com.greg.golf.entity.Player;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
	
	List<Game> findByPlayer(Player player);

}
