package com.greg.golf.repository;

import java.util.Date;
import java.util.Optional;

import com.greg.golf.security.JwtRequestFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.Tournament;
import com.greg.golf.entity.TournamentResult;
import com.greg.golf.service.PlayerService;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class TournamentResultRepositoryTest {

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtRequestFilter jwtRequestFilter;
	
	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

	private static Tournament tournament;

	@SuppressWarnings("unused")
	@Autowired
	private TournamentResultRepository tournamentResultRepository;

	@SuppressWarnings("unused")
	@Autowired
	private PlayerRepository playerRepository;

	@BeforeAll
	static void setup(@Autowired TournamentRepository tournamentRepository, @Autowired PlayerService ps) {
		
		Player player = ps.getPlayer(1L).orElseThrow();

		tournament = new Tournament();
		tournament.setName("Test tournament");
		tournament.setStartDate(new Date(1));
		tournament.setEndDate(new Date(1));
		tournament.setPlayer(player);
		tournament.setBestRounds(0);
		tournament.setStatus(Tournament.STATUS_OPEN);
		tournament.setPlayHcpMultiplayer(1F);
		tournament.setMaxPlayHcp(54);
		tournament.setCanUpdateHcp(true);
		tournament = tournamentRepository.save(tournament);
		log.info("Set up completed");
	}

	@DisplayName("Add tournament result")
	@Transactional
	@Test
	void addTournamentResultTest() {

		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setStrokeRounds(1);

		Player player = playerRepository.findById(1L).orElseThrow();
		tournamentResult.setPlayer(player);


		tournamentResult.setTournament(tournament);

		tournamentResult = tournamentResultRepository.save(tournamentResult);
		Assertions.assertNotNull(tournamentResult.getId(), "Id should not be null");
	}

	
	@DisplayName("Get tournament result")
	@Transactional
	@Test
	void getTournamentResultById() {

		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setStrokeRounds(1);

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
		tournamentResult.setStrokeRounds(1);

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
	void deleteTournamentById() {

		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setStrokeRounds(1);

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
	void getTournamentResultByPlayerAndTournament() {

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
	static void done(@Autowired TournamentRepository tournamentRepository) {
		tournamentRepository.deleteAll();
		log.info("Clean up completed");

	}
}
