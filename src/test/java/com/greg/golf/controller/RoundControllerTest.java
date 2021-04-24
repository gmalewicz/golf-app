package com.greg.golf.controller;

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
import org.springframework.http.HttpStatus;

import com.greg.golf.controller.dto.CourseDto;
import com.greg.golf.controller.dto.CourseTeeDto;
import com.greg.golf.controller.dto.LimitedRoundDto;
import com.greg.golf.controller.dto.LimitedRoundWithPlayersDto;
import com.greg.golf.controller.dto.PlayerDto;
import com.greg.golf.controller.dto.PlayerRoundDto;
import com.greg.golf.controller.dto.RoundDto;
import com.greg.golf.controller.dto.ScoreCardDto;
import com.greg.golf.entity.Course;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.service.CourseService;
import com.greg.golf.service.PlayerService;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class RoundControllerTest {
	
	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

	private static Player player;
	private static Round round;

	private final RoundController roundController;
	
	@Autowired
	public RoundControllerTest(RoundController roundController) {
		this.roundController = roundController;
	}
	
	@BeforeAll
	public static void setup(@Autowired PlayerService playerService, @Autowired CourseService courseService,
			@Autowired RoundRepository roundRepository, @Autowired PlayerRoundRepository playerRoundRepository) {

		player = playerService.getPlayer(1L).orElseThrow();

		round = new Round();

		Course course = courseService.getCourse(1L).orElseThrow();
		round.setCourse(course);
		List<Player> playerSet = new ArrayList<Player>();
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

	
		log.info("Set up completed");
	}



	
	@DisplayName("Add round")
	@Transactional
	@Test
	void addRoundTest() {
		
		RoundDto roundDto= new RoundDto();
		roundDto.setMatchPlay(false);
		Calendar calendar = new GregorianCalendar();
		calendar.set(2020, 5, 12);
		roundDto.setRoundDate(calendar.getTime());
		roundDto.setScoreCard(new ArrayList<ScoreCardDto>());
		ScoreCardDto scoreCard = new ScoreCardDto();
		scoreCard.setHole(1);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setStroke(5);
		roundDto.getScoreCard().add(scoreCard);
		CourseDto courseDto = new CourseDto();
		courseDto.setId(1l);
		List<CourseTeeDto> courseTeeDtoLst = new ArrayList<>();
		CourseTeeDto courseTeeDto = new CourseTeeDto();
		courseTeeDto.setId(1l);
		courseTeeDtoLst.add(courseTeeDto);
		courseDto.setTees(courseTeeDtoLst);
		roundDto.setCourse(courseDto);
		PlayerDto playerDto = new PlayerDto();
		playerDto.setId(1L);
		playerDto.setWhs(32.1f);
		List<PlayerDto> playerDtoLst = new ArrayList<>();
		playerDtoLst.add(playerDto);
		roundDto.setPlayer(playerDtoLst);
			
		HttpStatus status = this.roundController.addRound(roundDto);
		
		assertEquals(HttpStatus.OK, status);
	}
	
	@DisplayName("Get round for player test")
	@Transactional
	@Test
	void getRoundForPlayerTest() {
		
		List<LimitedRoundDto> roundDtoLst =  this.roundController.getRound(1l, 0);
		
		assertEquals(1, roundDtoLst.size());
	}
	
	@DisplayName("Get recent rounds")
	@Transactional
	@Test
	void getRecentRoundsTest() {
		
		List<LimitedRoundWithPlayersDto> roundDtoLst =  this.roundController.getRecentRounds(0);
		
		assertEquals(1, roundDtoLst.size());
	}
	
	@DisplayName("Get scorcards")
	@Transactional
	@Test
	void getScoreCardsTest() {
		
		List<ScoreCardDto> scoreCardDtoLst =  this.roundController.getScoreCards(round.getId());
		
		assertEquals(2, scoreCardDtoLst.size());
	}
	
	@DisplayName("Get round player details")
	@Transactional
	@Test
	void getRoundPlayerDetailsTest() {
		
		PlayerRoundDto playerRoundDto =  this.roundController.getRoundPlayerDetails(1l, round.getId());
		
		assertEquals(player.getWhs(), playerRoundDto.getWhs());
	}
	
	@DisplayName("Get player details for round")
	@Transactional
	@Test
	void getPlayerDetailsForRoundTest() {
		
		List<PlayerRoundDto> playerRoundDtoLst =  this.roundController.getPlayersDetailsForRound(round.getId());
		
		assertEquals(player.getWhs(), playerRoundDtoLst.get(0).getWhs());
	}
	
	
	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository) {

		roundRepository.deleteAll();

		log.info("Clean up completed");

	}

}
