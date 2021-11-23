package com.greg.golf.repository;

import java.util.Date;
import java.util.Optional;

import org.junit.ClassRule;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.Tournament;
import com.greg.golf.entity.TournamentResult;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.repository.TournamentResultRepository;
import com.greg.golf.service.PlayerService;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class TournamentResultRepositoryTest {
	
	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

	private static Tournament tournament;

	@Autowired
	private TournamentResultRepository tournamentResultRepository;

	@Autowired
	private PlayerRepository playerRepository;

	@BeforeAll
	public static void setup(@Autowired TournamentRepository tournamentRepository, @Autowired PlayerService ps) {
		
		Player player = ps.getPlayer(1L).orElseThrow();

		tournament = new Tournament();
		tournament.setName("Test tournament");
		tournament.setStartDate(new Date(1));
		tournament.setEndDate(new Date(1));
		tournament.setPlayer(player);
		tournament = tournamentRepository.save(tournament);
		log.info("Set up completed");
	}

	@DisplayName("Add tournament result")
	@Transactional
	@Test
	void addToturnmentResultTest() {

		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);

		Player player = playerRepository.findById(1L).orElseThrow();
		tournamentResult.setPlayer(player);

		tournamentResult.setTournament(tournament);

		tournamentResult = tournamentResultRepository.save(tournamentResult);
		Assertions.assertNotNull(tournamentResult.getId(), "Id should not be null");
	}

	
	@DisplayName("Get tournament result")
	@Transactional
	@Test
	void getToturnmentResultById() {

		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);

		Player player = playerRepository.findById(1L).orElseThrow();
		tournamentResult.setPlayer(player);

		tournamentResult.setTournament(tournament);

		tournamentResult = tournamentResultRepository.save(tournamentResult);
		Assertions.assertNotNull(
				tournamentResultRepository.findById(tournamentResult.getId()), "Tournament result should not be null");
	}
	
	@DisplayName("Find all amd sort by played rounds and strokes netto")
	@Transactional
	@Test
	void findByTournamentByOrderByPlayedRoundsAscByStrokesNettoAsc() {

		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);

		Player player = playerRepository.findById(1L).orElseThrow();
		tournamentResult.setPlayer(player);

		tournamentResult.setTournament(tournament);

		tournamentResultRepository.save(tournamentResult);
		Assertions.assertEquals(1,
				tournamentResultRepository.findByTournamentOrderByPlayedRoundsDescStbNetDesc(tournament).size());
	}

	@DisplayName("Delete tournament")
	@Transactional
	@Test
	void deleteToturnmentById() {

		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);

		Player player = playerRepository.findById(1L).orElseThrow();
		tournamentResult.setPlayer(player);

		tournamentResult.setTournament(tournament);

		tournamentResult = tournamentResultRepository.save(tournamentResult);

		tournamentResultRepository.deleteById(tournamentResult.getId());
		Assertions.assertTrue(
				tournamentResultRepository.findById(tournamentResult.getId()).isEmpty(), "Tournament result should be null");
	}
	
	@DisplayName("Find by player and tournament")
	@Transactional
	@Test
	void getToturnmentResultByPlayerAndTournament() {

		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);

		Player player = playerRepository.findById(1L).orElseThrow();
		tournamentResult.setPlayer(player);

		tournamentResult.setTournament(tournament);

		Optional<TournamentResult> tournamentResultOpt = tournamentResultRepository.findByPlayerAndTournament(player, tournament);
		Assertions.assertTrue(tournamentResultOpt.isEmpty(), "Tournament result should not be null");
	}

	@AfterAll
	public static void done(@Autowired TournamentRepository tournamentRepository) {
		tournamentRepository.deleteAll();
		log.info("Clean up completed");

	}
}
