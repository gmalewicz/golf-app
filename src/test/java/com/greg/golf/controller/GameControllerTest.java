package com.greg.golf.controller;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.greg.golf.controller.GameController;
import com.greg.golf.controller.dto.GameDataDto;
import com.greg.golf.controller.dto.GameDto;
import com.greg.golf.controller.dto.PlayerDto;
import com.greg.golf.entity.Course;
import com.greg.golf.entity.Game;
import com.greg.golf.entity.GameData;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.service.CourseService;
import com.greg.golf.service.GameService;
import com.greg.golf.service.PlayerService;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import lombok.extern.log4j.Log4j2;


@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class GameControllerTest {
	
	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

	private static Player player;
	private static Round round;
	
	private final GameController gameController;

	@Autowired
	public GameControllerTest(GameController gameController) {
		this.gameController = gameController;
	}

	@BeforeAll
	public static void setup(@Autowired PlayerService playerService, @Autowired CourseService courseService,
			@Autowired RoundRepository roundRepository, @Autowired PlayerRoundRepository playerRoundRepository) {

		player = playerService.getPlayer(1L).orElseThrow();

		round = new Round();

		Course course = courseService.getCourse(1L).orElseThrow();
		round.setCourse(course);
		SortedSet<Player> playerSet = new TreeSet<Player>();
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
		
	@DisplayName("Add game")
	@Transactional
	@Test
	void addGameTest(@Autowired ModelMapper modelMapper) {
		
		GameDto gameDto = new GameDto();
		gameDto.setPlayer(modelMapper.map(player, PlayerDto.class));
		gameDto.setGameDate(new Date());
		gameDto.setGameId(1l);
		gameDto.setStake(0.5f);
		
		GameDataDto gameDataDto = new GameDataDto();
		String[] nicks = {"golfer", "test"};
		gameDataDto.setPlayerNicks(nicks);
		Integer[] score = {1, 2};
		gameDataDto.setScore(score);;
		Short[][] gameResult = {{1, 2}};
		gameDataDto.setGameResult(gameResult);
		
		gameDto.setGameData(gameDataDto);
		
		HttpStatus status =  this.gameController.addGame(gameDto);
		
		assertEquals(HttpStatus.OK, status);
	}
	
	@DisplayName("Add game")
	@Transactional
	@Test
	void getGamesTest(@Autowired ModelMapper modelMapper, @Autowired GameService gameService) {
		
		Game game = new Game();
		game.setPlayer(player);
		game.setGameDate(new Date());
		game.setGameId(1l);
		game.setStake(0.5f);
		
		GameData gameData = new GameData();
		String[] nicks = {"golfer", "test"};
		gameData.setPlayerNicks(nicks);
		Integer[] score = {1, 2};
		gameData.setScore(score);;
		Short[][] gameResult = {{1, 2}};
		gameData.setGameResult(gameResult);
		
		game.setGameData(gameData);
		
		gameService.save(game);
		
		List<GameDto> gameDtoLst =  this.gameController.getGames(1l);
		
		assertEquals(1l, gameDtoLst.get(0).getGameId().longValue());
	}
	

	
	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository) {

		roundRepository.deleteAll();

		log.info("Clean up completed");

	}

}
