package com.greg.golf.repository;

import com.greg.golf.entity.LeaguePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaguePlayerRepository extends JpaRepository<LeaguePlayer, Long> {

    void deleteByLeagueIdAndPlayerId(Long leagueId, Long playerId);

    List<LeaguePlayer> findByLeagueId(Long leagueId);

   Optional<LeaguePlayer> findByLeagueIdAndPlayerId(Long leagueId, Long playerId);
}
