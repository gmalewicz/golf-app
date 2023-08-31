package com.greg.golf.service;

import com.greg.golf.controller.dto.EagleResultDto;
import com.greg.golf.entity.*;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.repository.CycleRepository;
import com.greg.golf.repository.CycleResultRepository;
import com.greg.golf.repository.CycleTournamentRepository;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.util.GolfPostgresqlContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @Autowired
    private LeagueService leagueService;

    @BeforeAll
    public static void setup(@Autowired PlayerService playerService) {

        Player player = playerService.getPlayer(1L).orElseThrow();

        league = new League();
        league.setName("Test league");
        league.setStatus(Cycle.STATUS_OPEN);
        league.setPlayer(player);

        log.info("Set up completed");
    }

    @DisplayName("Should add the new league")
    @Transactional
    @Test
    void addLeagueTest() {

        league = leagueService.addLeague(league);

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

    @AfterAll
    public static void done() {

        log.info("Clean up completed");

    }

}
