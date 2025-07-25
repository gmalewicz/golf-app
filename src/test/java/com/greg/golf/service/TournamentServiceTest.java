package com.greg.golf.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import com.greg.golf.entity.*;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.*;
import com.greg.golf.repository.*;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.aes.StringUtility;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import com.greg.golf.service.events.RoundEvent;
import com.greg.golf.util.GolfPostgresqlContainer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class TournamentServiceTest {

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtRequestFilter jwtRequestFilter;

	@ClassRule
	public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
			.getInstance();

	@SuppressWarnings("unused")
	@Autowired
	private TournamentService tournamentService;

	@SuppressWarnings("unused")
	@Autowired
	TournamentResultRepository tournamentResultRepository;

	@SuppressWarnings("unused")
	@MockitoBean
	private EmailServiceImpl emailService;

	private static Long roundId;

	@BeforeAll
	static void setup(@Autowired PlayerService playerService, @Autowired CourseService courseService,
			@Autowired RoundRepository roundRepository, @Autowired PlayerRoundRepository playerRoundRepository,
			@Autowired TournamentRepository tournamentRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();
		player.setScoreCard(new ArrayList<>());

		var round = new Round();

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
		roundId = round.getId();

		playerRoundRepository.updatePlayerRoundInfo(player.getWhs(), 135, 70.3f, 2L, 0, player.getId(), round.getId());

		// create the next round
		var round2 = new Round();
		round2.setCourse(course);
		var playerSet2 = new TreeSet<Player>();
		playerSet2.add(player);
		round2.setPlayer(playerSet2);
		round2.setRoundDate(new Date(3));
		round2.setMatchPlay(false);
		round2.setScoreCard(new ArrayList<>());
		for (var i = 0; i < 18; i++) {
			var scoreCard = new ScoreCard();
			scoreCard.setHole(i + 1);
			scoreCard.setPats(0);
			scoreCard.setPenalty(0);
			scoreCard.setPlayer(player);
			scoreCard.setRound(round2);
			scoreCard.setStroke(5);
			scoreCard.setHcp(2);
			round2.getScoreCard().add(scoreCard);
		}
		roundRepository.save(round2);

		playerRoundRepository.updatePlayerRoundInfo(player.getWhs(), 135, 70.3f, 2L, 0, player.getId(), round2.getId());

		var tournament = new Tournament();
		tournament.setEndDate(new Date(1));
		tournament.setStartDate(new Date(1));
		tournament.setName("Test Cup");
		tournament.setPlayer(player);
		tournament.setStatus(Tournament.STATUS_OPEN);
		tournament.setBestRounds(Common.ALL_ROUNDS);
		tournament.setMaxPlayHcp(54);
		tournament.setCanUpdateHcp(true);
		tournament.setPlayHcpMultiplayer(1f);
		tournamentRepository.save(tournament);

		log.info("Set up completed");
	}

	@DisplayName("Delete tournament")
	@Transactional
	@Test
	void deleteTournamentTest(@Autowired TournamentResultRepository tournamentResultRepository, @Autowired PlayerService playerService) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setPlayer(player);
		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);

		TournamentRound tournamentRound =
				tournamentService.addTournamentRound(1, 1, 1, 1, 1,
						"test", tournamentResult, false, roundId, 10, 10,  10);

		tournamentResult.setTournamentRound(new ArrayList<>());
		tournamentResult.getTournamentRound().add(tournamentRound);
		tournamentResultRepository.save(tournamentResult);

		tournamentService.deleteTournament(tournament.getId());
		Assertions.assertEquals(0, tournamentService.findAllTournamentsPageable(0).size());
	}

	@DisplayName("Delete tournament result")
	@Transactional
	@Test
	void deleteTournamentResultTest(@Autowired PlayerService playerService) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setPlayer(player);
		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);

		TournamentRound tournamentRound =
				tournamentService.addTournamentRound(1, 1, 1, 1, 1,
						"test", tournamentResult, false, roundId, 10, 10, 10);

		tournamentResult.setTournamentRound(new ArrayList<>());
		tournamentResult.getTournamentRound().add(tournamentRound);
		tournamentResultRepository.save(tournamentResult);

		tournamentService.deleteResult(tournamentResult.getId());
		Assertions.assertEquals(0, tournamentService.findAllTournamentsPageable(0).getFirst().getTournamentResult().size());
	}


	@DisplayName("Calculate net and gross STB")
	@Transactional
	@Test
	void calculateNetAndGrossSTBTest(@Autowired RoundRepository roundRepository,
									 @Autowired PlayerService playerService) {

		var player = playerService.getPlayer(1L).orElseThrow();

		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setPlayer(player);
		tournamentResult.setTournament(tournamentService.findAllTournamentsPageable(0).getFirst());
		tournamentResultRepository.save(tournamentResult);

		var retRound = roundRepository.findAll().getFirst();
		tournamentService.updateSTB(tournamentResult, retRound, player, 45);

        log.info("STB net: {}", tournamentResult.getStbNet());
        log.info("STB gross: {}", tournamentResult.getStbGross());
		Assertions.assertEquals(62, tournamentResult.getStbNet().intValue());
		Assertions.assertEquals(17, tournamentResult.getStbGross().intValue());
	}

	@DisplayName("Should add the new tournament")
	@Transactional
	@Test
	void addTheNewTournamentTest(@Autowired PlayerService playerService) {

		var player = playerService.getPlayer(1L).orElseThrow();

		var tournament = new Tournament();
		tournament.setEndDate(new Date(1));
		tournament.setStartDate(new Date(1));
		tournament.setName("Test Cup2");
		tournament.setPlayer(player);
		tournament.setBestRounds(1);
		tournament.setPlayHcpMultiplayer(1F);
		tournament.setMaxPlayHcp(54);
		tournament.setCanUpdateHcp(true);
		tournament = tournamentService.addTournament(tournament);

		Assertions.assertNotNull(tournament.getId());
	}

	@DisplayName("Calculate corrected strokes")
	@Transactional
	@Test
	void calculateCorrectedStrokesTest(@Autowired HoleRepository holeRepository, @Autowired PlayerService playerService,
									   @Autowired RoundRepository roundRepository,
									   @Autowired CourseRepository courseRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();
		var course = courseRepository.findById(1L).orElseThrow();
		var round = roundRepository.findAll().getFirst();

		round.getCourse().setHoles(holeRepository.findByCourse(course));

		round.getScoreCard().getFirst().setStroke(20);
		var correctedScore = tournamentService.getCorrectedStrokes(player, round);

        log.info("corrected Strokes: {}", correctedScore);
		Assertions.assertEquals(91, correctedScore);
	}

	@DisplayName("Calculate score differential")
	@Transactional
	@Test
	void calculateScoreDifferentialTest(@Autowired HoleRepository holeRepository, @Autowired PlayerService playerService,
										@Autowired RoundRepository roundRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();
		var round = roundRepository.findAll().getFirst();

		round.getCourse().setHoles(holeRepository.findByCourse(round.getCourse()));
		round.getScoreCard().getFirst().setStroke(20);

		var playerRound = new PlayerRound();
		playerRound.setSr(113);
		playerRound.setCr((float) 0);

		var scoreDifferential = tournamentService.getScoreDifferential(playerRound, round, player);

        log.info("score differential: {}", scoreDifferential);
		Assertions.assertEquals(91, (int) scoreDifferential);
	}

	@DisplayName("Should save tournament round")
	@Transactional
	@Test
	void saveTournamentRoundTest(@Autowired PlayerService playerService) {

		var player = playerService.getPlayer(1L).orElseThrow();

		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setPlayer(player);
		tournamentResult.setTournament(tournamentService.findAllTournamentsPageable(0).getFirst());
		tournamentResultRepository.save(tournamentResult);

		TournamentRound tournamentRound =
				tournamentService.addTournamentRound(1, 1, 1, 1, 1,
											"test", tournamentResult, false, 1, 10, 10, 10);

		Assertions.assertNotNull(tournamentRound.getId());
	}

	@DisplayName("Get all tournaments where all rounds are counted")
	@Transactional
	@Test
	void getAllTournamentsTestAllRounds() {

		Assertions.assertEquals(1, tournamentService.findAllTournamentsPageable(0).size());

	}

	@DisplayName("Get all tournaments where not all rounds are counted")
	@Transactional
	@Test
	void getAllTournamentResultsTestNotAllRounds(@Autowired TournamentRepository tournamentRepository) {

		var tournament = tournamentRepository.findAll().getFirst();
		tournament.setBestRounds(1);
		tournamentRepository.save(tournament);

		Assertions.assertEquals(0, tournamentService.findAllTournamentsResults(tournament.getId()).size());
	}


	@DisplayName("Calculate gross strokes")
	@Transactional
	@Test
	void getGrossStrokesTest(@Autowired PlayerService playerService, @Autowired RoundRepository roundRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();
		var round = roundRepository.findAll().getFirst();

		var grossStrokes = tournamentService.getGrossStrokes(player, round);

		Assertions.assertEquals(90, grossStrokes);

	}

	@DisplayName("Calculate net strokes")
	@Transactional
	@Test
	void getNetStrokesTest() {

		var netStrokes = tournamentService.getNetStrokes(99, 45);

		Assertions.assertEquals(54, netStrokes);

	}

	@DisplayName("Calculate net strokes with max playing hcp and cap")
	@Transactional
	@Test
	void getNetStrokesMaxHcpAndCapTest(@Autowired TournamentRepository tournamentRepository) {

		Tournament tournament = tournamentRepository.findAll().getFirst();
		tournament.setMaxPlayHcp(18);
		tournament.setPlayHcpMultiplayer(0.75f);
		tournamentRepository.save(tournament);

		var netStrokes = tournamentService.getNetStrokes(99, 18);

		Assertions.assertEquals(81, netStrokes);

	}

	@DisplayName("Calculate net strokes where net strokes is lower than 0")
	@Transactional
	@Test
	void getNetStrokesLowerThan0Test() {

		var netStrokes = tournamentService.getNetStrokes( 22, 45);

		Assertions.assertEquals(0, netStrokes);

	}

	@DisplayName("Should not allow add the same round to tournament twice")
	@Transactional
	@Test
	void addTesSameRoundTwiceTest(@Autowired RoundRepository roundRepository,
								  @Autowired TournamentPlayerRepository tournamentPlayerRepository,
								  @Autowired TournamentRoundRepository tournamentRoundRepository) {

		var round = roundRepository.findAll().getFirst();
		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);

		tournamentPlayerRepository.save(tournamentPlayer);

		tournamentService.addRound(tournament.getId(), round.getId(), true);
		assertEquals(1L, tournamentRoundRepository.count());

		Long tournamentId = tournament.getId();
		Long rndId = round.getId();

		tournamentService.addRound(tournamentId, rndId, true);
		assertEquals(1L, tournamentRoundRepository.count());
	}

	@DisplayName("Adding the new round to the tournament result")
	@Transactional
	@Test
	void addNewRoundToTournamentResultTest(@Autowired RoundRepository roundRepository,
										   @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var round = roundRepository.findAll().getFirst();
		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);

		tournamentPlayerRepository.save(tournamentPlayer);

		log.info(round.getPlayer().iterator().next().getNick());

		tournamentService.updateTournamentResult(round, tournament);
		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(90, tr.getStrokesBrutto().intValue());

	}

	@DisplayName("Adding the new round to the tournament result where strokes are not applicable")
	@Transactional
	@Test
	void addNewRoundToTournamentResultTestStrokesNotApplicable(@Autowired RoundRepository roundRepository,
															   @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var round = roundRepository.findAll().getFirst();
		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);

		tournamentPlayerRepository.save(tournamentPlayer);

		round.getScoreCard().getFirst().setStroke(Common.HOLE_GIVEN_UP);
		roundRepository.save(round);
		tournamentService.updateTournamentResult(round, tournament);
		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(0, tr.getStrokesBrutto().intValue());

	}

	@DisplayName("Adding the new round to the tournament result where strokes are not applicable and 1 round applicable")
	@Transactional
	@Test
	void addNewRoundToTournamentResultTestStrokesNotApplicableOneRoundApplicable(@Autowired RoundRepository roundRepository,
																				 @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var round = roundRepository.findAll().getFirst();

		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();
		tournament.setBestRounds(1);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);

		tournamentPlayerRepository.save(tournamentPlayer);


		round.getScoreCard().getFirst().setStroke(Common.HOLE_GIVEN_UP);
		roundRepository.save(round);
		tournamentService.updateTournamentResult(round, tournament);
		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(0, tr.getStrokesBrutto().intValue());

	}

	@DisplayName("Should update the tournament result with the new round")
	@Transactional
	@Test
	void updateTournamentResultWithNewRoundTest(@Autowired RoundRepository roundRepository,
												@Autowired PlayerService playerService,
												@Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();
		var round = roundRepository.findAll().getFirst();
		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(100);
		tournamentResult.setStrokesBrutto(100);
		tournamentResult.setStrokesNetto(100);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);
		tournamentService.updateTournamentResult(round, tournament);
		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(190, tr.getStrokesBrutto().intValue());
	}

	@DisplayName("Should update the tournament result with the new round where best round is 1")
	@Transactional
	@Test
	void updateTournamentResultWithNewRoundWithBest1Round1Test(@Autowired TournamentRepository tournamentRepository,
															   @Autowired RoundRepository roundRepository,
															   @Autowired PlayerService playerService,
															   @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();
		var rounds = roundRepository.findAll();


		var round = rounds.getFirst();

		// set up 1 round as number of best rounds
		var tournament = tournamentRepository.findAll().getFirst();
		tournament.setBestRounds(1);
		tournament = tournamentRepository.save(tournament);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(100);
		tournamentResult.setStrokesBrutto(100);
		tournamentResult.setStrokesNetto(100);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);
		tournamentService.addRound(tournament.getId(), round.getId(), true);

		var round2 =  rounds.get(1);
		tournamentService.addRound(tournament.getId(), round2.getId(), true);

		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(90, tr.getStrokesBrutto().intValue());
	}

	@DisplayName("Should update the tournament result with the new round but hole that has been given up")
	@Transactional
	@Test
	void updateTournamentResultWithNewRoundHoleGivenUpTest(@Autowired RoundRepository roundRepository, @Autowired PlayerService playerService) {

		var player = playerService.getPlayer(1L).orElseThrow();
		var round = roundRepository.findAll().getFirst();
		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var redRound = roundRepository.findById(round.getId()).orElseThrow();

		redRound.getScoreCard().getFirst().setStroke(Common.HOLE_GIVEN_UP);
		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(100);
		tournamentResult.setStrokesBrutto(100);
		tournamentResult.setStrokesNetto(100);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);
		tournamentService.updateTournamentResult(redRound, tournament);
		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(100, tr.getStrokesBrutto().intValue());
	}

	@DisplayName("Should update the tournament result with updated round")
	@Transactional
	@Test
	void updateTournamentResultWithUpdatedRoundTest(@Autowired RoundRepository roundRepository,
													@Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var round = roundRepository.findAll().getFirst();
		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		tournamentService.updateTournamentResult(round, tournament);

		// update the round which was already added to tournament
		round.getScoreCard().getFirst().setStroke(15);
		roundRepository.save(round);

		// try to update the tournament
		var roundEvent = new RoundEvent(this, round);
		tournamentService.handleRoundEvent(roundEvent);

		Assertions.assertEquals(15, round.getScoreCard().getFirst().getStroke().intValue());

		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(90, tr.getStrokesBrutto().intValue());

	}


	@DisplayName("Should add the the round to the tournament and update tournament result")
	@Transactional
	@Test
	void addTheNewRoundAndUpdateTournamentResultTest(@Autowired RoundRepository roundRepository,
													 @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var round = roundRepository.findAll().getFirst();
		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		tournamentService.addRound(tournament.getId(), round.getId(), true);

		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(90, tr.getStrokesBrutto().intValue());

	}

	@DisplayName("Should return tournament round for tournament result")
	@Transactional
	@Test
	void getTournamentRoundForTournamentResultTest(@Autowired RoundRepository roundRepository,
												   @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var round = roundRepository.findAll().getFirst();
		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		tournamentService.updateTournamentResult(round, tournament);

		var roundResults = tournamentResultRepository.findAll().getFirst();

		tournamentService.getTournamentRoundsForResult(roundResults.getId());

		Assertions.assertEquals(1, tournamentService.getTournamentRoundsForResult(roundResults.getId()).size());

	}

	@DisplayName("Should return rounds (which exists) applicable for tournament")
	@Transactional
	@Test
	void getApplicableRoundsForTournamentTest(@Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		var rndLst = tournamentService.getAllPossibleRoundsForTournament(tournament.getId());

		Assertions.assertEquals(1, rndLst.size());

	}

	@DisplayName("Should return rounds (which does not exist) applicable for tournament")
	@Transactional
	@Test
	void getApplicableRoundsForTournamentButRoundNotExistTest(@Autowired TournamentPlayerRepository tournamentPlayerRepository,
															  @Autowired RoundRepository roundRepository) {

		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		roundRepository.deleteAll();

		var rndLst = tournamentService.getAllPossibleRoundsForTournament(tournament.getId());

		Assertions.assertEquals(0, rndLst.size());

	}

	@DisplayName("Should attempt add round to tournament for player which is not participant")
	@Transactional
	@Test
	void addRoundToTournamentForPlayerNotParticipantTest(@Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(2L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		var rndLst = tournamentService.getAllPossibleRoundsForTournament(tournament.getId());

		Assertions.assertEquals(0, rndLst.size());

	}

	@DisplayName("Should add round on behalf for tournament")
	@Transactional
	@Test
	void addRoundOnBehalfForTournamentTestAndReturnNullLst(@Autowired PlayerService playerService,
														   @Autowired CourseService courseService,
														   @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();
		var course = courseService.getCourse(1L).orElseThrow();
		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		var round2 = new Round();
		round2.setCourse(course);
		var playerSet2 = new TreeSet<Player>();
		playerSet2.add(player);
		round2.setPlayer(playerSet2);
		round2.setRoundDate(new Date(4));
		round2.setMatchPlay(false);
		round2.setScoreCard(new ArrayList<>());
		for (var i = 0; i < 18; i++) {
			var scoreCard = new ScoreCard();
			scoreCard.setHole(i + 1);
			scoreCard.setPats(0);
			scoreCard.setPenalty(0);
			scoreCard.setPlayer(player);
			scoreCard.setRound(round2);
			scoreCard.setStroke(5);
			scoreCard.setHcp(2);
			round2.getScoreCard().add(scoreCard);
		}

		Assertions.assertEquals(90, tournamentService.addRoundOnBehalf(tournament.getId(), round2).getStrokesBrutto());
	}

	@DisplayName("Close tournament by authorized user")
	@Transactional
	@Test
	void closeTournamentByAuthorizedUserTest(@Autowired PlayerService playerService,
											 @Autowired TournamentRepository tournamentRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournament = tournamentRepository.findAll().getFirst();

		tournamentService.closeTournament(tournament.getId());

		assertEquals(Cycle.STATUS_CLOSE, tournamentService.findAllTournamentsPageable(0).getFirst().getStatus());

	}

	@DisplayName("Attempt to close tournament by unauthorized user")
	@Transactional
	@Test
	void attemptToCloseTournamentByUnauthorizedUserTest(@Autowired TournamentRepository tournamentRepository) {

		UserDetails userDetails = new User("2", "fake", new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournamentId = tournamentRepository.findAll().getFirst().getId();

		assertThrows(UnauthorizedException.class, () -> this.tournamentService.closeTournament(tournamentId));
	}

	@DisplayName("Attempt to add player to the tournament by authorized user")
	@Transactional
	@Test
	void attemptToAddPlayerToTournamentByAuthorizedUserTest(@Autowired PlayerService playerService,
															@Autowired TournamentRepository tournamentRepository,
															@Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournament = tournamentRepository.findAll().getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(tournament.getId());

		tournamentService.addPlayer(tournamentPlayer);

		assertEquals(1, tournamentPlayerRepository.findAll().size());

		var tempTournamentPlayer = new TournamentPlayer();
		tempTournamentPlayer.setPlayerId(1L);
		tempTournamentPlayer.setTournamentId(tournament.getId());

		assertThrows(DuplicatePlayerInTournamentException.class, () -> this.tournamentService.addPlayer(tempTournamentPlayer));
	}

	@DisplayName("Attempt to add player to the tournament by unauthorized user")
	@Transactional
	@Test
	void attemptToAddPlayerToTournamentByUnauthorizedUserTest(@Autowired TournamentRepository tournamentRepository) {

		UserDetails userDetails = new User("2", "fake", new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournament = tournamentRepository.findAll().getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(tournament.getId());

		assertThrows(UnauthorizedException.class, () -> this.tournamentService.addPlayer(tournamentPlayer));
	}

	@DisplayName("Attempt to add player to the tournament but player does not exist")
	@Transactional
	@Test
	void attemptToAddPlayerToTournamentByAuthorizedUserButPlayerNotExistsTest(@Autowired PlayerService playerService,
															  @Autowired TournamentRepository tournamentRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournament = tournamentRepository.findAll().getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(100L);
		tournamentPlayer.setTournamentId(tournament.getId());

		assertThrows(NoSuchElementException.class, () -> this.tournamentService.addPlayer(tournamentPlayer));
	}

	@DisplayName("Attempt to add player to the tournament but tournament does not exist")
	@Transactional
	@Test
	void attemptToAddPlayerToTournamentByAuthorizedUserButTournamentNotExistsTest(@Autowired PlayerService playerService) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(100L);

		assertThrows(NoSuchElementException.class, () -> this.tournamentService.addPlayer(tournamentPlayer));
	}

	@DisplayName("Attempt to delete all tournament players by authorized user")
	@Transactional
	@Test
	void attemptToDeleteAllTournamentPlayersByAuthorizedUserTest(@Autowired PlayerService playerService,
																 @Autowired TournamentRepository tournamentRepository,
																 @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournament = tournamentRepository.findAll().getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setNick("Test");
		tournamentPlayer.setWhs(1F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		assertEquals(1, tournamentPlayerRepository.findAll().size());
		tournamentService.deletePlayers(tournament.getId());
		assertEquals(0, tournamentPlayerRepository.findAll().size());
	}

	@DisplayName("Attempt to delete all tournament players by unauthorized user")
	@Transactional
	@Test
	void attemptToDeleteAllTournamentPlayersByUnauthorizedUserTest(@Autowired TournamentRepository tournamentRepository) {

		UserDetails userDetails = new User("2", "fake", new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournamentId = tournamentRepository.findAll().getFirst().getId();


		assertThrows(UnauthorizedException.class, () -> tournamentService.deletePlayers(tournamentId));
	}

	@DisplayName("Attempt to delete all tournament players but results exist")
	@Transactional
	@Test
	void attemptToDeleteAllTournamentPlayersButResultsExistTest(@Autowired PlayerService playerService,
																 @Autowired TournamentRepository tournamentRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournament = tournamentRepository.findAll().getFirst();
		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(100);
		tournamentResult.setStrokesBrutto(100);
		tournamentResult.setStrokesNetto(100);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);

		var tournamentId = tournament.getId();

		assertThrows(DeleteTournamentPlayerException.class, () -> tournamentService.deletePlayers(tournamentId));

	}

	@DisplayName("Attempt to delete single tournament player by authorized user")
	@Transactional
	@Test
	void attemptToDeleteSingleTournamentPlayerByAuthorizedUserTest(@Autowired PlayerService playerService,
																 @Autowired TournamentRepository tournamentRepository,
																 @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournament = tournamentRepository.findAll().getFirst();

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setNick("Test");
		tournamentPlayer.setWhs(1F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		assertEquals(1, tournamentPlayerRepository.findAll().size());
		tournamentService.deletePlayer(tournament.getId(), 1L);
		assertEquals(0, tournamentPlayerRepository.findAll().size());
	}

	@DisplayName("Attempt to delete single tournament player by unauthorized user")
	@Transactional
	@Test
	void attemptToDeleteSingleTournamentPlayerByUnauthorizedUserTest(@Autowired TournamentRepository tournamentRepository) {

		UserDetails userDetails = new User("2", "fake", new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournamentId = tournamentRepository.findAll().getFirst().getId();

		assertThrows(UnauthorizedException.class, () -> tournamentService.deletePlayer(tournamentId, 1L));
	}

	@DisplayName("Attempt to delete single tournament player but results exist")
	@Transactional
	@Test
	void attemptToDeleteSingleTournamentPlayerButResultsExistTest(@Autowired PlayerService playerService,
																@Autowired TournamentRepository tournamentRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournament = tournamentRepository.findAll().getFirst();
		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(100);
		tournamentResult.setStrokesBrutto(100);
		tournamentResult.setStrokesNetto(100);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);

		var tournamentId = tournament.getId();

		assertThrows(DeleteTournamentPlayerException.class, () -> tournamentService.deletePlayer(tournamentId, 1L));

	}

	@DisplayName("Attempt to get tournament players")
	@Transactional
	@Test
	void attemptToGetTournamentPlayersTest(@Autowired TournamentRepository tournamentRepository,
										   @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var tournament = tournamentRepository.findAll().getFirst();
		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setNick("Test");
		tournamentPlayer.setWhs(1F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		assertEquals(1, tournamentService.getTournamentPlayers(tournament.getId()).size());
	}

	@DisplayName("Attempt to update tournament player handicap")
	@Transactional
	@Test
	void attemptToUpdateTournamentPlayerHcpButTournamentIsClosedTest(@Autowired TournamentRepository tournamentRepository,
										   @Autowired TournamentPlayerRepository tournamentPlayerRepository,
												@Autowired PlayerService playerService) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournament = new Tournament();
		tournament.setEndDate(new Date(1));
		tournament.setStartDate(new Date(1));
		tournament.setName("Test Cup");
		tournament.setPlayer(player);
		tournament.setStatus(Tournament.STATUS_CLOSE);
		tournament.setBestRounds(Common.ALL_ROUNDS);
		tournament.setMaxPlayHcp(54);
		tournament.setCanUpdateHcp(true);
		tournament.setPlayHcpMultiplayer(1f);
		tournament = tournamentRepository.save(tournament);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setNick("Test");
		tournamentPlayer.setWhs(1F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		var tournamentId = tournament.getId();
		var playerId = tournamentPlayer.getPlayerId();

		assertThrows(HcpChangeNotAllowedException.class, () -> tournamentService.updatePlayerHcp(tournamentId, playerId, 2F));
	}

	@DisplayName("Attempt to update tournament player handicap")
	@Transactional
	@Test
	void attemptToUpdateTournamentPlayerHcpTest(@Autowired TournamentRepository tournamentRepository,
												@Autowired TournamentPlayerRepository tournamentPlayerRepository,
												@Autowired PlayerService playerService) {

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);


		var tournament = tournamentRepository.findAll().getFirst();
		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setNick("Test");
		tournamentPlayer.setWhs(1F);
		tournamentPlayer.setSex(false);
		tournamentPlayerRepository.save(tournamentPlayer);

		tournamentService.updatePlayerHcp(tournamentPlayer.getTournamentId(), tournamentPlayer.getPlayerId(), 2F);

		assertEquals(2F, tournamentService.getTournamentPlayers(tournament.getId()).getFirst().getWhs());
	}

	@DisplayName("Attempt to add tee time with no tee time")
	@Transactional
	@Test
	void attemptToAddTeeTimeWithNoTeeTime(@Autowired TeeTimeRepository teeTimeRepository) {

		var teeTimeParameters = new TeeTimeParameters();
		teeTimeParameters.setTeeTimes(new ArrayList<>());

		tournamentService.addTeeTimes(1L, teeTimeParameters);

		assertEquals(0, teeTimeRepository.findAll().size());
	}

	@DisplayName("Attempt to add tee time by unauthorized user")
	@Transactional
	@Test
	void attemptToAddTeeTimeByUnauthorizedUserTest() {

		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var teeTimeParameters = new TeeTimeParameters();
		var teeTime = new TeeTime();
		var teeTimes = new ArrayList<TeeTime>();
		teeTimes.add(teeTime);
		teeTimeParameters.setTeeTimes(teeTimes);

		UserDetails userDetails = new User("2", "fake", new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		Long id = tournament.getId();
		assertThrows(UnauthorizedException.class, () -> tournamentService.addTeeTimes(id, teeTimeParameters));
	}

	@DisplayName("Attempt to add tee time by authorized user")
	@Transactional
	@Test
	void attemptToAddTeeTimeByAuthorizedUserTest(@Autowired TeeTimeParametersRepository teeTimeParametersRepository,
												 @Autowired PlayerService playerService) {

		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var teeTimeParameters = new TeeTimeParameters();
		teeTimeParameters.setFirstTeeTime("10:00");
		teeTimeParameters.setFlightSize(4);
		teeTimeParameters.setPublished(false);
		teeTimeParameters.setTeeTimeStep(10);

		var teeTime = new TeeTime();
		teeTime.setTime("10:00");
		teeTime.setFlight(1);
		teeTime.setHcp(10.0F);
		teeTime.setNick("Golfer");

		var teeTimes = new ArrayList<TeeTime>();
		teeTimes.add(teeTime);
		teeTimeParameters.setTeeTimes(teeTimes);

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		tournamentService.addTeeTimes(tournament.getId(), teeTimeParameters);

		assertEquals(1, teeTimeParametersRepository.findAll().size());
	}

	@DisplayName("Attempt to get tee times")
	@Transactional
	@Test
	void attemptToGetTeeTimes(@Autowired PlayerService playerService) {

		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var teeTimeParameters = new TeeTimeParameters();
		teeTimeParameters.setFirstTeeTime("10:00");
		teeTimeParameters.setFlightSize(4);
		teeTimeParameters.setPublished(false);
		teeTimeParameters.setTeeTimeStep(10);

		var teeTime = new TeeTime();
		teeTime.setTime("10:00");
		teeTime.setFlight(1);
		teeTime.setHcp(10.0F);
		teeTime.setNick("Golfer");

		var teeTimes = new ArrayList<TeeTime>();
		teeTimes.add(teeTime);
		teeTimeParameters.setTeeTimes(teeTimes);

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		tournamentService.addTeeTimes(tournament.getId(), teeTimeParameters);

		assertDoesNotThrow(() -> tournamentService.getTeeTimes(tournament.getId()));
	}

	@DisplayName("Attempt to delete tee times by unauthorized user")
	@Transactional
	@Test
	void attemptToDeleteTeeTimesByUnauthorizedUserTest() {

		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var teeTimeParameters = new TeeTimeParameters();
		var teeTime = new TeeTime();
		var teeTimes = new ArrayList<TeeTime>();
		teeTimes.add(teeTime);
		teeTimeParameters.setTeeTimes(teeTimes);

		UserDetails userDetails = new User("2", "fake", new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		Long id = tournament.getId();
		assertThrows(UnauthorizedException.class, () -> tournamentService.deleteTeeTimes(id));
	}

	@DisplayName("Attempt to delete tee times by authorized user")
	@Transactional
	@Test
	void attemptToDeleteTeeTimesByAuthorizedUserTest(@Autowired PlayerService playerService) {

		var tournament = tournamentService.findAllTournamentsPageable(0).getFirst();

		var teeTimeParameters = new TeeTimeParameters();
		teeTimeParameters.setFirstTeeTime("10:00");
		teeTimeParameters.setFlightSize(4);
		teeTimeParameters.setPublished(false);
		teeTimeParameters.setTeeTimeStep(10);

		var teeTime = new TeeTime();
		teeTime.setTime("10:00");
		teeTime.setFlight(1);
		teeTime.setHcp(10.0F);
		teeTime.setNick("Golfer");

		var teeTimes = new ArrayList<TeeTime>();
		teeTimes.add(teeTime);
		teeTimeParameters.setTeeTimes(teeTimes);

		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		tournamentService.addTeeTimes(tournament.getId(), teeTimeParameters);
		tournamentService.deleteTeeTimes(tournament.getId());

		assertNull(tournamentService.getTeeTimes(tournament.getId()));
	}

	@DisplayName("Send notification")
	@Transactional
	@Test
	void attemptToSendNotificationButPlayerNotProvidedEmailTest(@Autowired TournamentRepository tournamentRepository,
									   @Autowired PlayerService playerService,
									   @Autowired TournamentNotificationRepository tournamentNotificationRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();

		var tournament = tournamentRepository.findAll().getFirst();
		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(100);
		tournamentResult.setStrokesBrutto(100);
		tournamentResult.setStrokesNetto(100);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);

		var tournamentId = tournament.getId();

		var tournamentNotification = new TournamentNotification();
		tournamentNotification.setPlayerId(1L);
		tournamentNotification.setTournamentId(tournamentId);
		tournamentNotificationRepository.save(tournamentNotification);

		assertEquals(0, tournamentService.processNotifications(tournamentId, TournamentService.SORT_STB_NET));
	}

	@DisplayName("Send notification")
	@Transactional
	@Test
	void attemptToSendNotificationTest(@Autowired TournamentRepository tournamentRepository,
																@Autowired PlayerService playerService,
																@Autowired TournamentNotificationRepository tournamentNotificationRepository) {


		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		player.setEmail("grzegorz.malewicz@gmail.com");
		playerService.update(player);

		var tournament = tournamentRepository.findAll().getFirst();
		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(100);
		tournamentResult.setStrokesBrutto(100);
		tournamentResult.setStrokesNetto(100);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);

		var tournamentId = tournament.getId();

		var tournamentNotification = new TournamentNotification();
		tournamentNotification.setPlayerId(1L);
		tournamentNotification.setTournamentId(tournamentId);
		tournamentNotificationRepository.save(tournamentNotification);

		try {
			doNothing().when(emailService).sendEmail(any(), any(), any());
		} catch (Exception e) {
			fail("Method emailService.sendMail throws exception");
		}

		assertDoesNotThrow(() -> tournamentService.processNotifications(tournamentId, TournamentService.SORT_STB_NET));
		assertDoesNotThrow(() -> tournamentService.processNotifications(tournamentId, TournamentService.SORT_STB));
		assertDoesNotThrow(() -> tournamentService.processNotifications(tournamentId, TournamentService.SORT_STR_NET));
		assertDoesNotThrow(() -> tournamentService.processNotifications(tournamentId, TournamentService.SORT_STR));

		try {
			doThrow(MessagingException.class).when(emailService).sendEmail(any(), any(), any());
		} catch (Exception e) {
			fail("Method emailService.sendMail throws exception");
		}

		// verify if exception is caught
		assertThrows(GeneralException.class, () -> tournamentService.processNotifications(tournamentId, TournamentService.SORT_STB_NET));

		//remove notification
		tournamentService.removeNotification(tournamentId);

		assertEquals(0, tournamentNotificationRepository.findByTournamentId(tournamentId).size());

	}

	@DisplayName("Add notification for tournament")
	@Transactional
	@Test
	void attemptToAddNotificationTournamentTest(@Autowired TournamentRepository tournamentRepository,
													     @Autowired PlayerService playerService,
														 @Autowired PlayerRepository playerRepository,
													     @Autowired TournamentNotificationRepository tournamentNotificationRepository) {


		var player = playerService.getPlayer(1L).orElseThrow();
		try {
			player.setEmail(StringUtility.encryptString("test@gmail.com", "testPassword"));
		} catch (Exception e) {
			fail("Should not throw any exception");
		}
		playerRepository.save(player);

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);


		var tournament = tournamentRepository.findAll().getFirst();
		tournament.setStatus(Tournament.STATUS_CLOSE);
		tournamentRepository.save(tournament);

		tournamentService.addNotification(tournament.getId());

		assertEquals(0, tournamentNotificationRepository.findAll().size());

		tournament.setStatus(Tournament.STATUS_OPEN);
		tournamentRepository.save(tournament);

		tournamentService.addNotification(tournament.getId());

		assertEquals(1, tournamentNotificationRepository.findAll().size());

	}

	@DisplayName("Add notification for opened tournament")
	@Transactional
	@Test
	void attemptToAddNotificationForOpenedTournamentButNoEmailSetTest(@Autowired TournamentRepository tournamentRepository,
														 @Autowired PlayerService playerService) {


		var player = playerService.getPlayer(1L).orElseThrow();

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);


		var tournament = tournamentRepository.findAll().getFirst();

		Long id = tournament.getId();
		assertThrows(MailNotSetException.class, () -> tournamentService.addNotification(id));
	}

	@AfterAll
	static void done(@Autowired RoundRepository roundRepository,
			@Autowired TournamentRepository tournamentRepository, @Autowired TournamentResultRepository tr) {

		roundRepository.deleteAll();
		tr.deleteAll();
		tournamentRepository.deleteAll();

		log.info("Clean up completed");

	}

}
