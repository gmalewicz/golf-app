package com.greg.golf.service;

import com.greg.golf.entity.Cycle;
import com.greg.golf.entity.CycleTournament;
import com.greg.golf.entity.Player;
import com.greg.golf.repository.CycleRepository;
import com.greg.golf.util.GolfPostgresqlContainer;
import lombok.extern.log4j.Log4j2;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class CycleServiceTest {

	@ClassRule
	public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
			.getInstance();

	private static Player player;
	private static Cycle cycle;

	@Autowired
	private CycleService cycleService;

	@BeforeAll
	public static void setup(@Autowired PlayerService playerService) {

		player = playerService.getPlayer(1L).orElseThrow();

		cycle = new Cycle();
		cycle.setName("Test cycle");
		cycle.setStatus(Cycle.STATUS_OPEN);
		cycle.setPlayer(player);

		log.info("Set up completed");
	}

	@DisplayName("Should add the new cycle")
	@Transactional
	@Test
	void addCycleTest() {

		cycle = cycleService.addCycle(cycle);

		assertNotNull(cycle.getId());
	}

	@DisplayName("Should add the new cycle tournament")
	@Transactional
	@Test
	void addCycleTournamentTest() {

		cycle = cycleService.addCycle(cycle);

		var cycleTournament = new CycleTournament();
		cycleTournament.setName("Test cycle tournament");
		cycleTournament.setBestOf(false);
		cycleTournament.setRounds(1);
		cycleTournament.setCycle(cycle);
		cycleTournament.setStartDate(new Date(1));
		cycleTournament = cycleService.addCycleTournament(cycleTournament, null);

		assertNotNull(cycleTournament.getId());
	}

	@DisplayName("Get all cycles")
	@Transactional
	@Test
	void getAllCyclesTest() {

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

		cycle = cycleService.addCycle(cycle);

		var cycleTournament2 = new CycleTournament();
		cycleTournament2.setName("Test cycle tournament 2");
		cycleTournament2.setBestOf(false);
		cycleTournament2.setRounds(1);
		cycleTournament2.setCycle(cycle);
		cycleTournament2.setStartDate(new Date(2));
		cycleService.addCycleTournament(cycleTournament2, null);

		var cycleTournament1 = new CycleTournament();
		cycleTournament1.setName("Test cycle tournament 1");
		cycleTournament1.setBestOf(false);
		cycleTournament1.setRounds(1);
		cycleTournament1.setCycle(cycle);
		cycleTournament1.setStartDate(new Date(1));
		cycleService.addCycleTournament(cycleTournament1, null);

		var cycleTournaments = cycleService.findAllCycleTournaments(cycle.getId());

		assertEquals(2, cycleTournaments.size());
		assertEquals((new Date(1)).getTime(), cycleTournaments.get(0).getStartDate().getTime());
		assertEquals((new Date(2)).getTime(), cycleTournaments.get(1).getStartDate().getTime());

	}

	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
