package com.greg.golf.repository;

import com.greg.golf.entity.TournamentPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentPlayerRepository extends JpaRepository<TournamentPlayer, Long> {

    void deleteByTournamentId(Long tournamentId);

    void deleteByTournamentIdAndPlayerId(Long tournamentId, Long playerId);

    List<TournamentPlayer> findByTournamentId(Long tournamentId);

    Optional<TournamentPlayer> findByTournamentIdAndPlayerId(Long tournamentId, Long playerId);

}
