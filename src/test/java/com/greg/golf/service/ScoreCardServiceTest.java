package com.greg.golf.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import com.greg.golf.security.JwtRequestFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.util.GolfPostgresqlContainer;

@Slf4j
@SpringBootTest
class ScoreCardServiceTest {

	@SuppressWarnings("unused")
	@MockBean
	private JwtRequestFilter jwtRequestFilter;

	@ClassRule
	public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
			.getInstance();

	private static Long roundId;

	@Autowired
	private ScoreCardService scoreCardService;

	@BeforeAll
	public static void setup(@Autowired PlayerService playerService, @Autowired CourseService courseService,
			@Autowired RoundRepository roundRepository, @Autowired PlayerRoundRepository playerRoundRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();

		var round = new Round();

		var course = courseService.getCourse(1L).orElseThrow();
		round.setCourse(course);
		var playerSet = new TreeSet<Player>();
		playerSet.add(player);
		round.setPlayer(playerSet);
		round.setMatchPlay(false);
		var calendar = new GregorianCalendar();
		calendar.set(2020, Calendar.JUNE, 12);
		round.setRoundDate(calendar.getTime());
		round.setScoreCard(new ArrayList<>());
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
		playerRoundRepository.updatePlayerRoundInfo(player.getWhs(), 1, 1F, 2L, 1, player.getId(), round.getId());

		log.info("Set up completed");
	}

	@DisplayName("Get scorcards")
	@Transactional
	@Test
	void getScoreCardsTest(@Autowired RoundRepository roundRepository) {
		
		var round = roundRepository.findById(roundId).orElseThrow();
		
		var scoreCardLst =  scoreCardService.listByRound(round);
		
		Assertions.assertEquals(2, scoreCardLst.size());
	}
	
	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository) {

		roundRepository.deleteAll();
		log.info("Clean up completed");

	}

}
