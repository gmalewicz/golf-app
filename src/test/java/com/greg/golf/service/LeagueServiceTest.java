package com.greg.golf.service;

import com.greg.golf.entity.*;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.DuplicatePlayerInLeagueException;
import com.greg.golf.error.LeagueClosedException;
import com.greg.golf.error.UnauthorizedException;
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

    private static League league;
    private static LeaguePlayer leaguePlayer;

    @SuppressWarnings("unused")
    @Autowired
    private LeagueService leagueService;

    @BeforeAll
    public static void setup(@Autowired PlayerService playerService) {

        Player player = playerService.getPlayer(1L).orElseThrow();

        league = new League();
        league.setName("Test league");
        league.setStatus(Cycle.STATUS_OPEN);
        league.setPlayer(player);

        leaguePlayer = new LeaguePlayer();
        leaguePlayer.setPlayerId(1L);
        leaguePlayer.setNick("Greg");

        log.info("Set up completed");
    }

    @DisplayName("Should add the new league")
    @Transactional
    @Test
    void addLeagueTest() {

        leagueService.addLeague(league);

        assertNotNull(league.getId());
    }

    @DisplayName("Get all leagues")
    @Transactional
    @Test
    void getAllLeaguesTest() {

        leagueService.addLeague(league);

        assertEquals(1, leagueService.findAllLeagues().size());
        assertEquals("Test league", leagueService.findAllLeagues().get(0).getName());

    }

    @DisplayName("Should not add league player by unauthorized user")
    @Transactional
    @Test
    void addLeaguePlayerByUnauthorizedUserTest() {

        league.setStatus(League.STATUS_OPEN);
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

        league.setStatus(League.STATUS_CLOSE);
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

        league.setStatus(League.STATUS_OPEN);
        leagueService.addLeague(league);
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

        league.setStatus(League.STATUS_OPEN);
        leagueService.addLeague(league);
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

    @AfterAll
    public static void done() {

        log.info("Clean up completed");

    }

}
