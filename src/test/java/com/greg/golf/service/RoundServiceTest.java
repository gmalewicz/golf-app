package com.greg.golf.service;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.PlayerRound;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.entity.Tournament;
import com.greg.golf.error.PlayerAlreadyHasThatRoundException;
import com.greg.golf.error.TooFewHolesForTournamentException;
import com.greg.golf.error.TooManyPlayersException;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class RoundServiceTest {

	@ClassRule
	public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
			.getInstance();

	private static Long roundId;

	@Autowired
	private RoundService roundService;

	@BeforeAll
	public static void setup(@Autowired PlayerService playerService, @Autowired CourseService courseService,
			@Autowired RoundRepository roundRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();

		var round = new Round();

		var course = courseService.getCourse(1L).orElseThrow();
		round.setCourse(course);
		var playerSet = new TreeSet<Player>();
		playerSet.add(player);
		round.setPlayer(playerSet);
		round.setMatchPlay(false);
		var calendar = new GregorianCalendar();
		calendar.set(2020, 5, 12);
		round.setRoundDate(calendar.getTime());
		round.setScoreCard(new ArrayList<ScoreCard>());
		var scoreCard = new ScoreCard();
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
		roundId = round.getId();

		log.info("Set up completed");
	}

	@DisplayName("Delete score card for nonexisting round")
	@Transactional
	@Test
	void deleteScoreCardForNonExistingRoundTest() {

		Assertions.assertThrows(NoSuchElementException.class, () -> {
			roundService.deleteScorecard(1L, 999L);
		});
	}

	@DisplayName("Delete score card for null argument")
	@Transactional
	@Test
	void deleteScoreCardForEmptyRoundTest() {

		Assertions.assertThrows(InvalidDataAccessApiUsageException.class, () -> {
			roundService.deleteScorecard(1L, null);
		});
	}

	@DisplayName("Delete score card for incorrect player")
	@Transactional
	@Test
	void deleteScoreCardWithIncorrectPlayerTest() {

		Assertions.assertThrows(NoSuchElementException.class, () -> {
			roundService.deleteScorecard(2L, roundId);
		});
	}

	@DisplayName("Delete score card for a round with only one player")
	@Transactional
	@Test
	void deleteScoreCardForRoundWithOnePlayerTest(@Autowired RoundRepository roundRepository) {

		roundService.deleteScorecard(1L, roundId);
		assertEquals(0, roundRepository.count());

	}

	@DisplayName("Delete score card for a round with two players")
	@Transactional
	@Test
	void deleteScoreCardForRoundWithTwoPlayerTest(@Autowired RoundRepository roundRepository,
			@Autowired PlayerRepository playerRepository) {

		var round = roundRepository.getOne(roundId);

		var player = new Player();
		player.setNick("player2");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		playerRepository.save(player);
		round.getPlayer().add(player);
		player.setRounds(new ArrayList<Round>());
		player.getRounds().add(round);
		roundRepository.save(round);

		var scoreCard = new ScoreCard();
		scoreCard.setHole(1);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setPlayer(player);
		scoreCard.setRound(round);
		scoreCard.setStroke(5);
		round.getScoreCard().add(scoreCard);
		roundRepository.save(round);

		roundService.deleteScorecard(player.getId(), round.getId());
		assertEquals(2, round.getScoreCard().size());

	}

	@DisplayName("Try to save the round with more than 4 players")
	@Transactional
	@Test
	void saveRoundWithMoreThan4PlayersTest(@Autowired RoundRepository roundRepository,
			@Autowired PlayerRepository playerRepository) {

		var round = roundRepository.getOne(roundId);

		var player = new Player();
		player.setNick("player2");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		playerRepository.save(player);
		round.getPlayer().add(player);
		player = new Player();
		player.setNick("player3");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		playerRepository.save(player);
		round.getPlayer().add(player);
		player = new Player();
		player.setNick("player4");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		playerRepository.save(player);
		round.getPlayer().add(player);
		roundRepository.save(round);
		// create the new player
		player = new Player();
		player.setNick("player5");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		playerRepository.save(player);

		var newRound = new Round();
		newRound.setCourse(round.getCourse());
		var playerSet = new TreeSet<Player>();
		playerSet.add(player);
		newRound.setPlayer(playerSet);
		newRound.setMatchPlay(false);
		newRound.setRoundDate(round.getRoundDate());
		newRound.setScoreCard(new ArrayList<ScoreCard>());

		Assertions.assertThrows(TooManyPlayersException.class, () -> {
			roundService.saveRound(newRound);
		});
	}

	@DisplayName("Try to save the round for the same player twice")
	@Transactional
	@Test
	void saveRoundForTheSamePlayerTwiceTest(@Autowired RoundRepository roundRepository,
			@Autowired PlayerRepository playerRepository) {

		var round = roundRepository.getOne(roundId);

		// create the player
		var player = new Player();
		player.setId(1L);
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);

		var newRound = new Round();
		newRound.setCourse(round.getCourse());
		var playerSet = new TreeSet<Player>();
		playerSet.add(player);
		newRound.setPlayer(playerSet);
		newRound.setMatchPlay(false);
		newRound.setRoundDate(round.getRoundDate());
		newRound.setScoreCard(new ArrayList<ScoreCard>());

		Assertions.assertThrows(PlayerAlreadyHasThatRoundException.class, () -> {
			roundService.saveRound(newRound);
		});
	}

	@DisplayName("Try to add scorecard to existing round")
	@Transactional
	@Test
	void addScorecardToExistingRoundTest(@Autowired RoundRepository roundRepository,
			@Autowired PlayerRepository playerRepository) {

		var round = roundRepository.getOne(roundId);

		// create the new player
		var player = new Player();
		player.setNick("player5");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		playerRepository.save(player);

		// creae the new round
		var newRound = new Round();
		newRound.setCourse(round.getCourse());
		var playerSet = new TreeSet<Player>();
		playerSet.add(player);
		newRound.setPlayer(playerSet);
		newRound.setMatchPlay(false);
		newRound.setRoundDate(round.getRoundDate());

		// create one score card and add it to the round
		var scoreCard = new ScoreCard();
		scoreCard.setHole(1);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setPlayer(player);
		// scoreCard.setRound(round);
		scoreCard.setStroke(5);
		newRound.setScoreCard(new ArrayList<ScoreCard>());
		newRound.getScoreCard().add(scoreCard);

		roundService.saveRound(newRound);

		assertEquals(2, roundRepository.findById(round.getId()).orElseThrow().getPlayer().size());
	}

	@DisplayName("Try to add scorecard to existing round with tournament")
	@Transactional
	@Test
	void addScorecardWithTournamentToExistingRoundTest(@Autowired RoundRepository roundRepository,
			@Autowired PlayerRepository playerRepository, @Autowired TournamentRepository tournamentRepository) {

		var round = roundRepository.getOne(roundId);

		var tournament = new Tournament();
		tournament.setEndDate(round.getRoundDate());
		tournament.setStartDate(round.getRoundDate());
		tournament.setName("Test tournament");
		tournament.setPlayer(round.getPlayer().first());
		tournamentRepository.save(tournament);

		round.setTournament(tournament);
		round = roundRepository.save(round);

		// create the new player
		var player = new Player();
		player.setNick("player5");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		playerRepository.save(player);

		// creae the new round
		var newRound = new Round();
		newRound.setCourse(round.getCourse());
		var playerSet = new TreeSet<Player>();
		playerSet.add(player);
		newRound.setPlayer(playerSet);
		newRound.setMatchPlay(false);
		newRound.setRoundDate(round.getRoundDate());

		// create one score card and add it to the round
		var scoreCard = new ScoreCard();
		scoreCard.setHole(1);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setPlayer(player);
		// scoreCard.setRound(round);
		scoreCard.setStroke(5);
		newRound.setScoreCard(new ArrayList<ScoreCard>());
		newRound.getScoreCard().add(scoreCard);

		Assertions.assertThrows(TooFewHolesForTournamentException.class, () -> {
			roundService.saveRound(newRound);
		});
	}

	@DisplayName("Get Round inside range applicable for tournamnet")
	@Transactional
	@Test
	void getForPlayerRoundDetailsTest() {

		List<PlayerRound> pr = roundService.getByRoundId(roundId);

		assertEquals(1, pr.size());

	}

	@DisplayName("Get Round inside range applicable for tournamnet")
	@Transactional
	@Test
	void getRoundInsideRangeApplicableTest() {

		var startDate = new GregorianCalendar();
		startDate.set(2020, 5, 11, 0, 0, 0);
		var endDate = new GregorianCalendar();
		endDate.set(2020, 06, 14, 0, 0, 0);

		var rounds = roundService.findByDates(startDate.getTime(), endDate.getTime());

		assertEquals(1, rounds.size());

	}

	@DisplayName("Round applicable for tournamnet not found")
	@Transactional
	@Test
	void getRoundInsideRangeNotApplicableTest() {

		var startDate = new GregorianCalendar();
		startDate.set(2020, 5, 13, 0, 0, 0);
		var endDate = new GregorianCalendar();
		endDate.set(2020, 06, 14, 0, 0, 0);

		var rounds = roundService.findByDates(startDate.getTime(), endDate.getTime());

		assertEquals(0, rounds.size());

	}

	@DisplayName("Get Round pageable")
	@Transactional
	@Test
	void getRoundPageableTest(@Autowired PlayerRepository playerRepository) {

		var player = playerRepository.findById(1L).orElseThrow();

		var rounds = roundService.listByPlayerPageable(player, 0);

		assertEquals(1, rounds.size());

	}

	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository) {

		roundRepository.deleteAll();
		log.info("Clean up completed");

	}

}
