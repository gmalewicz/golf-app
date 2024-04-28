package com.greg.golf.repository;

import com.greg.golf.entity.TournamentNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentNotificationRepository extends JpaRepository<TournamentNotification, Long> {

    List<TournamentNotification> findByTournamentId(Long tournamentId);

    void deleteByTournamentIdAndPlayerId(Long leagueId, Long playerId);
}
