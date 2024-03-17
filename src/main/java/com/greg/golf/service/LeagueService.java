package com.greg.golf.service;

import com.greg.golf.configurationproperties.LeagueServiceConfig;
import com.greg.golf.entity.*;
import com.greg.golf.error.*;
import com.greg.golf.repository.LeagueMatchRepository;
import com.greg.golf.repository.LeaguePlayerRepository;
import com.greg.golf.repository.LeagueRepository;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.service.helpers.RoleVerification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service("leagueService")
public class LeagueService {

    private static final String AUTHORIZATION_ERROR = "Attempt to add player by unauthorized user";

    private final LeagueServiceConfig leagueServiceConfig;

    private final LeagueRepository leagueRepository;

    private final PlayerRepository playerRepository;

    private final LeaguePlayerRepository leaguePlayerRepository;

    private final LeagueMatchRepository leagueMatchRepository;

    @Transactional
    public void addLeague(League league) {

        leagueRepository.save(league);
    }

    @Transactional(readOnly = true)
    public List<League> findAllLeaguesPageable(Integer pageNo) {
        return leagueRepository.findAllByOrderByIdDesc(PageRequest.of(pageNo, leagueServiceConfig.getPageSize()));
    }

    @Transactional
    public void addPlayer(LeaguePlayer leaguePlayer) throws DuplicatePlayerInLeagueException, UnauthorizedException {

        // only league owner can do it
        RoleVerification.verifyPlayer(leaguePlayer.getLeague().getPlayer().getId(), AUTHORIZATION_ERROR);

        // league must be opened
        if (leaguePlayer.getLeague().getStatus().equals(League.STATUS_CLOSE)) {
            throw new LeagueClosedException();
        }

        //check if player exists
        var player = playerRepository.findById(leaguePlayer.getPlayerId()).orElseThrow();

        //prepare data to save
        leaguePlayer.setNick(player.getNick());

        // save entity
        // trow exception if player has been already added to the league
        if (leaguePlayerRepository.findByLeagueIdAndPlayerId(leaguePlayer.getLeague().getId(), leaguePlayer.getPlayerId()).isEmpty()) {
            leaguePlayerRepository.save(leaguePlayer);
        } else {
            throw new DuplicatePlayerInLeagueException();
        }
    }

    @Transactional
    public void deletePlayer(Long leagueId, long playerId) throws PlayerHasMatchException {

        var league = leagueRepository.findById(leagueId).orElseThrow();

        // only league owner can do it
        RoleVerification.verifyPlayer(league.getPlayer().getId(), AUTHORIZATION_ERROR);

        // league must be opened
        if (league.getStatus().equals(League.STATUS_CLOSE)) {
            throw new LeagueClosedException();
        }

        // verify if player can be deleted. He cannot participate in any match.
        league.getLeagueMatches().forEach(match -> {
            if (match.getWinnerId() == playerId || match.getLooserId() == playerId) {
                throw new PlayerHasMatchException();
            }
        });

        league.getLeaguePlayers().removeIf(p ->  p.getPlayerId().equals(playerId) && p.getLeague().getId().equals(leagueId));
        leaguePlayerRepository.deleteByLeagueIdAndPlayerId(leagueId, playerId);
    }

    @Transactional
    public List<LeaguePlayer> getLeaguePlayers(Long leagueId) {

        return leaguePlayerRepository.findByLeagueId(leagueId);

    }

    @Transactional
    public void closeLeague(Long leagueId) {

        var league = leagueRepository.findById(leagueId).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(league.getPlayer().getId(), AUTHORIZATION_ERROR);

        // league must be opened
        if (league.getStatus().equals(League.STATUS_CLOSE)) {
            throw new LeagueClosedException();
        }

        // set close flag
        league.setStatus(League.STATUS_CLOSE);
        leagueRepository.save(league);
    }

    @Transactional
    public List<LeagueMatch> getMatches(Long leagueId) {


        return leagueMatchRepository.findByLeagueId(leagueId);

    }

    public void addMatch(LeagueMatch leagueMatch) throws DuplicateMatchInLeagueException, MatchResultForNotLeaguePlayerException {

        var league = leagueRepository.findById(leagueMatch.getLeague().getId()).orElseThrow();

        // only league owner can do it
        RoleVerification.verifyPlayer(league.getPlayer().getId(), AUTHORIZATION_ERROR);

        // league must be opened
        if (league.getStatus().equals(League.STATUS_CLOSE)) {
            throw new LeagueClosedException();
        }

        // match cannot be added twice
        if (!leagueMatchRepository
                .findByWinnerIdAndLooserIdAndLeague(leagueMatch.getWinnerId(), leagueMatch.getLooserId(), leagueMatch.getLeague()).isEmpty()) {
            throw new DuplicateMatchInLeagueException();
        }

        // winner and looser must be league players

        if (leaguePlayerRepository.findByLeagueIdAndPlayerId(league.getId(), leagueMatch.getLooserId()).isEmpty() ||
                leaguePlayerRepository.findByLeagueIdAndPlayerId(league.getId(), leagueMatch.getWinnerId()).isEmpty()) {
            throw new MatchResultForNotLeaguePlayerException();
        }

        leagueMatchRepository.save(leagueMatch);
    }

    @Transactional
    public void deleteMatch(Long leagueId, Long winnerId, Long looserId) {

        var league = leagueRepository.findById(leagueId).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(league.getPlayer().getId(), AUTHORIZATION_ERROR);

        // league must be opened
        if (league.getStatus().equals(League.STATUS_CLOSE)) {
            throw new LeagueClosedException();
        }

        leagueMatchRepository.deleteByLeagueIdAndWinnerIdAndLooserId(leagueId, winnerId, looserId);

    }

    @Transactional
    public void deleteLeague(Long leagueId) {

        var league = leagueRepository.findById(leagueId).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(league.getPlayer().getId(), AUTHORIZATION_ERROR);

        leagueRepository.delete(league);

    }

}
