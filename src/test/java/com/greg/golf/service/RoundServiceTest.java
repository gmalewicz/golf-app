package com.greg.golf.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import java.util.List;


import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.PlayerRound;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.entity.Tournament;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.repository.TournamentResultRepository;
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
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

	private static Player player;
	private static Round round;

	@Autowired
	private RoundService roundService;

	@Autowired
	TournamentResultRepository tournamentResultRepository;

	@Autowired
	TournamentRepository tournamentRepository;

	@Autowired
	RoundRepository roundRepository;

	@BeforeAll
	public static void setup(@Autowired PlayerService playerService, @Autowired CourseService courseService,
			@Autowired RoundRepository roundRepository) {

		player = playerService.getPlayer(1L).orElseThrow();

		round = new Round();

		Course course = courseService.getCourse(1L).orElseThrow();
		round.setCourse(course);
		List<Player> playerSet = new ArrayList<Player>();
		playerSet.add(player);
		round.setPlayer(playerSet);
		round.setMatchPlay(false);
		Calendar calendar = new GregorianCalendar();
		calendar.set(2020, 5, 12);
		round.setRoundDate(calendar.getTime());
		round.setScoreCard(new ArrayList<ScoreCard>());
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

		log.info("Set up completed");
	}
	
	@DisplayName("Get Round inside range applicable for tournamnet")
	@Transactional
	@Test
	void getForPlayerRoundDetailsTest() {

		List<PlayerRound> pr = roundService.getByRoundId(round.getId());

		assertEquals(1, pr.size());

	}
	

	@DisplayName("Get Round inside range applicable for tournamnet")
	@Transactional
	@Test
	void getRoundInsideRangeApplicableTest() {

		Calendar startDate = new GregorianCalendar();
		startDate.set(2020, 5, 11, 0, 0, 0);
		Calendar endDate = new GregorianCalendar();
		endDate.set(2020, 06, 14, 0, 0, 0);

		List<Round> rounds = roundService.findByDates(startDate.getTime(), endDate.getTime());

		assertEquals(1, rounds.size());

	}

	@DisplayName("Round applicable for tournamnet not found")
	@Transactional
	@Test
	void getRoundInsideRangeNotApplicableTest() {

		Calendar startDate = new GregorianCalendar();
		startDate.set(2020, 5, 11, 0, 0, 0);
		Calendar endDate = new GregorianCalendar();
		endDate.set(2020, 06, 14, 0, 0, 0);

		Tournament tournament = new Tournament();
		tournament.setEndDate(new Date(1));
		tournament.setStartDate(new Date(1));
		tournament.setName("Test Cup");
		tournament.setPlayer(player);
		tournament = tournamentRepository.save(tournament);
		round.setTournament(tournament);
		roundRepository.save(round);

		log.debug("------------------------ round tournament: " + round.getTournament());

		List<Round> rounds = roundService.findByDates(startDate.getTime(), endDate.getTime());

		log.debug("------------------------ round tournament: " + round.getTournament());

		assertEquals(0, rounds.size());

	}

	@DisplayName("Get Round pageable")
	@Transactional
	@Test
	void getRoundPageableTest() {

		List<Round> rounds = roundService.listByPlayerPageable(player, 0);

		assertEquals(1, rounds.size());

	}

	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository) {

		roundRepository.deleteAll();
		log.info("Clean up completed");

	}

}
