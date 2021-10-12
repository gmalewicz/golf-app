package com.greg.golf.service;

import com.greg.golf.entity.Cycle;
import com.greg.golf.entity.CycleTournament;
import com.greg.golf.entity.Player;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class CycleServiceTest {

	@ClassRule
	public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
			.getInstance();

	private static Player player;

	@Autowired
	private CycleService cycleService;

	@BeforeAll
	public static void setup(@Autowired PlayerService playerService) {

		player = playerService.getPlayer(1L).orElseThrow();

		log.info("Set up completed");
	}

	@DisplayName("Should add the new cycle")
	@Transactional
	@Test
	void addCycleTest() {

		var cycle = new Cycle();
		cycle.setName("Test cycle");
		cycle.setStatus(Cycle.STATUS_OPEN);
		cycle.setPlayer(player);
		cycle.setRule(Cycle.RULE_STANDARD);
		cycle = cycleService.addCycle(cycle);

		assertNotNull(cycle.getId());
	}

	@DisplayName("Should add the new cycle tournament")
	@Transactional
	@Test
	void addCycleTournamentTest() {

		var cycle = new Cycle();
		cycle.setName("Test cycle");
		cycle.setStatus(Cycle.STATUS_OPEN);
		cycle.setPlayer(player);
		cycle.setRule(Cycle.RULE_STANDARD);
		cycle = cycleService.addCycle(cycle);

		var cycleTournament = new CycleTournament();
		cycleTournament.setName("Test cycle tournament");
		cycleTournament.setBestOf(false);
		cycleTournament.setRounds(1);
		cycleTournament.setCycle(cycle);
		cycleTournament = cycleService.addCycleTournament(cycleTournament);

		assertNotNull(cycleTournament.getId());
	}

	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
