package com.greg.golf.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import com.greg.golf.entity.*;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.DeleteTournamentPlayerException;
import com.greg.golf.error.DuplicatePlayerInTournamentException;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.repository.*;
import com.greg.golf.security.JwtRequestFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import com.greg.golf.service.events.RoundEvent;
import com.greg.golf.util.GolfPostgresqlContainer;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class TournamentServiceTest {

	@SuppressWarnings("unused")
	@MockBean
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

	private static Long roundId;

	@BeforeAll
	public static void setup(@Autowired PlayerService playerService, @Autowired CourseService courseService,
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
		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);

		TournamentRound tournamentRound =
				tournamentService.addTournamentRound(1, 1, 1, 1, 1,
						"test", tournamentResult, false, roundId);

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
		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);

		TournamentRound tournamentRound =
				tournamentService.addTournamentRound(1, 1, 1, 1, 1,
						"test", tournamentResult, false, roundId);

		tournamentResult.setTournamentRound(new ArrayList<>());
		tournamentResult.getTournamentRound().add(tournamentRound);
		tournamentResultRepository.save(tournamentResult);

		tournamentService.deleteResult(tournamentResult.getId());
		Assertions.assertEquals(0, tournamentService.findAllTournamentsPageable(0).get(0).getTournamentResult().size());
	}


	@DisplayName("Calculate net and gross STB")
	@Transactional
	@Test
	void calculateNetAndGrossSTBTest(@Autowired RoundRepository roundRepository, @Autowired PlayerService playerService) {

		var player = playerService.getPlayer(1L).orElseThrow();

		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setStrokeRounds(1);
		tournamentResult.setPlayer(player);
		tournamentResult.setTournament(tournamentService.findAllTournamentsPageable(0).get(0));
		tournamentResultRepository.save(tournamentResult);

		var retRound = roundRepository.findAll().get(0);
		tournamentService.updateSTB(tournamentResult, retRound, null, player, 38.4F);

		log.info("STB net: " + tournamentResult.getStbNet());
		log.info("STB gross: " + tournamentResult.getStbGross());
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
		var round = roundRepository.findAll().get(0);

		//round.setCourse(course);

		round.getCourse().setHoles(holeRepository.findByCourse(course));

		round.getScoreCard().get(0).setStroke(20);
		var correctedScore = tournamentService.getCorrectedStrokes(player, round);

		log.info("corrected Strokes: " + correctedScore);
		Assertions.assertEquals(91, correctedScore);
	}

	@DisplayName("Calculate score differential")
	@Transactional
	@Test
	void calculateScoreDifferentialTest(@Autowired HoleRepository holeRepository, @Autowired PlayerService playerService,
										@Autowired RoundRepository roundRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();
		var round = roundRepository.findAll().get(0);

		round.getCourse().setHoles(holeRepository.findByCourse(round.getCourse()));
		round.getScoreCard().get(0).setStroke(20);

		var playerRound = new PlayerRound();
		playerRound.setSr(113);
		playerRound.setCr((float) 0);

		var scoreDifferential = tournamentService.getScoreDifferential(playerRound, round, player);

		log.info("score differential: " + scoreDifferential);
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
		tournamentResult.setTournament(tournamentService.findAllTournamentsPageable(0).get(0));
		tournamentResultRepository.save(tournamentResult);

		TournamentRound tournamentRound =
				tournamentService.addTournamentRound(1, 1, 1, 1, 1,
											"test", tournamentResult, false, 1);

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

		var tournament = tournamentRepository.findAll().get(0);
		tournament.setBestRounds(1);
		tournamentRepository.save(tournament);

		Assertions.assertEquals(0, tournamentService.findAllTournamentsResults(tournament.getId()).size());
	}


	@DisplayName("Calculate gross strokes")
	@Transactional
	@Test
	void getGrossStrokesTest(@Autowired PlayerService playerService, @Autowired RoundRepository roundRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();
		var round = roundRepository.findAll().get(0);

		var grossStrokes = tournamentService.getGrossStrokes(player, round);

		Assertions.assertEquals(90, grossStrokes);

	}

	@DisplayName("Calculate net strokes")
	@Transactional
	@Test
	void getNetStrokesTest(@Autowired PlayerService playerService, @Autowired RoundRepository roundRepository) {

		var player = playerService.getPlayer(1L).orElseThrow();
		var round = roundRepository.findAll().get(0);

		var netStrokes = tournamentService.getNetStrokes(player, round, 99, null, 38.4F);

		Assertions.assertEquals(54, netStrokes);

	}

	@DisplayName("Calculate net strokes where net strokes is lower than 0")
	@Transactional
	@Test
	void getNetStrokesLowerThan0Test(@Autowired PlayerService playerService, @Autowired RoundRepository roundRepository) {

		var round = roundRepository.findAll().get(0);
		var player = playerService.getPlayer(1L).orElseThrow();

		var netStrokes = tournamentService.getNetStrokes(player, round, 22, null, 38.4F);

		Assertions.assertEquals(0, netStrokes);

	}

	@DisplayName("Should not allow add the same round to tournament twice")
	@Transactional
	@Test
	void addTesSameRoundTwiceTest(@Autowired RoundRepository roundRepository,
								  @Autowired TournamentPlayerRepository tournamentPlayerRepository,
								  @Autowired TournamentRoundRepository tournamentRoundRepository) {

		var round = roundRepository.findAll().get(0);
		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);

		tournamentPlayerRepository.save(tournamentPlayer);

		tournamentService.addRound(tournament.getId(), round.getId(), true);
		assertEquals(1L, tournamentRoundRepository.count());

		Long tournamentId = tournament.getId();
		Long roundId = round.getId();

		tournamentService.addRound(tournamentId, roundId, true);
		assertEquals(1L, tournamentRoundRepository.count());
	}

	@DisplayName("Adding the new round to the tournament result")
	@Transactional
	@Test
	void addNewRoundToTournamentResultTest(@Autowired RoundRepository roundRepository,
										   @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var round = roundRepository.findAll().get(0);
		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);

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

		var round = roundRepository.findAll().get(0);
		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);

		tournamentPlayerRepository.save(tournamentPlayer);

		round.getScoreCard().get(0).setStroke(Common.HOLE_GIVEN_UP);
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

		var round = roundRepository.findAll().get(0);

		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);
		tournament.setBestRounds(1);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);

		tournamentPlayerRepository.save(tournamentPlayer);


		round.getScoreCard().get(0).setStroke(Common.HOLE_GIVEN_UP);
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
		var round = roundRepository.findAll().get(0);
		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
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


		var round = rounds.get(0);

		// set up 1 round as number of best rounds
		var tournament = tournamentRepository.findAll().get(0);
		tournament.setBestRounds(1);
		tournament = tournamentRepository.save(tournament);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
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

		//playerRoundRepository.updatePlayerRoundInfo(player.getWhs(), 135, 70.3f, 2L, 0, player.getId(), round.getId());
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
		var round = roundRepository.findAll().get(0);
		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var redRound = roundRepository.findById(round.getId()).orElseThrow();

		redRound.getScoreCard().get(0).setStroke(Common.HOLE_GIVEN_UP);
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

		var round = roundRepository.findAll().get(0);
		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayerRepository.save(tournamentPlayer);

		tournamentService.updateTournamentResult(round, tournament);

		// update the round which was already added to tournament
		round.getScoreCard().get(0).setStroke(15);
		roundRepository.save(round);

		// try to update the tournament
		var roundEvent = new RoundEvent(this, round);
		tournamentService.handleRoundEvent(roundEvent);

		Assertions.assertEquals(15, round.getScoreCard().get(0).getStroke().intValue());

		var tr = tournamentResultRepository.findByTournament(tournament).orElseThrow();
		Assertions.assertEquals(90, tr.getStrokesBrutto().intValue());

	}


	@DisplayName("Should add the the round to the tournament and update tournament result")
	@Transactional
	@Test
	void addTheNewRoundAndUpdateTournamentResultTest(@Autowired RoundRepository roundRepository,
													 @Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var round = roundRepository.findAll().get(0);
		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
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

		var round = roundRepository.findAll().get(0);
		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);
		tournamentPlayerRepository.save(tournamentPlayer);

		tournamentService.updateTournamentResult(round, tournament);

		var roundResults = tournamentResultRepository.findAll().get(0);

		tournamentService.getTournamentRoundsForResult(roundResults.getId());

		Assertions.assertEquals(1, tournamentService.getTournamentRoundsForResult(roundResults.getId()).size());

	}

	@DisplayName("Should return rounds (which exists) applicable for tournament")
	@Transactional
	@Test
	void getApplicableRoundsForTournamentTest(@Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);

		tournamentPlayerRepository.save(tournamentPlayer);

		var rndLst = tournamentService.getAllPossibleRoundsForTournament(tournament.getId());

		Assertions.assertEquals(1, rndLst.size());

	}

	@DisplayName("Should return rounds (which does not exist) applicable for tournament")
	@Transactional
	@Test
	void getApplicableRoundsForTournamentButRoundNotExistTest(@Autowired TournamentPlayerRepository tournamentPlayerRepository,
															  @Autowired RoundRepository roundRepository) {

		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);

		tournamentPlayerRepository.save(tournamentPlayer);

		roundRepository.deleteAll();

		var rndLst = tournamentService.getAllPossibleRoundsForTournament(tournament.getId());

		Assertions.assertEquals(0, rndLst.size());

	}

	@DisplayName("Should attempt add round to tournament for player which is not participant")
	@Transactional
	@Test
	void addRoundToTournamentForPlayerNotParticipantTest(@Autowired TournamentPlayerRepository tournamentPlayerRepository) {

		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(2L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);

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
		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setNick("golfer");
		tournamentPlayer.setWhs(10.0F);

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

		var tournament = tournamentRepository.findAll().get(0);

		tournamentService.closeTournament(tournament.getId());

		assertEquals(Cycle.STATUS_CLOSE, tournamentService.findAllTournamentsPageable(0).get(0).getStatus());

	}

	@DisplayName("Attempt to close tournament by unauthorized user")
	@Transactional
	@Test
	void attemptToCloseTournamentByUnauthorizedUserTest(@Autowired TournamentRepository tournamentRepository) {

		UserDetails userDetails = new User("2", "fake", new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournamentId = tournamentRepository.findAll().get(0).getId();

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

		var tournament = tournamentRepository.findAll().get(0);

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

		var tournament = tournamentRepository.findAll().get(0);

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

		var tournament = tournamentRepository.findAll().get(0);

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

		var tournament = tournamentRepository.findAll().get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setNick("Test");
		tournamentPlayer.setWhs(1F);
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

		var tournamentId = tournamentRepository.findAll().get(0).getId();


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

		var tournament = tournamentRepository.findAll().get(0);
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

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		var tournament = tournamentRepository.findAll().get(0);

		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setNick("Test");
		tournamentPlayer.setWhs(1F);
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

		var tournamentId = tournamentRepository.findAll().get(0).getId();

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

		var tournament = tournamentRepository.findAll().get(0);
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

		var tournament = tournamentRepository.findAll().get(0);
		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setNick("Test");
		tournamentPlayer.setWhs(1F);
		tournamentPlayerRepository.save(tournamentPlayer);

		assertEquals(1, tournamentService.getTournamentPlayers(tournament.getId()).size());
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


		var tournament = tournamentRepository.findAll().get(0);
		var tournamentPlayer = new TournamentPlayer();
		tournamentPlayer.setPlayerId(1L);
		tournamentPlayer.setTournamentId(tournament.getId());
		tournamentPlayer.setNick("Test");
		tournamentPlayer.setWhs(1F);
		tournamentPlayerRepository.save(tournamentPlayer);

		tournamentService.updatePlayer(tournamentPlayer.getTournamentId(), tournamentPlayer.getPlayerId(), 2F);

		assertEquals(2F, tournamentService.getTournamentPlayers(tournament.getId()).get(0).getWhs());
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
	void attemptToAddTeeTimeByUnauthorizedUserTest(@Autowired TournamentRepository tournamentRepository) {

		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

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

		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

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
	void attemptToGetTeeTimes(@Autowired TeeTimeRepository teeTimeRepository,
							  @Autowired PlayerService playerService) {

		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

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

		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

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
	void attemptToDeleteTeeTimesByAuthorizedUserTest(@Autowired TeeTimeParametersRepository teeTimeParametersRepository,
												 @Autowired PlayerService playerService) {

		var tournament = tournamentService.findAllTournamentsPageable(0).get(0);

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

	@AfterAll
	public static void done(@Autowired RoundRepository roundRepository,
			@Autowired TournamentRepository tournamentRepository, @Autowired TournamentResultRepository tr) {

		roundRepository.deleteAll();
		tr.deleteAll();
		tournamentRepository.deleteAll();

		log.info("Clean up completed");

	}

}
