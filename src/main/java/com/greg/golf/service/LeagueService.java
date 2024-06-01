package com.greg.golf.service;

import com.greg.golf.configurationproperties.LeagueServiceConfig;
import com.greg.golf.controller.dto.LeagueResultDto;
import com.greg.golf.entity.*;
import com.greg.golf.error.*;
import com.greg.golf.repository.*;
import com.greg.golf.service.helpers.RoleVerification;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
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

    private final LeagueNotificationRepository leagueNotificationRepository;

    private final PlayerService playerService;

    private final EmailServiceImpl emailServiceImpl;

    private final TemplateEngine templateEngine;

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

        //remove notifications
        leagueNotificationRepository.deleteByLeagueId(leagueId);
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

        // remove notifications
        leagueNotificationRepository.deleteByLeagueId(leagueId);

        leagueRepository.delete(league);

    }

    @Transactional(readOnly = true)
    public int processNotifications(Long leagueId, LeagueResultDto[] leagueResultDto) throws GeneralException {

        int sentNotifications = 0;

        var notifications = leagueNotificationRepository.findByLeagueId(leagueId);

        if (!notifications.isEmpty()) {

            var recipients = new ArrayList<String>();

            notifications.forEach(notification -> {

                var email = playerService.getEmail(notification.getPlayerId());

                if (email != null) {
                    recipients.add(email);
                }
            });

            if (!recipients.isEmpty()) {

                var league = leagueRepository.findById(leagueId);

                // only tournament owner can do it
                RoleVerification.verifyPlayer(league.orElseThrow().getPlayer().getId(), "Attempt to process notifications by unauthorized user");

                var context = new Context();
                context.setVariable("results", leagueResultDto);
                context.setVariable("leagueName", league.orElseThrow().getName());


                String body = templateEngine.process("LeagueResultsTemplate.html", context);

                try {
                    emailServiceImpl.sendEmail(recipients.toArray(new String[0]), "Tournament results updated - " + league.orElseThrow().getName(), body);
                    sentNotifications++;
                } catch (MessagingException e) {
                    throw new GeneralException();
                }
            }
        }
        return sentNotifications;
    }

    @Transactional
    public void addNotification(Long leagueId) throws DuplicateNotificationException, MailNotSetException {

        Long playerId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        log.info("trying to add notifications for league: " + leagueId + " and player: " + playerId);

        // check if player has defined email
        var player = playerRepository.findById(playerId);
        if (player.orElseThrow().getEmail() == null) {
            throw new MailNotSetException();
        }

        // check if tournament is not closed
        var league = leagueRepository.findById(leagueId).orElseThrow();
        if (league.getStatus() == League.STATUS_OPEN) {

            var leagueNotification = new LeagueNotification();
            leagueNotification.setLeagueId(leagueId);
            leagueNotification.setPlayerId(playerId);

            try {
                leagueNotificationRepository.save(leagueNotification);
            } catch (Exception ex) {
                throw  new DuplicateNotificationException();
            }
        }
    }

    @Transactional
    public void removeNotification(Long leagueId) {

        Long playerId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        log.info("trying to remove notifications for league: " + leagueId + " and player: " + playerId);

        leagueNotificationRepository.deleteByLeagueIdAndPlayerId(leagueId, playerId);
    }
}
