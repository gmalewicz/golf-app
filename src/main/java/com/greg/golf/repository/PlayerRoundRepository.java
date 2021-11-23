package com.greg.golf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.entity.PlayerRound;

@Repository
public interface PlayerRoundRepository extends JpaRepository<PlayerRound, Long> {

	@Transactional(propagation = Propagation.REQUIRED)
	@Modifying
	@Query("UPDATE PlayerRound pr SET pr.whs = :whs, pr.teeId = :teeId, pr.sr = :sr, pr.cr = :cr, pr.teeType = :teeType where pr.playerId = :playerId AND pr.roundId = :roundId")
	void updatePlayerRoundInfo(@Param("whs") Float whs,
							  @Param("sr") Integer sr,
							  @Param("cr") Float cr,
							  @Param("teeId") Long teeId,  
							  @Param("teeType") Integer teeType,
							  @Param("playerId") Long playerId, 
							  @Param("roundId") Long roundId);
	
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	@Query("SELECT pr FROM PlayerRound pr WHERE pr.playerId = :playerId AND pr.roundId = :roundId")
	Optional<PlayerRound> getForPlayerAndRound(@Param("playerId") Long playerId, @Param("roundId") Long roundId);
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	Optional<List<PlayerRound>> findByRoundIdOrderByPlayerId(Long roundId);

}
