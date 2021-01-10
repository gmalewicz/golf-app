package com.greg.golf.controller;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.greg.golf.controller.GolfRESTController;
import com.greg.golf.entity.Course;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.entity.Tournament;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.repository.TournamentResultRepository;
import com.greg.golf.service.CourseService;
import com.greg.golf.service.PlayerService;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class GolfRESTControllerTest {

	private static Player player;
	private static Round round;
	private static Tournament tournament;
	// private static Course course;
	
	@Autowired TournamentResultRepository tournamentResultRepository;
	
	@Autowired
	private GolfRESTController golfRESTController;

	@BeforeAll
	public static void setup(@Autowired PlayerService playerService, @Autowired CourseService courseService,
			@Autowired RoundRepository roundRepository, @Autowired PlayerRoundRepository playerRoundRepository,
			@Autowired TournamentRepository tournamentRepository) {

		player = playerService.getPlayer(1L).orElseThrow();

		round = new Round();

		Course course = courseService.getCourse(1L).orElseThrow();
		round.setCourse(course);
		Set<Player> playerSet = new HashSet<Player>();
		playerSet.add(player);
		round.setPlayer(playerSet);
		round.setMatchPlay(false);
		round.setRoundDate(new Date(1));
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

		List<Tournament> tournaments = this.golfRESTController.getTournaments();
		assertEquals(tournaments.size(), 1);

	}
	
	@DisplayName("Gets all tournaments empty")
	@Transactional
	@Test
	void getTrournamentsWithEmptyTest(@Autowired TournamentRepository tournamentRepository) {

		tournamentRepository.deleteAll();
		List<Tournament> tournaments = this.golfRESTController.getTournaments();
		assertEquals(tournaments.size(), 0);

	}

	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository,
			@Autowired TournamentRepository tournamentRepository,
			@Autowired TournamentResultRepository tr) {
		
		roundRepository.deleteAll();
		tr.deleteAll();
		tournamentRepository.deleteAll();

		log.info("Clean up completed");

	}

}
