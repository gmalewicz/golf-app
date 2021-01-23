package com.greg.golf.controller;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import com.greg.golf.controller.GolfRESTController;
import com.greg.golf.controller.dto.CourseDto;
import com.greg.golf.controller.dto.CourseTeeDto;
import com.greg.golf.controller.dto.GameDataDto;
import com.greg.golf.controller.dto.GameDto;
import com.greg.golf.controller.dto.HoleDto;
import com.greg.golf.controller.dto.PlayerDto;
import com.greg.golf.controller.dto.PlayerRoundDto;
import com.greg.golf.controller.dto.RoundDto;
import com.greg.golf.controller.dto.ScoreCardDto;
import com.greg.golf.controller.dto.TournamentDto;
import com.greg.golf.controller.dto.TournamentResultDto;
import com.greg.golf.entity.Course;
import com.greg.golf.entity.FavouriteCourse;
import com.greg.golf.entity.Game;
import com.greg.golf.entity.GameData;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.entity.Tournament;
import com.greg.golf.entity.TournamentResult;
import com.greg.golf.repository.FavouriteCourseRepository;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.repository.TournamentResultRepository;
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
class GolfRESTControllerTest {
	
	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

	private static Player player;
	private static Round round;
	private static Tournament tournament;

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

		List<TournamentDto> tournaments = this.golfRESTController.getTournaments();
		assertEquals(1, tournaments.size());

	}

	@DisplayName("Gets all tournaments empty")
	@Transactional
	@Test
	void getTrournamentsWithEmptyTest(@Autowired TournamentRepository tournamentRepository) {

		tournamentRepository.deleteAll();
		List<TournamentDto> tournaments = this.golfRESTController.getTournaments();
		assertEquals(0, tournaments.size());

	}

	@DisplayName("Gets list of holes for course")
	@Transactional
	@Test
	void getListOfHolesTest() {

		List<HoleDto> holeLst = this.golfRESTController.getHoles(1l);

		assertEquals(18, holeLst.size());

	}

	@DisplayName("Add course test")
	@Transactional
	@Test
	void addCourseTest() {

		CourseDto courseDto = new CourseDto();
		courseDto.setName("Test course");
		courseDto.setHoleNbr(9);
		courseDto.setPar(36);

		List<HoleDto> holeDtoLst = new ArrayList<>();

		for (int i = 0; i < 9; i++) {
			HoleDto holeDto = new HoleDto();
			holeDto.setNumber(i + 1);
			holeDto.setPar(4);
			holeDto.setSi(18);
			holeDtoLst.add(holeDto);
		}

		courseDto.setHoles(holeDtoLst);

		List<CourseTeeDto> courseTeeDtoLst = new ArrayList<>();
		CourseTeeDto courseTeeDto = new CourseTeeDto();
		courseTeeDto.setCr(71f);
		courseTeeDto.setSr(78);
		courseTeeDto.setTeeType(1);
		courseTeeDto.setTee("Ladies red 1-18");
		courseTeeDtoLst.add(courseTeeDto);

		courseDto.setTees(courseTeeDtoLst);

		HttpStatus status = this.golfRESTController.addCourse(courseDto);

		assertEquals(HttpStatus.OK, status);
	}
	
	@DisplayName("Get tees test")
	@Transactional
	@Test
	void getTeesTest() {

		List<CourseTeeDto> retTees = this.golfRESTController.getTees(1l);

		assertEquals(9, retTees.size());
	}
	
	@DisplayName("Get favourite course test")
	@Transactional
	@Test
	void getFavouriteCourseTest(@Autowired FavouriteCourseRepository favouriteCourseRepository) {

		FavouriteCourse fc = new FavouriteCourse();
		Player player = new Player();
		player.setId(1L);
		Course course = new Course();
		course.setId(1L);
		fc.setPlayer(player);
		fc.setCourse(course);
		
		favouriteCourseRepository.save(fc);
		
		List<CourseDto> retCourses = this.golfRESTController.getFavouriteCourses(1l);
		
		assertEquals(1, retCourses.size());
	}
	
	@DisplayName("Get course to favourites")
	@Transactional
	@Test
	void getCourseToFavouritesTest() {
		
		CourseDto courseDto = new CourseDto();
		courseDto.setId(1l);;
		courseDto.setName("Test course");
		courseDto.setHoleNbr(9);
		courseDto.setPar(36);
		HttpStatus status = this.golfRESTController.addCourseToFavourites(1l, courseDto);
		
		assertEquals(HttpStatus.OK, status);
	}
	
	@DisplayName("Delete course from favourites")
	@Transactional
	@Test
	void deleteCourseFromFavouritesTest() {
		
		CourseDto courseDto = new CourseDto();
		courseDto.setId(1l);;
		courseDto.setName("Test course");
		courseDto.setHoleNbr(9);
		courseDto.setPar(36);
		this.golfRESTController.addCourseToFavourites(1l, courseDto);
		HttpStatus status = this.golfRESTController.deleteCourseFromFavourites(1l, courseDto);
		
		assertEquals(HttpStatus.OK, status);
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
		Set<PlayerDto> playerDtoLst = new HashSet<>();
		playerDtoLst.add(playerDto);
		roundDto.setPlayer(playerDtoLst);
			
		HttpStatus status = this.golfRESTController.addRound(roundDto);
		
		assertEquals(HttpStatus.OK, status);
	}
	
	@DisplayName("Get round for player test")
	@Transactional
	@Test
	void getRoundForPlayerTest() {
		
		List<RoundDto> roundDtoLst =  this.golfRESTController.getRound(1l, 0);
		
		assertEquals(1, roundDtoLst.size());
	}
	
	@DisplayName("Get recent rounds")
	@Transactional
	@Test
	void getRecentRoundsTest() {
		
		List<RoundDto> roundDtoLst =  this.golfRESTController.getRecentRounds(0);
		
		assertEquals(1, roundDtoLst.size());
	}
	
	@DisplayName("Get scorcards")
	@Transactional
	@Test
	void getScoreCardsTest() {
		
		List<ScoreCardDto> scoreCardDtoLst =  this.golfRESTController.getScoreCards(round.getId());
		
		assertEquals(2, scoreCardDtoLst.size());
	}
	
	@DisplayName("Get round player details")
	@Transactional
	@Test
	void getRoundPlayerDetailsTest() {
		
		PlayerRoundDto playerRoundDto =  this.golfRESTController.getRoundPlayerDetails(1l, round.getId());
		
		assertEquals(player.getWhs(), playerRoundDto.getWhs());
	}
	
	@DisplayName("Get player details for round")
	@Transactional
	@Test
	void getPlayerDetailsForRoundTest() {
		
		List<PlayerRoundDto> playerRoundDtoLst =  this.golfRESTController.getPlayersDetailsForRound(round.getId());
		
		assertEquals(player.getWhs(), playerRoundDtoLst.get(0).getWhs());
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
		
		HttpStatus status =  this.golfRESTController.addGame(gameDto);
		
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
		
		List<GameDto> gameDtoLst =  this.golfRESTController.getGames(1l);
		
		assertEquals(1l, gameDtoLst.get(0).getGameId().longValue());
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
		
		List<TournamentResultDto> trDtoLst =  this.golfRESTController.getTournamentResult(tournament.getId());
		
		assertEquals(1, trDtoLst.get(0).getStbNet().intValue());
	}
	
	@DisplayName("Get rounds for tournament")
	@Transactional
	@Test
	void getRoundsForTournamentTest() {
		
	
		List<RoundDto> rndDtoLst =  this.golfRESTController.getTournamentRounds(tournament.getId());
		
		assertEquals(1, rndDtoLst.size());
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
