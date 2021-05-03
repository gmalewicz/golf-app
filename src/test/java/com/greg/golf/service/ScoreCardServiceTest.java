package com.greg.golf.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.util.GolfPostgresqlContainer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
class ScoreCardServiceTest {

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
		playerRoundRepository.updatePlayerRoundInfo(player.getWhs(), 1, 1F, 2L, 1, player.getId(), round.getId());

		log.info("Set up completed");
	}

	@DisplayName("Get scorcards")
	@Transactional
	@Test
	void getScoreCardsTest(@Autowired RoundRepository roundRepository) {
		
		var round = roundRepository.findById(roundId).orElseThrow();
		
		var scoreCardLst =  scoreCardService.listByRound(round);
		
		assertEquals(2, scoreCardLst.size());
	}
	
	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository) {

		roundRepository.deleteAll();
		log.info("Clean up completed");

	}

}
