package com.greg.golf.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import com.greg.golf.security.JwtRequestFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.PlayerRound;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.entity.Tournament;
import com.greg.golf.entity.TournamentResult;
import com.greg.golf.entity.TournamentRound;
import com.greg.golf.error.RoundAlreadyAddedToTournamentException;
import com.greg.golf.repository.HoleRepository;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.repository.TournamentResultRepository;
import com.greg.golf.service.events.RoundEvent;
import com.greg.golf.util.GolfPostgresqlContainer;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class TournamentServiceTest {

	@SuppressWarnings("unused")
	@MockBean
	private JwtRequestFilter jwtRequestFilter;

	@ClassRule
	public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
			.getInstance();

	private static Player player;
	private static Round round;
	private static Tournament tournament;

	@SuppressWarnings("unused")
	@Autowired
	private TournamentService tournamentService;

	@SuppressWarnings("unused")
	@Autowired
	TournamentResultRepository tournamentResultRepository;

	@BeforeAll
	public static void setup(@Autowired PlayerService playerService, @Autowired CourseService courseService,
			@Autowired RoundRepository roundRepository, @Autowired PlayerRoundRepository playerRoundRepository,
			@Autowired TournamentRepository tournamentRepository) {

		player = playerService.getPlayer(1L).orElseThrow();

		round = new Round();

		var course = courseService.getCourse(1L).orElseThrow();
		round.setCourse(course);
		var playerSet = new TreeSet<Player>();
		playerSet.add(player);
		round.setPlayer(playerSet);
		round.setRoundDate(new Date(1));
		round.setMatchPlay(false);
		round.setScoreCard(new ArrayList<>());

		for (var i = 0; i < 18; i++) {
			var scoreCard = new ScoreCard();
			scoreCard.setHole(i + 1);
			scoreCard.setPats(0);
			scoreCard.setPenalty(0);
			scoreCard.setPlayer(player);
			scoreCard.setRound(round);
			scoreCard.setStroke(5);
			scoreCard.setHcp(2);
			round.getScoreCard().add(scoreCard);
		}
		round = roundRepository.save(round);

		playerRoundRepository.updatePlayerRoundInfo(player.getWhs(), 135, 70.3f, 2L, 0, player.getId(), round.getId());

		tournament = new Tournament();
		tournament.setEndDate(new Date(1));
		tournament.setStartDate(new Date(1));
		tournament.setName("Test Cup");
		tournament.setPlayer(player);
		tournament = tournamentRepository.save(tournament);

		log.info("Set up completed");
	}

	@DisplayName("Calculate net and gross STB")
	@Transactional
	@Test
	void calculateNetAndGrossSTBTest(@Autowired RoundRepository roundRepository) {

		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);

		var redRound = roundRepository.findById(round.getId()).orElseThrow();
		tournamentService.updateSTB(tournamentResult, redRound, null, player);

		log.info("STB netto: " + tournamentResult.getStbNet());
		log.info("STB gross: " + tournamentResult.getStbGross());
		Assertions.assertEquals(62, tournamentResult.getStbNet().intValue());
		Assertions.assertEquals(17, tournamentResult.getStbGross().intValue());
	}

	@DisplayName("Should add the new tournamnet")
	@Transactional
	@Test
	void addTheNewTournamnetTest() {

		var tournament = new Tournament();
		tournament.setEndDate(new Date(1));
		tournament.setStartDate(new Date(1));
		tournament.setName("Test Cup2");
		tournament.setPlayer(player);
		tournament = tournamentService.addTournament(tournament);

		Assertions.assertNotNull(tournament.getId());
	}

	@DisplayName("Calculate corrected strokes")
	@Transactional
	@Test
	void calculateCorrectedStrokesTest(@Autowired HoleRepository holeRepository) {

		round.getCourse().setHoles(holeRepository.findByCourse(round.getCourse()));

		round.getScoreCard().get(0).setStroke(20);
		var correctedScore = tournamentService.getCorrectedStrokes(player, round);

		log.info("corrected Strokes: " + correctedScore);
		Assertions.assertEquals(93, correctedScore);
	}

	@DisplayName("Calculate score differential")
	@Transactional
	@Test
	void calculateScoreDifferentialTest(@Autowired HoleRepository holeRepository) {

		round.getCourse().setHoles(holeRepository.findByCourse(round.getCourse()));
		round.getScoreCard().get(0).setStroke(20);

		var playerRound = new PlayerRound();
		playerRound.setSr(113);
		playerRound.setCr((float) 0);

		var scoreDifferential = tournamentService.getScoreDifferential(playerRound, round, player);

		log.info("score differential: " + scoreDifferential);
		Assertions.assertEquals(93, (int) scoreDifferential);
	}

	@DisplayName("Should save tournament round")
	@Transactional
	@Test
	void saveTournamentRoundTest() {

		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);

		TournamentRound tournamentRound =
				tournamentService.addTournamentRound(1, 1, 1, 1, 1, "test", tournamentResult, false);

		Assertions.assertNotNull(tournamentRound.getId());
	}

	@DisplayName("Get all tournaments")
	@Transactional
	@Test
	void getAllTournamentsTest() {

		Assertions.assertEquals(1, tournamentService.findAllTournaments().size());

	}

	@DisplayName("Calculate gross strokes")
	@Transactional
	@Test
	void getGrossStrokesTest() {

		var grossStrokes = tournamentService.getGrossStrokes(player, round);

		Assertions.assertEquals(90, grossStrokes);

	}

	@DisplayName("Calculate net strokes")
	@Transactional
	@Test
	void getNetStrokesTest() {

		var netStrokes = tournamentService.getNetStrokes(player, round, 99, null);

		Assertions.assertEquals(54, netStrokes);

	}

	@DisplayName("Calculate net strokes where net strokes is lower than 0")
	@Transactional
	@Test
	void getNetStrokesLowerThan0Test() {

		var netStrokes = tournamentService.getNetStrokes(player, round, 22, null);

		Assertions.assertEquals(0, netStrokes);

	}

	@DisplayName("Should add round to tournament")
	@Transactional
	@Test
	void addRoundTest() {

		var t = tournamentService.addRound(tournament.getId(), round.getId(), false);
		Assertions.assertEquals(t.getRound().get(0).getId(), round.getId());

	}

	@DisplayName("Should not allow add the same round to tournament twice")
	@Transactional
	@Test
	void addTesSameRoundTwiceTest() {

		tournamentService.addRound(tournament.getId(), round.getId(), false);
		Long tournamentId = tournament.getId();
		Long roundId = round.getId();
		assertThrows(RoundAlreadyAddedToTournamentException.class,
				() -> tournamentService.addRound(tournamentId, roundId, false));

	}

	@DisplayName("Adding the new round to the tournamnet result")
	@Transactional
	@Test
	void addNewRoundToTournamnetResultTest(@Autowired RoundRepository roundRepository) {

		var redRound = roundRepository.findById(round.getId()).orElseThrow();
		redRound.setTournament(tournament);
		tournamentService.updateTournamentResult(redRound, tournament);
		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(90, tr.getStrokesBrutto().intValue());

	}

	@DisplayName("Should update the tournamnet result with the new round")
	@Transactional
	@Test
	void updateTournamnetResultWithNewRoundTest(@Autowired RoundRepository roundRepository) {

		var redRound = roundRepository.findById(round.getId()).orElseThrow();
		redRound.setTournament(tournament);
		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(100);
		tournamentResult.setStrokesBrutto(100);
		tournamentResult.setStrokesNetto(100);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);
		tournamentService.updateTournamentResult(redRound, tournament);
		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(190, tr.getStrokesBrutto().intValue());

	}

	@DisplayName("Should update the tournamnet result with updated round")
	@Transactional
	@Test
	void updateTournamnetResultWithUpdatedRoundTest(@Autowired RoundRepository roundRepository) {

		// get round
		var redRound = roundRepository.findById(round.getId()).orElseThrow();

		// add round to tournament
		redRound.setTournament(tournament);
		tournamentService.updateTournamentResult(redRound, tournament);

		// update the round which was already added to tournament
		redRound.getScoreCard().get(0).setStroke(15);
		roundRepository.save(redRound);

		// try to update the tournamnet
		var roundEvent = new RoundEvent(this, redRound);
		tournamentService.handleRoundEvent(roundEvent);

		Assertions.assertEquals(15, redRound.getScoreCard().get(0).getStroke().intValue());

		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(90, tr.getStrokesBrutto().intValue());

	}

	@DisplayName("Should add the the round to the tournamnet and update tournamnet result")
	@Transactional
	@Test
	void addTheNewRoundandUpdateTournamentResultTest(@Autowired RoundRepository roundRepository) {

		var t = tournamentService.addRound(tournament.getId(), round.getId(), true);
		Assertions.assertEquals(t.getRound().get(0).getId(), round.getId());

		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(90, tr.getStrokesBrutto().intValue());

	}

	@DisplayName("Should return tournamnt round for tournament result")
	@Transactional
	@Test
	void getTournamentRoundForTournamentResultTest(@Autowired RoundRepository roundRepository) {

		var redRound = roundRepository.findById(round.getId()).orElseThrow();
		redRound.setTournament(tournament);
		tournamentService.updateTournamentResult(redRound, tournament);

		var roundResults = tournamentResultRepository.findAll().get(0);

		tournamentService.getTournamentRoundsForResult(roundResults.getId());

		Assertions.assertEquals(1, tournamentService.getTournamentRoundsForResult(roundResults.getId()).size());

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
