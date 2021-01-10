package com.greg.golf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Player;


@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
	
	 @Query("SELECT p FROM Player p where p.nick = :nick") 
	 Optional<Player> findPlayerByNick(@Param("nick") String nick);
}
