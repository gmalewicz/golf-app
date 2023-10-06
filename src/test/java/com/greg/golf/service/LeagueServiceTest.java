package com.greg.golf.service;

import com.greg.golf.entity.*;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.DuplicatePlayerInLeagueException;
import com.greg.golf.error.LeagueClosedException;
import com.greg.golf.error.PlayerHasMatchException;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.repository.LeagueMatchRepository;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.util.GolfPostgresqlContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class LeagueServiceTest {

    @SuppressWarnings("unused")
    @MockBean
    private JwtRequestFilter jwtRequestFilter;

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

        assertEquals(1, leagueService.findAllLeagues().size());
        assertEquals("Test league", leagueService.findAllLeagues().get(0).getName());

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

        assertThrows(UnauthorizedException.class, () -> this.leagueService.deletePlayer(league.getId(), 1L));

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

        assertThrows(LeagueClosedException.class, () -> this.leagueService.deletePlayer(league.getId(), 1L));

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

        assertThrows(PlayerHasMatchException.class, () -> this.leagueService.deletePlayer(league.getId(), 1L));

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
       // leaguePlayerRepository.save(leaguePlayer);
        //leagueService.addLeague(league);

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", "fake", authorities));


        leagueService.deletePlayer(league.getId(), 1L);

        assertEquals(0, leagueService.getLeaguePlayers(league.getId()).size());
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

        assertThrows(UnauthorizedException.class, () -> this.leagueService.closeLeague(league.getId()));
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

        assertThrows(LeagueClosedException.class, () -> this.leagueService.closeLeague(league.getId()));
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

        assertTrue(this.leagueService.findAllLeagues().get(0).getStatus());
    }

    @AfterAll
    public static void done() {

        log.info("Clean up completed");

    }

}
