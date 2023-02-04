package com.greg.golf.service;

import java.util.*;

import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.security.JwtRequestFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.CourseTee;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.PlayerRound;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.entity.Tournament;
import com.greg.golf.error.PlayerAlreadyHasThatRoundException;
import com.greg.golf.error.ScoreCardUpdateException;
import com.greg.golf.error.TooManyPlayersException;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.service.events.RoundEvent;
import com.greg.golf.util.GolfPostgresqlContainer;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
// @ExtendWith(SpringExtension.class)
class RoundServiceTest {

	@SuppressWarnings("unused")
	@MockBean
	private JwtRequestFilter jwtRequestFilter;

	@ClassRule
	public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
			.getInstance();

	private static Long roundId;

	@SuppressWarnings("unused")
	@Autowired
	private RoundService roundService;

	@SuppressWarnings("unused")
	@MockBean
	private TournamentService mockTournamentService;

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

	@DisplayName("Delete score card for nonexistent round")
	@Transactional
	@Test
	void deleteScoreCardForNonExistingRoundTest() {

		Assertions.assertThrows(NoSuchElementException.class, () -> roundService.deleteScorecard(1L, 999L));
	}

	@DisplayName("Delete score card for null argument")
	@Transactional
	@Test
	void deleteScoreCardForEmptyRoundTest() {

		Assertions.assertThrows(InvalidDataAccessApiUsageException.class, () -> roundService.deleteScorecard(1L, null));
	}

	@DisplayName("Delete score card for incorrect player")
	@Transactional
	@Test
	void deleteScoreCardWithIncorrectPlayerTest() {

		Assertions.assertThrows(NoSuchElementException.class, () -> roundService.deleteScorecard(2L, roundId));
	}

	@DisplayName("Delete score card for a round with only one player")
	@Transactional
	@Test
	void deleteScoreCardForRoundWithOnePlayerTest(@Autowired RoundRepository roundRepository) {

		roundService.deleteScorecard(1L, roundId);
		Assertions.assertEquals(0, roundRepository.count());

	}

	@DisplayName("Delete score card for a round with two players")
	@Transactional
	@Test
	void deleteScoreCardForRoundWithTwoPlayerTest(@Autowired RoundRepository roundRepository,
			@Autowired PlayerRepository playerRepository) {

		var round = roundRepository.findById(roundId).orElseThrow();

		var player = new Player();
		player.setNick("player2");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		player.setModified(false);
		player.setType(Common.TYPE_PLAYER_LOCAL);
		playerRepository.save(player);
		round.getPlayer().add(player);
		player.setRounds(new ArrayList<>());
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
		Assertions.assertEquals(2, round.getScoreCard().size());

	}

	@DisplayName("Try to save the round with more than 4 players")
	@Transactional
	@Test
	void saveRoundWithMoreThan4PlayersTest(@Autowired RoundRepository roundRepository,
			@Autowired PlayerRepository playerRepository) {

		var round = roundRepository.findById(roundId).orElseThrow();

		var player = new Player();
		player.setNick("player2");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		player.setModified(false);
		player.setType(Common.TYPE_PLAYER_LOCAL);
		playerRepository.save(player);
		round.getPlayer().add(player);
		player = new Player();
		player.setNick("player3");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		player.setModified(false);
		player.setType(Common.TYPE_PLAYER_LOCAL);
		playerRepository.save(player);
		round.getPlayer().add(player);
		player = new Player();
		player.setNick("player4");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		player.setModified(false);
		player.setType(Common.TYPE_PLAYER_LOCAL);
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
		player.setModified(false);
		player.setType(Common.TYPE_PLAYER_LOCAL);
		playerRepository.save(player);

		var newRound = new Round();
		newRound.setCourse(round.getCourse());
		var playerSet = new TreeSet<Player>();
		playerSet.add(player);
		newRound.setPlayer(playerSet);
		newRound.setMatchPlay(false);
		newRound.setRoundDate(round.getRoundDate());
		newRound.setScoreCard(new ArrayList<>());

		Assertions.assertThrows(TooManyPlayersException.class, () -> roundService.saveRound(newRound));
	}

	@DisplayName("Try to save the round for the same player twice")
	@Transactional
	@Test
	void saveRoundForTheSamePlayerTwiceTest(@Autowired RoundRepository roundRepository) {

		var round = roundRepository.findById(roundId).orElseThrow();

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
		newRound.setScoreCard(new ArrayList<>());

		Assertions.assertThrows(PlayerAlreadyHasThatRoundException.class, () -> roundService.saveRound(newRound));
	}

	@DisplayName("Try to add scorecard to existing round")
	@Transactional
	@Test
	void addScorecardToExistingRoundTest(@Autowired RoundRepository roundRepository,
			@Autowired PlayerRepository playerRepository) {

		var round = roundRepository.findById(roundId).orElseThrow();

		// create the new player
		var player = new Player();
		player.setNick("player5");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		player.setModified(false);
		player.setType(Common.TYPE_PLAYER_LOCAL);
		playerRepository.save(player);

		// create the new round
		var newRound = new Round();
		newRound.setCourse(round.getCourse());
		var playerSet = new TreeSet<Player>();
		playerSet.add(player);
		newRound.setPlayer(playerSet);
		newRound.setMatchPlay(false);
		newRound.setRoundDate(round.getRoundDate());

		// create one scorecard and add it to the round
		var scoreCard = new ScoreCard();
		scoreCard.setHole(1);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setPlayer(player);
		scoreCard.setStroke(5);
		newRound.setScoreCard(new ArrayList<>());
		newRound.getScoreCard().add(scoreCard);

		roundService.saveRound(newRound);

		Assertions.assertEquals(2, roundRepository.findById(round.getId()).orElseThrow().getPlayer().size());
	}

	@DisplayName("Try to add scorecard to existing round with tournament")
	@Transactional
	@Test
	void addScorecardWithTournamentToExistingRoundTest(@Autowired RoundRepository roundRepository,
			@Autowired PlayerRepository playerRepository, @Autowired TournamentRepository tournamentRepository) {

		var round = roundRepository.findById(roundId).orElseThrow();

		var tournament = new Tournament();
		tournament.setEndDate(round.getRoundDate());
		tournament.setStartDate(round.getRoundDate());
		tournament.setName("Test tournament");
		tournament.setPlayer(round.getPlayer().iterator().next());
		tournament.setBestRounds(0);
		tournament.setStatus(Tournament.STATUS_OPEN);
		tournamentRepository.save(tournament);

		round = roundRepository.save(round);

		// create the new player
		var player = new Player();
		player.setNick("player5");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		player.setModified(false);
		player.setType(Common.TYPE_PLAYER_LOCAL);
		playerRepository.save(player);

		// create the new round
		var newRound = new Round();
		newRound.setCourse(round.getCourse());
		var playerSet = new TreeSet<Player>();
		playerSet.add(player);
		newRound.setPlayer(playerSet);
		newRound.setMatchPlay(false);
		newRound.setRoundDate(round.getRoundDate());

		// create one scorecard and add it to the round
		var scoreCard = new ScoreCard();
		scoreCard.setHole(1);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setPlayer(player);
		scoreCard.setStroke(5);
		newRound.setScoreCard(new ArrayList<>());
		newRound.getScoreCard().add(scoreCard);

		var valueCapture = ArgumentCaptor.forClass(RoundEvent.class);
		Mockito.doNothing().when(mockTournamentService).handleRoundEvent(valueCapture.capture());

		roundService.saveRound(newRound);
		Assertions.assertEquals(2, roundRepository.findById(round.getId()).orElseThrow().getPlayer().size());
	}

	@DisplayName("Try to update correct scorecard")
	@Transactional
	@Test
	void correctScoreCardUpdateTest(@Autowired RoundRepository roundRepository) {

		var round = roundRepository.findById(roundId).orElseThrow();

		// create the new round
		var newRound = new Round();
		newRound.setId(round.getId());
		newRound.setCourse(round.getCourse());
		var playerSet = new TreeSet<Player>();
		playerSet.add(round.getPlayer().iterator().next());
		newRound.setPlayer(playerSet);
		newRound.setMatchPlay(false);
		newRound.setRoundDate(round.getRoundDate());
		newRound.setScoreCard(new ArrayList<>());
		var scoreCard = new ScoreCard();
		scoreCard.setHole(1);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setPlayer(round.getPlayer().iterator().next());
		scoreCard.setRound(round);
		scoreCard.setStroke(6);
		newRound.getScoreCard().add(scoreCard);
		// update the scorecard
		roundService.updateScoreCard(newRound);

		round = roundRepository.findById(roundId).orElseThrow();

		Assertions.assertEquals(1, round.getScoreCard().size());
		Assertions.assertEquals(6, round.getScoreCard().get(0).getStroke().intValue());
	}

	@DisplayName("Try to update scorecard with more than 1 player")
	@Transactional
	@Test
	void scoreCardUpdateWithMoreThanOnePlayerTest(@Autowired RoundRepository roundRepository,
			@Autowired PlayerRepository playerRepository) {

		// create the new player
		var player = new Player();
		player.setNick("player5");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		player.setModified(false);
		player.setType(Common.TYPE_PLAYER_LOCAL);
		playerRepository.save(player);

		var round = roundRepository.findById(roundId).orElseThrow();

		// create the new round
		var newRound = new Round();
		newRound.setId(round.getId());
		newRound.setCourse(round.getCourse());
		var playerSet = new TreeSet<Player>();
		playerSet.add(player);
		playerSet.add(round.getPlayer().iterator().next());
		newRound.setPlayer(playerSet);
		newRound.setMatchPlay(false);
		newRound.setRoundDate(round.getRoundDate());

		Assertions.assertThrows(ScoreCardUpdateException.class, () -> roundService.updateScoreCard(newRound));
	}

	@DisplayName("Try to update scorecard without player")
	@Transactional
	@Test
	void scoreCardUpdateWithoutPlayerTest(@Autowired RoundRepository roundRepository) {

		var round = roundRepository.findById(roundId).orElseThrow();

		// create the new round
		var newRound = new Round();
		newRound.setId(round.getId());
		newRound.setCourse(round.getCourse());
		newRound.setMatchPlay(false);
		newRound.setRoundDate(round.getRoundDate());

		Assertions.assertThrows(ScoreCardUpdateException.class, () -> roundService.updateScoreCard(newRound));
	}

	@DisplayName("Get Round inside range applicable for tournament")
	@Transactional
	@Test
	void getForPlayerRoundDetailsTest() {

		List<PlayerRound> pr = roundService.getByRoundId(roundId);

		Assertions.assertEquals(1, pr.size());

	}

	@DisplayName("Get Round inside range applicable for tournament")
	@Transactional
	@Test
	void getRoundInsideRangeApplicableTest() {

		var startDate = new GregorianCalendar();
		startDate.set(2020, Calendar.JUNE, 11, 0, 0, 0);
		var endDate = new GregorianCalendar();
		endDate.set(2020, Calendar.JULY, 14, 0, 0, 0);

		var rounds = roundService.findByDates(startDate.getTime(), endDate.getTime());

		Assertions.assertEquals(1, rounds.size());

	}

	@DisplayName("Round applicable for tournament not found")
	@Transactional
	@Test
	void getRoundInsideRangeNotApplicableTest() {

		var startDate = new GregorianCalendar();
		startDate.set(2020, Calendar.JUNE, 13, 0, 0, 0);
		var endDate = new GregorianCalendar();
		endDate.set(2020, Calendar.JULY, 14, 0, 0, 0);

		var rounds = roundService.findByDates(startDate.getTime(), endDate.getTime());

		Assertions.assertEquals(0, rounds.size());

	}

	@DisplayName("Get Round pageable")
	@Transactional
	@Test
	void getRoundPageableTest(@Autowired PlayerRepository playerRepository) {

		var player = playerRepository.findById(1L).orElseThrow();

		var rounds = roundService.listByPlayerPageable(player, 0);

		Assertions.assertEquals(1, rounds.size());

	}
	
	@DisplayName("Add round")
	@Transactional
	@Test
	void addRoundTest(@Autowired RoundRepository roundRepository) {
		
		var round= new Round();
		round.setMatchPlay(false);
		var calendar = new GregorianCalendar();
		calendar.set(2020, Calendar.JUNE, 12);
		round.setRoundDate(calendar.getTime());
		round.setScoreCard(new ArrayList<>());
		var scoreCard = new ScoreCard();
		scoreCard.setHole(1);
		scoreCard.setPats(0);
		scoreCard.setPenalty(0);
		scoreCard.setStroke(5);
		round.getScoreCard().add(scoreCard);
		var course = new Course();
		course.setId(1L);
		var courseTeeLst = new ArrayList<CourseTee>();
		var courseTee = new CourseTee();
		courseTee.setId(1L);
		courseTeeLst.add(courseTee);
		course.setTees(courseTeeLst);
		round.setCourse(course);
		var player = new Player();
		player.setId(1L);
		player.setWhs(32.1f);
		var playerLst = new TreeSet<Player>();
		playerLst.add(player);
		round.setPlayer(playerLst);
			
		roundService.saveRound(round);
		
		Assertions.assertEquals(2, roundRepository.findAll().size());
	}
	
	@DisplayName("Get recent rounds")
	@Transactional
	@Test
	void getRecentRoundsTest() {
		
		var roundLst =  roundService.getRecentRounds(0);
		
		Assertions.assertEquals(1, roundLst.size());
	}
	
	@DisplayName("Get round player details")
	@Transactional
	@Test
	void getRoundPlayerDetailsTest(@Autowired PlayerRepository playerRepository) {
		
		var player = playerRepository.findById(1L).orElseThrow();
		
		var playerRound =  roundService.getForPlayerRoundDetails(1L, roundId);
		
		Assertions.assertEquals(player.getWhs(), playerRound.getWhs());
	}

	@DisplayName("Update player hcp for round")
	@Transactional
	@Test
	void updatePlayerRoundWhsTest() {

		roundService.updateRoundWhs(1L, roundId, 11.3F);
		var playerRound =  roundService.getForPlayerRoundDetails(1L, roundId);

		Assertions.assertEquals(11.3F, playerRound.getWhs());
	}

	@DisplayName("Get player round count")
	@Transactional
	@Test
	void getPlayerRoundCntByAuthorizedUserTest(@Autowired PlayerService playerService) {

		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.ADMIN));

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

		var retList = playerService.getPlayerRoundCnt();

		Assertions.assertEquals(1, retList.size());
		Assertions.assertEquals(1, retList.get(0).getRoundCnt());
	}

	@DisplayName("Get player round count")
	@Transactional
	@Test
	void getPlayerRoundCntByUnauthorizedUserTest(@Autowired PlayerService playerService) {

		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

		assertThrows(UnauthorizedException.class, playerService::getPlayerRoundCnt);
	}

	@DisplayName("Swap players for round")
	@Transactional
	@Test
	void swapPlayersForTest(@Autowired PlayerRepository playerRepository, @Autowired PlayerRoundRepository playerRoundRepository) {

		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.ADMIN));

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("authorized", "fake", authorities));

		// create the new player
		var player = new Player();
		player.setNick("player5");
		player.setPassword("test");
		player.setSex(true);
		player.setWhs(30.1f);
		player.setRole(0);
		player.setModified(false);
		player.setType(Common.TYPE_PLAYER_LOCAL);
		playerRepository.save(player);

		roundService.swapPlayer(1L, player.getId(), roundId);

		assertEquals(player.getId(), playerRoundRepository.getForPlayerAndRound(player.getId(), roundId).orElseThrow().getPlayerId());
	}

	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository) {

		roundRepository.deleteAll();
		log.info("Clean up completed");

	}

}
