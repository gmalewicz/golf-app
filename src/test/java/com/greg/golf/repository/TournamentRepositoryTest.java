package com.greg.golf.repository;

import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.Tournament;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.service.PlayerService;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TournamentRepositoryTest {

	private static Player player;

	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();
	
	@Autowired
	private TournamentRepository tournamentRepository;

	@BeforeAll
	public static void setup(@Autowired PlayerService ps) {
		
		player = ps.getPlayer(1L).orElseThrow();
	}
	
	@DisplayName("Add tournament")
	@Transactional
	@Test
	void addToturnmentTest() {

		Tournament tournament = new Tournament();
		tournament.setName("Test tournament");
		tournament.setStartDate(new Date(1));
		tournament.setEndDate(new Date(1));
		List<Round> roundList = new ArrayList<Round>();
		// roundList.add(round);
		tournament.setRound(roundList);
		tournament.setPlayer(player);
		tournament = tournamentRepository.save(tournament);
		assertNotNull("Id should not be null", tournament.getId());
	}

	@DisplayName("Get tournament")
	@Transactional
	@Test
	void getToturnmentById() {

		Tournament tournament = new Tournament();
		tournament.setName("Test tournament");
		tournament.setStartDate(new Date(1));
		tournament.setEndDate(new Date(1));
		List<Round> roundList = new ArrayList<Round>();
		// roundList.add(round);
		tournament.setRound(roundList);
		tournament.setPlayer(player);
		tournament = tournamentRepository.save(tournament);
		assertNotNull("Tournament should not be null", tournamentRepository.findById(tournament.getId()));
	}

	@DisplayName("Delete tournament")
	@Transactional
	@Test
	void deleteToturnmentById() {

		Tournament tournament = new Tournament();
		tournament.setName("Test tournament"); 
		tournament.setStartDate(new Date(1));
		tournament.setEndDate(new Date(1));
		List<Round> roundList = new ArrayList<Round>();
		// roundList.add(round);
		tournament.setRound(roundList);
		tournament.setPlayer(player);
		tournament = tournamentRepository.save(tournament);
		tournamentRepository.deleteById(tournament.getId());
		assertTrue("Tournament should be null", tournamentRepository.findById(tournament.getId()).isEmpty());
	}
/*
	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository) {
		roundRepository.deleteAll();
		log.info("Clean up completed");

	}
*/
}
