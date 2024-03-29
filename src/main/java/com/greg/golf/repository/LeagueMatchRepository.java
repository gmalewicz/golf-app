package com.greg.golf.repository;

import com.greg.golf.entity.League;
import com.greg.golf.entity.LeagueMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueMatchRepository extends JpaRepository<LeagueMatch, Long> {

    List<LeagueMatch> findByLeagueId(Long leagueId);

    List<LeagueMatch> findByWinnerIdAndLooserIdAndLeague(Long winnerId, Long looserId, League league);

    void deleteByLeagueIdAndWinnerIdAndLooserId(Long leagueId, Long winnerId, Long looserId);
}
