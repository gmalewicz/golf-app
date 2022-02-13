package com.greg.golf.repository;

import java.util.List;
import java.util.Optional;

import com.greg.golf.repository.projection.PlayerRoundCnt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Player;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
	
	 @Query("SELECT p FROM Player p where p.nick = :nick") 
	 Optional<Player> findPlayerByNick(@Param("nick") String nick);

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	@Query("SELECT p.id AS id, p.nick AS nick, p.sex AS sex, p.whs AS whs, p.role AS role, p.type AS type, COUNT(r) AS roundCnt FROM Player p left join p.rounds as r GROUP BY p.id, p.nick, p.sex, p.whs, p.role ORDER By p.id")
	List<PlayerRoundCnt> getPlayerRoundCnt();
}
