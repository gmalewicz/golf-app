package com.greg.golf.service;

import com.greg.golf.controller.dto.LeagueResultDto;
import com.greg.golf.entity.*;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.*;
import com.greg.golf.repository.*;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.aes.StringUtility;
import com.greg.golf.util.GolfPostgresqlContainer;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class LeagueServiceTest {

    @SuppressWarnings("unused")
    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @SuppressWarnings("unused")
    @MockitoBean
    private EmailServiceImpl emailService;

    @ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
            .getInstance();

    private static Player player;

    @SuppressWarnings("unused")
    @Autowired
    private LeagueService leagueService;

    @BeforeAll
    public static void setup(@Autowired PlayerService playerService) {

        if (player == null) {
            player = playerService.getPlayer(1L).orElseThrow();
        }

        log.info("Set up completed");
    }

    @DisplayName("Should add the new league")
    @Transactional
    @Test
    void addLeagueTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);

        assertNotNull(league.getId());
    }

    @DisplayName("Get all leagues")
    @Transactional
    @Test
    void getAllLeaguesTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);

        assertEquals(1, leagueService.findAllLeaguesPageable(0).size());
        assertEquals("Test league", leagueService.findAllLeaguesPageable(0).get(0).getName());

    }

    @DisplayName("Should not add league player by unauthorized user")
    @Transactional
    @Test
    void addLeaguePlayerByUnauthorizedUserTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setNick("Greg");
        leaguePlayer.setPlayerId(1L);
        leagueService.addLeague(league);
        leaguePlayer.setLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("2", "fake", authorities));

        assertThrows(UnauthorizedException.class, () -> this.leagueService.addPlayer(leaguePlayer));
    }

    @DisplayName("Should not add league player to close league")
    @Transactional
    @Test
    void addLeaguePlayerForClosedLeagueTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_CLOSE);
        league.setName("Test league");
        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setNick("Greg");
        leaguePlayer.setPlayerId(1L);
        leagueService.addLeague(league);
        leaguePlayer.setLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        assertThrows(LeagueClosedException.class, () -> this.leagueService.addPlayer(leaguePlayer));
    }

    @DisplayName("Should not add league player to close league")
    @Transactional
    @Test
    void addLeaguePlayerForNonExistingPlayerTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);
        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setNick("Greg");
        leaguePlayer.setLeague(league);
        leaguePlayer.setPlayerId(2L);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        assertThrows(NoSuchElementException.class, () -> this.leagueService.addPlayer(leaguePlayer));
    }

    @DisplayName("Should add league player to league")
    @Transactional
    @Test
    void addLeaguePlayerForLeagueTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);
        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setNick("Greg");
        leaguePlayer.setLeague(league);
        leaguePlayer.setPlayerId(1L);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        leagueService.addPlayer(leaguePlayer);

        assertNotNull(leaguePlayer.getId());

        assertThrows(DuplicatePlayerInLeagueException.class, () -> this.leagueService.addPlayer(leaguePlayer));
    }

    @DisplayName("Should not delete players for non existing league")
    @Transactional
    @Test
    void deletePlayersForNonExistingLeagueTest() {

        assertThrows(NoSuchElementException.class, () -> this.leagueService.deletePlayer(1L, 1L));

    }

    @DisplayName("Should not delete players by unauthorized user")
    @Transactional
    @Test
    void deletePlayerByUnauthorizedUserTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("2", "fake", authorities));

        var leagueId = league.getId();
        assertThrows(UnauthorizedException.class, () -> this.leagueService.deletePlayer(leagueId, 1L));

    }

    @DisplayName("Should not delete players for closed league")
    @Transactional
    @Test
    void deletePlayerForClosedLeagueTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_CLOSE);
        league.setName("Test league");
        leagueService.addLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        var leagueId = league.getId();
        assertThrows(LeagueClosedException.class, () -> this.leagueService.deletePlayer(leagueId, 1L));

    }

    @DisplayName("Should not delete players that has match")
    @Transactional
    @Test
    void deletePlayerWithMatchesTest(@Autowired LeagueMatchRepository leagueMatchRepository) {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);
        var leagueMatch = new LeagueMatch();
        leagueMatch.setLooserId(1L);
        leagueMatch.setWinnerId(1L);
        leagueMatch.setResult("A/S");
        leagueMatch.setLeague(league);
        var matches = new ArrayList<LeagueMatch>();
        matches.add(leagueMatch);
        league.setLeagueMatches(matches);
        leagueMatchRepository.save(leagueMatch);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        var leagueId = league.getId();
        assertThrows(PlayerHasMatchException.class, () -> this.leagueService.deletePlayer(leagueId, 1L));

    }

    @DisplayName("Should delete player")
    @Transactional
    @Test
    void deletePlayerTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);
        var matches = new ArrayList<LeagueMatch>();
        league.setLeagueMatches(matches);

        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(1L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Greg");
        var players = new ArrayList<LeaguePlayer>();
        players.add(leaguePlayer);
        league.setLeaguePlayers(players);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        var leagueId = league.getId();
        leagueService.deletePlayer(leagueId, 1L);

        assertEquals(0, leagueService.getLeaguePlayers(leagueId).size());
    }

    @DisplayName("Should not close league by unauthorized user")
    @Transactional
    @Test
    void closeLeagueByUnauthorizedUserTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("2", "fake", authorities));

        var leagueId = league.getId();
        assertThrows(UnauthorizedException.class, () -> this.leagueService.closeLeague(leagueId));
    }

    @DisplayName("Should not close league that has been already closed")
    @Transactional
    @Test
    void closeAlreadyClosedLeagueTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_CLOSE);
        league.setName("Test league");
        leagueService.addLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        var leagueId = league.getId();
        assertThrows(LeagueClosedException.class, () -> this.leagueService.closeLeague(leagueId));
    }

    @DisplayName("Should close league")
    @Transactional
    @Test
    void closeLeagueTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        this.leagueService.closeLeague(league.getId());

        assertTrue(this.leagueService.findAllLeaguesPageable(0).get(0).getStatus());
    }

    @DisplayName("Should not add match by unauthorized user")
    @Transactional
    @Test
    void addMatchByUnauthorizedUserTest(@Autowired LeagueRepository leagueRepository, @Autowired LeaguePlayerRepository leaguePlayerRepository) {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueRepository.save(league);

        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(1L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Greg");
        leaguePlayerRepository.save(leaguePlayer);

        leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(2L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Test");
        leaguePlayerRepository.save(leaguePlayer);

        var leagueMatch = new LeagueMatch();
        leagueMatch.setLooserId(1L);
        leagueMatch.setWinnerId(2L);
        leagueMatch.setResult("A/S");
        leagueMatch.setLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("2", "fake", authorities));

        assertThrows(UnauthorizedException.class, () -> this.leagueService.addMatch(leagueMatch));
    }

    @DisplayName("Should add match to the league")
    @Transactional
    @Test
    void addMatchTest(@Autowired LeagueRepository leagueRepository, @Autowired LeaguePlayerRepository leaguePlayerRepository) {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueRepository.save(league);

        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(1L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Greg");
        leaguePlayerRepository.save(leaguePlayer);

        leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(2L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Test");
        leaguePlayerRepository.save(leaguePlayer);

        var leagueMatch = new LeagueMatch();
        leagueMatch.setLooserId(1L);
        leagueMatch.setWinnerId(2L);
        leagueMatch.setResult("A/S");
        leagueMatch.setLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        leagueService.addMatch(leagueMatch);

        var leagueId = league.getId();
        assertEquals(1, this.leagueService.getMatches(leagueId).size());

        assertThrows(DuplicateMatchInLeagueException.class, () -> this.leagueService.addMatch(leagueMatch));
    }

    @DisplayName("Should add match to the closed league")
    @Transactional
    @Test
    void addMatchToClosedLeagueTest(@Autowired LeagueRepository leagueRepository, @Autowired LeaguePlayerRepository leaguePlayerRepository) {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_CLOSE);
        league.setName("Test league");
        leagueRepository.save(league);

        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(1L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Greg");
        leaguePlayerRepository.save(leaguePlayer);

        leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(2L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Test");
        leaguePlayerRepository.save(leaguePlayer);

        var leagueMatch = new LeagueMatch();
        leagueMatch.setLooserId(1L);
        leagueMatch.setWinnerId(2L);
        leagueMatch.setResult("A/S");
        leagueMatch.setLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        assertThrows(LeagueClosedException.class, () -> this.leagueService.addMatch(leagueMatch));
    }

    @DisplayName("Should not add match to the league if player is not league participant")
    @Transactional
    @Test
    void addMatchToLeagueWithWrongPlayerTest(@Autowired LeagueRepository leagueRepository, @Autowired LeaguePlayerRepository leaguePlayerRepository) {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueRepository.save(league);

        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(1L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Greg");
        leaguePlayerRepository.save(leaguePlayer);

        var leagueMatch = new LeagueMatch();
        leagueMatch.setLooserId(1L);
        leagueMatch.setWinnerId(2L);
        leagueMatch.setResult("A/S");
        leagueMatch.setLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        assertThrows(MatchResultForNotLeaguePlayerException.class, () -> this.leagueService.addMatch(leagueMatch));
    }

    @DisplayName("Should delete match")
    @Transactional
    @Test
    void deleteMatchTest(@Autowired LeagueRepository leagueRepository, @Autowired LeaguePlayerRepository leaguePlayerRepository) {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueRepository.save(league);

        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(1L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Greg");
        leaguePlayerRepository.save(leaguePlayer);

        leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(2L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Test");
        leaguePlayerRepository.save(leaguePlayer);

        var leagueMatch = new LeagueMatch();
        leagueMatch.setLooserId(1L);
        leagueMatch.setWinnerId(2L);
        leagueMatch.setResult("A/S");
        leagueMatch.setLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        leagueService.addMatch(leagueMatch);

        var leagueId = league.getId();
        assertEquals(1, leagueService.getMatches(leagueId).size());

        leagueService.deleteMatch(league.getId(), 2L, 1L);

        assertEquals(0, leagueService.getMatches(leagueId).size());

    }

    @DisplayName("Should not delete match for unauthorized user")
    @Transactional
    @Test
    void deleteMatchByUnauthorizedUserTest(@Autowired LeagueRepository leagueRepository, @Autowired LeaguePlayerRepository leaguePlayerRepository) {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueRepository.save(league);

        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(1L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Greg");
        leaguePlayerRepository.save(leaguePlayer);

        leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(2L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Test");
        leaguePlayerRepository.save(leaguePlayer);

        var leagueMatch = new LeagueMatch();
        leagueMatch.setLooserId(1L);
        leagueMatch.setWinnerId(2L);
        leagueMatch.setResult("A/S");
        leagueMatch.setLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("2", "fake", authorities));

        var leagueId = league.getId();
        assertThrows(UnauthorizedException.class, () -> this.leagueService.deleteMatch(leagueId, 2L, 1L));

    }

    @DisplayName("Should not delete match for closed league")
    @Transactional
    @Test
    void deleteMatchForClosedLeagueTest(@Autowired LeagueRepository leagueRepository, @Autowired LeaguePlayerRepository leaguePlayerRepository) {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_CLOSE);
        league.setName("Test league");
        leagueRepository.save(league);

        var leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(1L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Greg");
        leaguePlayerRepository.save(leaguePlayer);

        leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(2L);
        leaguePlayer.setLeague(league);
        leaguePlayer.setNick("Test");
        leaguePlayerRepository.save(leaguePlayer);

        var leagueMatch = new LeagueMatch();
        leagueMatch.setLooserId(1L);
        leagueMatch.setWinnerId(2L);
        leagueMatch.setResult("A/S");
        leagueMatch.setLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        var leagueId = league.getId();
        assertThrows(LeagueClosedException.class, () -> this.leagueService.deleteMatch(leagueId, 2L, 1L));

    }

    @DisplayName("Should delete league")
    @Transactional
    @Test
    void deleteLeagueTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));

        leagueService.deleteLeague(league.getId());

        assertEquals(0, leagueService.findAllLeaguesPageable(0).size());

    }

    @DisplayName("Should not delete league by unauthorized user")
    @Transactional
    @Test
    void deleteLeagueByUnauthorizedUserTest() {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("2", "fake", authorities));

        var leagueId = league.getId();
        assertThrows(UnauthorizedException.class, () -> this.leagueService.deleteLeague(leagueId));

    }

    @DisplayName("Add notification for opened league but player does not have mail set")
    @Transactional
    @Test
    void attemptToAddNotificationForOpenedTournamentButNoEmailSetTest(@Autowired LeagueRepository leagueRepository,
                                                                      @Autowired PlayerService playerService,
                                                                      @Autowired TournamentNotificationRepository tournamentNotificationRepository) {

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);

        player = playerService.getPlayer(1L).orElseThrow();

        UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);


        var league2 = leagueRepository.findAll().get(0);

        Long id = league2.getId();
        assertThrows(MailNotSetException.class, () -> leagueService.addNotification(id));
    }

    @DisplayName("Add notification for opened league")
    @Transactional
    @Test
    void attemptToAddNotificationForOpenLeagueTest(@Autowired LeagueRepository leagueRepository,
                                                         @Autowired PlayerService playerService,
                                                         @Autowired PlayerRepository playerRepository,
                                                         @Autowired LeagueNotificationRepository leagueNotificationRepository) {


        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);

        player = playerService.getPlayer(1L).orElseThrow();
        try {
            player.setEmail(StringUtility.encryptString("test@gmail.com", "testPassword"));
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
        playerRepository.save(player);

        UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);


        var leagueId = leagueRepository.findAll().get(0).getId();
        leagueService.addNotification(leagueId);

        assertEquals(1, leagueNotificationRepository.findAll().size());

        //attempt to add notification the second time
        assertThrows(DuplicateNotificationException.class, () -> leagueService.addNotification(leagueId));
    }

    @DisplayName("Send notification test")
    @Transactional
    @Test
    void attemptToSendNotificationTest(@Autowired LeagueRepository LeagueRepository,
                                       @Autowired PlayerService playerService,
                                       @Autowired LeagueNotificationRepository leagueNotificationRepository) {

        player = playerService.getPlayer(1L).orElseThrow();

        UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        var league = new League();
        league.setPlayer(player);
        league.setStatus(League.STATUS_OPEN);
        league.setName("Test league");
        leagueService.addLeague(league);
        var leagueId = league.getId();

        var leagueResultDto = new LeagueResultDto();
        leagueResultDto.setNick("Test");
        leagueResultDto.setBig(1);
        leagueResultDto.setSmall(1);
        leagueResultDto.setMatchesPlayed(1);

        // attempt to send notification but no notification is defined
        assertEquals(0, leagueService.processNotifications(leagueId, new LeagueResultDto[]{leagueResultDto}));

        player.setEmail("grzegorz.malewicz@gmail.com");
        playerService.update(player);

        // create notification
        leagueService.addNotification(leagueId);

        try {
            doNothing().when(emailService).sendEmail(any(), any(), any());
        } catch (Exception e) {
            fail("Method emailService.sendMail throws exception");
        }

        assertDoesNotThrow(() -> leagueService.processNotifications(leagueId, new LeagueResultDto[]{leagueResultDto}));

        try {
            doThrow(MessagingException.class).when(emailService).sendEmail(any(), any(), any());
        } catch (Exception e) {
            fail("Method emailService.sendMail throws exception");
        }

        // verify if exception is caught
        assertThrows(GeneralException.class, () -> leagueService.processNotifications(leagueId, new LeagueResultDto[]{leagueResultDto}));

        //remove notification
        leagueService.removeNotification(leagueId);

        assertEquals(0, leagueNotificationRepository.findByLeagueId(leagueId).size());
    }

    @AfterAll
    public static void done() {

        log.info("Clean up completed");

    }

}
