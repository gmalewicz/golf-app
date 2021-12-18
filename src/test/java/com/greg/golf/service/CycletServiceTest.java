package com.greg.golf.service;

import com.greg.golf.controller.dto.EagleResultDto;
import com.greg.golf.entity.Cycle;
import com.greg.golf.entity.CycleTournament;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.UnauthorizedException;
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
class CycleServiceTest {

    @ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
            .getInstance();

    private static Cycle cycle;
    private static EagleResultDto eagleResultDto;

    @Autowired
    private CycleService cycleService;

    @BeforeAll
    public static void setup(@Autowired PlayerService playerService) {

        Player player = playerService.getPlayer(1L).orElseThrow();

        cycle = new Cycle();
        cycle.setName("Test cycle");
        cycle.setStatus(Cycle.STATUS_OPEN);
        cycle.setPlayer(player);
        cycle.setBestRounds(1);
        cycle.setMaxWhs(12.0F);

        eagleResultDto = new EagleResultDto();
        eagleResultDto.setR(new int[]{40, 0, 0, 0});
        eagleResultDto.setWhs(36.0F);
        eagleResultDto.setLastName("Bond");
        eagleResultDto.setFirstName("James");


        log.info("Set up completed");
    }

    @DisplayName("Should add the new cycle by authorized user")
    @Transactional
    @Test
    void addCycleByAuthorizedUserTest() {

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.ADMIN));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

        cycle = cycleService.addCycle(cycle);

        assertNotNull(cycle.getId());
    }

    @DisplayName("Should not add the new cycle by unauthorized user")
    @Transactional
    @Test
    void addCycleByUnauthorizedUserTest() {

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

        assertThrows(UnauthorizedException.class, () -> this.cycleService.addCycle(cycle));
    }

    @DisplayName("Should add the cycle tournament")
    @Transactional
    @ParameterizedTest
    @CsvSource({
            "false, 1, 36.0, 1",
            "false, 1, 11.0, 1",
            "false, 1, 11.0, 0",
            "true, 1, 11.0, 0",
            "false, 1, 11.0, 1",
    })
    void addCycleTournamentByAuthorizedUserTest(boolean bestOf, int rounds, float whs, int bestRounds) {

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.ADMIN));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

        cycle.setBestRounds(bestRounds);
        cycle = cycleService.addCycle(cycle);

        var cycleTournament = new CycleTournament();
        cycleTournament.setName("Test cycle tournament");
        cycleTournament.setBestOf(bestOf);
        cycleTournament.setRounds(rounds);
        cycleTournament.setCycle(cycle);

        eagleResultDto.setWhs(whs);

        cycleTournament = cycleService.addCycleTournament(cycleTournament, new EagleResultDto[]{eagleResultDto});

        assertNotNull(cycleTournament.getId());
    }

    @DisplayName("Should not add the cycle tournament by unauthorized user")
    @Transactional
    @Test
    void addCycleTournamentByUnauthorizedUserTest() {

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.ADMIN));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

        cycle.setBestRounds(1);
        cycle = cycleService.addCycle(cycle);

        authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

        var cycleTournament = new CycleTournament();
        cycleTournament.setName("Test cycle tournament");
        cycleTournament.setBestOf(false);
        cycleTournament.setRounds(1);
        cycleTournament.setCycle(cycle);

        eagleResultDto.setWhs(36.0F);

        assertThrows(UnauthorizedException.class, () -> cycleService.addCycleTournament(cycleTournament, new EagleResultDto[]{eagleResultDto}));

    }


    @DisplayName("Get all cycles")
    @Transactional
    @Test
    void getAllCyclesTest() {

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.ADMIN));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

        cycleService.addCycle(cycle);
        cycle.setName("Test cycle 2");
        cycleService.addCycle(cycle);

        assertEquals(2, cycleService.findAllCycles().size());
        assertEquals("Test cycle", cycleService.findAllCycles().get(1).getName());
        assertEquals("Test cycle 2", cycleService.findAllCycles().get(0).getName());

    }

    @DisplayName("Get all cycle tournaments")
    @Transactional
    @Test
    void getAllCycleTournamentTest() {

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.ADMIN));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

        cycle = cycleService.addCycle(cycle);

        var cycleTournament2 = new CycleTournament();
        cycleTournament2.setName("Test cycle tournament 2");
        cycleTournament2.setBestOf(false);
        cycleTournament2.setRounds(1);
        cycleTournament2.setCycle(cycle);
        cycleService.addCycleTournament(cycleTournament2, new EagleResultDto[]{eagleResultDto});

        var cycleTournament1 = new CycleTournament();
        cycleTournament1.setName("Test cycle tournament 1");
        cycleTournament1.setBestOf(false);
        cycleTournament1.setRounds(1);
        cycleTournament1.setCycle(cycle);
        cycleService.addCycleTournament(cycleTournament1, new EagleResultDto[]{eagleResultDto});

        var cycleTournaments = cycleService.findAllCycleTournaments(cycle.getId());

        assertEquals(2, cycleTournaments.size());

    }

    @DisplayName("Get all cycles")
    @Transactional
    @Test
    void getAllCycleResultsTest() {

        assertEquals(0, cycleService.findCycleResults(1L).size());
    }

    @DisplayName("Close cycle by authorized user")
    @Transactional
    @Test
    void closeCycleByAuthorizedUserTest() {

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.ADMIN));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

        cycle = cycleService.addCycle(cycle);

        cycleService.closeCycle(cycle.getId());

        cycleService.findAllCycles();

        assertEquals(Cycle.STATUS_CLOSE, cycleService.findAllCycles().get(0).getStatus());

    }

    @DisplayName("Attempt to close cycle by unauthorized user")
    @Transactional
    @Test
    void attemptToCloseCycleByUnauthorizedUserTest() {

        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Common.ADMIN));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

        Long cycleId = cycleService.addCycle(cycle).getId();

        authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

        assertThrows(UnauthorizedException.class, () -> this.cycleService.closeCycle(cycleId));
    }


    @AfterAll
    public static void done() {

        log.info("Clean up completed");

    }

}
