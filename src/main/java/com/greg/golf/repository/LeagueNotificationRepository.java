package com.greg.golf.repository;

import com.greg.golf.entity.LeagueNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueNotificationRepository extends JpaRepository<LeagueNotification, Long> {

    List<LeagueNotification> findByLeagueId(Long leagueId);

    void deleteByLeagueIdAndPlayerId(Long leagueId, Long playerId);

    void deleteByLeagueId(Long leagueId);
}
