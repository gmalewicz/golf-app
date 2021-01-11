package com.greg.golf.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import java.util.Set;

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
import com.greg.golf.entity.TournamentResult;
import com.greg.golf.entity.TournamentRound;
import com.greg.golf.repository.HoleRepository;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.repository.TournamentResultRepository;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class TournamentServiceTest {

	private static Player player;
	private static Round round;
	private static Tournament tournament;
	// private static Course course;

	@Autowired
	private TournamentService tournamentService;
	
	@Autowired TournamentResultRepository tournamentResultRepository;

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
		round.setRoundDate(new Date(1));
		round.setMatchPlay(false);
		round.setScoreCard(new ArrayList<ScoreCard>());
		for (int i = 0; i < 18; i++) {
			ScoreCard scoreCard = new ScoreCard();
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
		
		playerRoundRepository.updatePlayerRoundInfo(player.getWhs(), 135, 70.3f, 2l, 0, player.getId(), round.getId());

		tournament = new Tournament();
		tournament.setEndDate(new Date(1));
		tournament.setStartDate(new Date(1));
		tournament.setName("Test Cup");
		tournament.setPlayer(player);
		tournament = tournamentRepository.save(tournament);

		log.info("Set up completed");
	}
	
	@DisplayName("Calculate net and gross STB")
	@Transactional
	@Test
	void calculateNetAndGrossSTBTest(@Autowired RoundRepository roundRepository) {
		
		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);
		
		Round redRound = roundRepository.findById(round.getId()).orElseThrow();
		tournamentService.updateSTB(tournamentResult, redRound, null, player);
			
		log.info("STB netto: " + tournamentResult.getStbNet());
		log.info("STB gross: " + tournamentResult.getStbGross());
		assertEquals(62, tournamentResult.getStbNet().intValue());
		assertEquals(17, tournamentResult.getStbGross().intValue());
	}
	
	
	@DisplayName("Calculate corrected strokes")
	@Transactional
	@Test
	void calculateCorrectedStrokesTest(@Autowired HoleRepository holeRepository) {
		
		
		round.getCourse().setHoles(holeRepository.findByCourse(round.getCourse()));
		
		round.getScoreCard().get(0).setStroke(20);
		int correctedScore = tournamentService.getCorrectedStrokes(player, round);
		
			
		log.info("corrected Strokes: " + correctedScore);
		assertEquals(93, correctedScore);
	}
	
	
	@DisplayName("Calculate score differential")
	@Transactional
	@Test
	void calculateScoreDifferentialTest(@Autowired HoleRepository holeRepository) {
		
		
		round.getCourse().setHoles(holeRepository.findByCourse(round.getCourse()));
		round.getScoreCard().get(0).setStroke(20);
		
		PlayerRound playerRound = new PlayerRound();
		playerRound.setSr(113);
		playerRound.setCr((float)0);
		
		
		
		float scoreDifferential = tournamentService.getScoreDifferential(playerRound, round, player);
		
			
		log.info("score differential: " + scoreDifferential);
		assertEquals(93, (int)scoreDifferential);
	}
	
	@DisplayName("Save tournament round")
	@Transactional
	@Test
	void saveTournamentRoundTest() {
		
		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(0);
		tournamentResult.setStrokesBrutto(0);
		tournamentResult.setStrokesNetto(0);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);
		
		TournamentRound tournamentRound = tournamentService.addTournamentRound(1, 1, 1, 1, 1, "test", tournamentResult);
		
		//log.info("score differential: " + scoreDifferential);
		assertNotNull(tournamentRound.getId());
	}
	
	
	
	@DisplayName("Get all tournaments")
	@Transactional
	@Test
	void getAllTournamentsTest() {

		assertEquals(1, tournamentService.findAllTournamnets().size());

	}
	

	@DisplayName("Calculate gross strokes")
	@Transactional
	@Test
	void getGrossStrokesTest() {

		int grossStrokes = tournamentService.getGrossStrokes(player, round);

		assertEquals(90, grossStrokes);

	}

	@DisplayName("Calculate net strokes")
	@Transactional
	@Test
	void getNetStrokesTest() {

		int netStrokes = tournamentService.getNetStrokes(player, round, 99, null);

		assertEquals(54, netStrokes);

	}

	@DisplayName("Add round to tournament")
	@Transactional
	@Test
	void addRoundTest() {

		tournamentService.addRound(tournament.getId(), round, false);
		assertEquals(tournament, round.getTournament());

	}

	@DisplayName("Adding the new round to the tournamnet result")
	@Transactional
	@Test
	void addNewRoundToTournamnetResultTest(@Autowired RoundRepository roundRepository) {
		
		Round redRound = roundRepository.findById(round.getId()).orElseThrow();
		redRound.setTournament(tournament);
		tournamentService.updateTournamentResult(redRound);
		TournamentResult tr = tournamentService.findByPlayerAndTournament(player, tournament).orElseThrow();
		assertEquals(90, tr.getStrokesBrutto().intValue());

	}
	
	@DisplayName("Update the tournamnet result with the new round")
	@Transactional
	@Test
	void updateTournamnetResultWithNewRoundTest(@Autowired RoundRepository roundRepository) {
		
		Round redRound = roundRepository.findById(round.getId()).orElseThrow();
		redRound.setTournament(tournament);
		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(100);
		tournamentResult.setStrokesBrutto(100);
		tournamentResult.setStrokesNetto(100);
		tournamentResult.setStbGross(0);
		tournamentResult.setStbNet(0);
		tournamentResult.setPlayer(player);
		tournamentResult.setTournament(tournament);
		tournamentResultRepository.save(tournamentResult);
		tournamentService.updateTournamentResult(redRound);
		TournamentResult tr = tournamentService.findByPlayerAndTournament(player, tournament).orElseThrow();
		assertEquals(190, tr.getStrokesBrutto().intValue());

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
