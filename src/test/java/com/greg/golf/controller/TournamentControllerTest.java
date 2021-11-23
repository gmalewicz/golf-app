package com.greg.golf.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import com.greg.golf.controller.dto.LimitedRoundWithPlayersDto;
import com.greg.golf.controller.dto.TournamentDto;
import com.greg.golf.controller.dto.TournamentResultDto;
import com.greg.golf.entity.Course;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.entity.Tournament;
import com.greg.golf.entity.TournamentResult;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.repository.TournamentResultRepository;
import com.greg.golf.service.CourseService;
import com.greg.golf.service.PlayerService;
import com.greg.golf.util.GolfPostgresqlContainer;

import lombok.extern.log4j.Log4j2;


@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class TournamentControllerTest {
	
	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

	private static Player player;
	private static Tournament tournament;

	private final TournamentController tournamentController;
	
	@Autowired
	public TournamentControllerTest(TournamentController tournamentController) {
		this.tournamentController = tournamentController;
	}

	@BeforeAll
	public static void setup(@Autowired PlayerService playerService, @Autowired CourseService courseService,
			@Autowired RoundRepository roundRepository, @Autowired PlayerRoundRepository playerRoundRepository,
			@Autowired TournamentRepository tournamentRepository) {

		player = playerService.getPlayer(1L).orElseThrow();

		Round round = new Round();

		Course course = courseService.getCourse(1L).orElseThrow();
		round.setCourse(course);
		SortedSet<Player> playerSet = new TreeSet<>();
		playerSet.add(player);
		round.setPlayer(playerSet);
		round.setMatchPlay(false);
		round.setRoundDate(new Date(1));
		round.setScoreCard(new ArrayList<>());
		ScoreCard scoreCard = new ScoreCard();
		scoreCard.setHole(1);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setPlayer(player);
		scoreCard.setRound(round);
		scoreCard.setStroke(5);
		round.getScoreCard().add(scoreCard);
		scoreCard = new ScoreCard();
		scoreCard.setHole(2);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setPlayer(player);
		scoreCard.setRound(round);
		scoreCard.setStroke(4);
		round.getScoreCard().add(scoreCard);
		round = roundRepository.save(round);
		playerRoundRepository.updatePlayerRoundInfo(player.getWhs(), 1, 1F, 2L, 1, player.getId(), round.getId());

		tournament = new Tournament();
		tournament.setEndDate(new Date(1));
		tournament.setStartDate(new Date(1));
		tournament.setName("Test Cup");
		tournament.setPlayer(player);
		tournament = tournamentRepository.save(tournament);

		log.info("Set up completed");
	}

	@DisplayName("Gets all tournaments not empty")
	@Transactional
	@Test
	void getTrournamentsTest() {

		List<TournamentDto> tournaments = this.tournamentController.getTournaments();
		Assertions.assertEquals(1, tournaments.size());

	}

	@DisplayName("Gets all tournaments empty")
	@Transactional
	@Test
	void getTrournamentsWithEmptyTest(@Autowired TournamentRepository tournamentRepository) {

		tournamentRepository.deleteAll();
		List<TournamentDto> tournaments = this.tournamentController.getTournaments();
		Assertions.assertEquals(0, tournaments.size());

	}
	
	@DisplayName("Get tournamnet results")
	@Transactional
	@Test
	void getTournamnetResultsTest(@Autowired TournamentResultRepository tournamentResultRepository) {
		
		TournamentResult tr = new TournamentResult();
		tr.setPlayedRounds(1);
		tr.setPlayer(player);
		tr.setStbGross(1);
		tr.setStbNet(1);
		tr.setStbNet(1);
		tr.setStrokesBrutto(1);
		tr.setStrokesNetto(1);
		tr.setTournament(tournament);
		tournamentResultRepository.save(tr);
		
		List<TournamentResultDto> trDtoLst =  this.tournamentController.getTournamentResult(tournament.getId());
		
		Assertions.assertEquals(1, trDtoLst.get(0).getStbNet().intValue());
	}
	
	@DisplayName("Get rounds for tournament")
	@Transactional
	@Test
	void getRoundsForTournamentTest() {
		
	
		List<LimitedRoundWithPlayersDto> rndDtoLst =  this.tournamentController.getTournamentRounds(tournament.getId());
		
		Assertions.assertEquals(1, rndDtoLst.size());
	}
	
	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository,
			@Autowired TournamentRepository tournamentRepository, @Autowired TournamentResultRepository tr) {

		roundRepository.deleteAll();
		tr.deleteAll();
		tournamentRepository.deleteAll();

		log.info("Clean up completed");

	}
}
