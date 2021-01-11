package com.greg.golf.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.entity.CourseTee;
import com.greg.golf.entity.Hole;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.PlayerRound;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.entity.Tournament;
import com.greg.golf.entity.TournamentResult;
import com.greg.golf.entity.TournamentRound;
import com.greg.golf.error.RoundAlreadyAddedToTournamentException;
import com.greg.golf.error.TooFewHolesForTournamentException;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.TournamentRepository;
import com.greg.golf.repository.TournamentResultRepository;
import com.greg.golf.repository.TournamentRoundRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("tournamentService")
public class TournamentService {

	private static final int TOURNAMENT_HOLES = 18;

	@Autowired
	private TournamentResultRepository tournamentResultRepository;

	@Autowired
	private TournamentRepository tournamentRepository;

	@Autowired
	private RoundService roundService;

	@Autowired
	private CourseService courseService;

	@Autowired
	private PlayerRoundRepository playerRoundRepository;

	@Autowired
	private ScoreCardService scoreCardService;

	@Autowired
	private TournamentRoundRepository tournamentRoundRepository;

	@Transactional
	public List<Tournament> findAllTournamnets() {
		return tournamentRepository.findAll();
	}

	@Transactional
	public List<TournamentResult> findAllTournamnetsResults(Tournament tournamnet) {
		return tournamentResultRepository.findByTournamentOrderByPlayedRoundsDescStbNetDesc(tournamnet);
	}

	@Transactional
	public Optional<TournamentResult> findByPlayerAndTournament(Player player, Tournament tournament) {
		return tournamentResultRepository.findByPlayerAndTournament(player, tournament);
	}

	// test required
	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	@Transactional
	public Optional<TournamentResult> findByTournament(Tournament tournament) {
		return tournamentResultRepository.findByTournament(tournament);
	}

	@Transactional
	public Tournament addTournamnet(Tournament tournament) {

		// just to make the time adjustment
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(tournament.getEndDate());
		calendar.roll(Calendar.DATE, 1);
		calendar.roll(Calendar.HOUR, 23);

		tournament.setEndDate(calendar.getTime());

		return tournamentRepository.save(tournament);
	}

	@Transactional
	public Tournament addRound(Long tournamnetId, Round round, boolean updateResults) {

		// preparation and checking
		if (round.getPlayer() == null || round.getPlayer().isEmpty()) {

			round.setPlayer(new HashSet<>());
			List<PlayerRound> playerRounds = roundService.getByRoundId(round.getId());
			playerRounds.forEach(pr -> {

				Player player = new Player();
				player.setId(pr.getPlayerId());
				player.setWhs(pr.getWhs());
				round.getPlayer().add(player);
				round.getCourse().setTees(new ArrayList<>());
				CourseTee courseTee = new CourseTee();
				courseTee.setId(pr.getTeeId());
				round.getCourse().getTees().add(courseTee);
				log.debug("Player added");
			});
		}

		if (round.getScoreCard() == null || round.getScoreCard().isEmpty()) {

			round.setScoreCard(scoreCardService.listByRound(round));
		}

		// first check if round is not already added to that tournament
		Tournament tournament = tournamentRepository.findById(tournamnetId).orElseThrow();

		if (tournament.getRound().contains(round)) {
			log.warn("Attempt to add twice the same round to tournament");
			throw new RoundAlreadyAddedToTournamentException();
		}

		// add round to tournament
		tournament.addRound(round);
		tournamentRepository.save(tournament);
		if (updateResults) {
			updateTournamentResult(round);
		}
		return tournament;
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void updateTournamentResult(Round round) {

		// first verify if round has 18 holes played for each player
		verifyRoundCorecteness(round);

		// iterate through round players and check if they already added
		round.getPlayer().forEach(player -> {

			PlayerRound playerRound = roundService.getForPlayerRoundDetails(player.getId(), round.getId());

			Optional<TournamentResult> tournamentResultOpt = tournamentResultRepository
					.findByPlayerAndTournament(player, round.getTournament());
			tournamentResultOpt.ifPresentOrElse(tournamentResult -> {
				log.debug("Attempting to update tournamnet result");
				// first check if the round has not been already added
				if (playerRound.getTournamentId() == null) {
					
					tournamentResult.setPlayedRounds(tournamentResult.getPlayedRounds() + 1);
					int grossStrokes = getGrossStrokes(player, round);
					tournamentResult.setStrokesBrutto(tournamentResult.getStrokesBrutto() + grossStrokes);
					int netStrokes = getNetStrokes(player, round, grossStrokes, playerRound);
					tournamentResult.setStrokesNetto(tournamentResult.getStrokesNetto() + netStrokes);
					List<Integer> stb = updateSTB(tournamentResult, round, playerRound, player);
					// save entity
					tournamentResultRepository.save(tournamentResult);
					addTournamentRound(stb.get(1), stb.get(0), grossStrokes, netStrokes,
							getScoreDifferential(playerRound, round, player), round.getCourse().getName(), tournamentResult);
					
				}

			}, () -> {
				log.debug("Attempting to add the new round to tournamnet result");
				// if it is the first record to be added to result than create it
				TournamentResult tournamentResult = buildEmptyTournamentResult(player);
				// get gross and net strokes
				tournamentResult.setStrokesBrutto(getGrossStrokes(player, round));
				tournamentResult.setStrokesNetto(
						getNetStrokes(player, round, tournamentResult.getStrokesBrutto(), playerRound));
				tournamentResult.setTournament(round.getTournament());
				// update stb results
				List<Integer> stb = updateSTB(tournamentResult, round, playerRound, player);
				// save entity
				tournamentResultRepository.save(tournamentResult);
				
				addTournamentRound(stb.get(1), stb.get(0), tournamentResult.getStrokesBrutto(), tournamentResult.getStrokesNetto(),
						getScoreDifferential(playerRound, round, player), round.getCourse().getName(), tournamentResult);

			});
			
			// set tournament id in player_round
			playerRound.setTournamentId(round.getTournament().getId());
			playerRoundRepository.save(playerRound);

		});
	}

	public TournamentRound addTournamentRound(int stbGross, int stbNet, int strokesGross, int strokesNet,
			float scrDiff, String courseName, TournamentResult tournamentResult) {

		TournamentRound tournamnetRound = new TournamentRound();
		tournamnetRound.setCourseName(courseName);
		tournamnetRound.setScrDiff(scrDiff);
		tournamnetRound.setStbGross(stbGross);
		tournamnetRound.setStbNet(stbNet);
		tournamnetRound.setStrokesBrutto(strokesGross);
		tournamnetRound.setStrokesNetto(strokesNet);
		tournamnetRound.setTournamentResult(tournamentResult);
		
		tournamnetRound = tournamentRoundRepository.save(tournamnetRound);
		
		return tournamnetRound;
	}
	
	public List<TournamentRound> getTournamentRoundsForResult(Long resultId) {
		
		TournamentResult tr = new TournamentResult();
		tr.setId(resultId);
		
		return tournamentRoundRepository.findByTournamentResultOrderByIdAsc(tr);
		
	}
	

	// calculate score differential
	public float getScoreDifferential(PlayerRound playerRound, Round round, Player player) {
		
		return (113 / (float)playerRound.getSr()) * (getCorrectedStrokes(player, round) - playerRound.getCr());

	}

	// calculate gross strokes
	public int getGrossStrokes(Player player, Round round) {

		log.debug("player: " + player);
		log.debug("round: " + round);

		// calculate gross result
		int grossStrokes = round.getScoreCard().stream().filter(scoreCard -> scoreCard.getPlayer().equals(player))
				.mapToInt(ScoreCard::getStroke).sum();
		log.debug("Calculated gross strokes: " + grossStrokes);
		return grossStrokes;
	}

	// calculate corrected strokes
	public int getCorrectedStrokes(Player player, Round round) {

		log.debug("player: " + player);
		log.debug("round: " + round);

		List<Hole> holes = round.getCourse().getHoles();

		// calculate gross result
		int grossStrokes = round.getScoreCard().stream().filter(scoreCard -> scoreCard.getPlayer().equals(player))
				.mapToInt(scoreCard -> {

					if ((scoreCard.getHcp() + 2 + holes.get(scoreCard.getHole() - 1).getPar()) < scoreCard
							.getStroke()) {
						return scoreCard.getHcp() + 2 + holes.get(scoreCard.getHole() - 1).getPar();
					} else {
						return scoreCard.getStroke();
					}

				}).sum();

		log.debug("Calculated corrected strokes: " + grossStrokes);
		return grossStrokes;
	}

	// calculate net strokes
	public int getNetStrokes(Player player, Round round, int grossStrokes, PlayerRound playerRound) {

		// calculate course HCP
		int courseHCP = getCourseHCP(playerRound, round, player);

		int netStrokes = grossStrokes - courseHCP;
		if (netStrokes < 0) {
			netStrokes = 0;
		}

		log.debug("Calculated net strokes: " + netStrokes);
		return netStrokes;
	}

	// returns STB netto at index 0 and STB gross at index 1
	public List<Integer> updateSTB(TournamentResult tournamentResult, Round round, PlayerRound playerRound, Player player) {

		//create List of ret values 
		List<Integer> retStb= new ArrayList<>();
		
		// calculate course HCP
		int courseHCP = getCourseHCP(playerRound, round, player);

		// calculate hole HCP for player
		int hcpAll = (int) Math.floor((double)courseHCP / 18);
		int hcpIncMaxHole = courseHCP - (hcpAll * 18);
		log.debug("hcpAll " + hcpAll);
		log.debug("hcpIncMaxHole " + hcpIncMaxHole);

		// fill all holes with hcpAll value or initialize it with 0 if hcpAll is 0
		round.getScoreCard().forEach(scoreCard -> scoreCard.setHcp(hcpAll));
		if (round.getCourse().getHoles() == null || round.getCourse().getHoles().isEmpty()) {
			log.debug("getting holes");
			round.getCourse().setHoles(courseService.getHoles(round.getCourse()));
		}
		List<Hole> holes = round.getCourse().getHoles();
		// get list of score card for player
		List<ScoreCard> playerScoreCard = round.getScoreCard().stream()
				.filter(scoreCard -> scoreCard.getPlayer().equals(player)).collect(Collectors.toList());
		playerScoreCard.forEach(scoreCard -> {
			if (hcpIncMaxHole > 0 && holes.get(scoreCard.getHole() - 1).getSi() <= hcpIncMaxHole) {
				// if some holes needs hcp update increase them
				scoreCard.setHcp(hcpAll + 1);
			}

			// update STB netto for each hole
			scoreCard.setStbNet(
					holes.get(scoreCard.getHole() - 1).getPar() - (scoreCard.getStroke() - scoreCard.getHcp()) + 2);
			if (scoreCard.getStbNet() < 0) {
				scoreCard.setStbNet(0);
			}
			// update STB brutto for each hole
			scoreCard.setStbGross(holes.get(scoreCard.getHole() - 1).getPar() - scoreCard.getStroke() + 2);
			if (scoreCard.getStbGross() < 0) {
				scoreCard.setStbGross(0);
			}
		});

		
		retStb.add(playerScoreCard.stream().mapToInt(ScoreCard::getStbNet).sum());
		tournamentResult.setStbNet(
				tournamentResult.getStbNet() + retStb.get(0));

		retStb.add(playerScoreCard.stream().mapToInt(ScoreCard::getStbGross).sum());
		tournamentResult.setStbGross(
				tournamentResult.getStbGross() +  retStb.get(1));
		
		return retStb;
	}

	// test required
	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	@Transactional
	public List<Round> getAllPossibleRoundsForTournament(Long tournamentId) {

		Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow();
		return roundService.findByDates(tournament.getStartDate(), tournament.getEndDate());

	}

	private int getCourseHCP(PlayerRound playerRound, Round round, Player player) {

		if (playerRound == null) {

			playerRound = roundService.getForPlayerRoundDetails(player.getId(), round.getId());
		}

		CourseTee courseTee = courseService.getTeeByid(playerRound.getTeeId()).orElseThrow();

		// calculate course HCP
		int courseHCP = Math
				.round(playerRound.getWhs() * courseTee.getSr() / 113 + courseTee.getCr() - round.getCourse().getPar());

		log.debug("Course SR: " + courseTee.getSr());
		log.debug("Course CR: " + courseTee.getCr());
		log.debug("Course Par: " + round.getCourse().getPar());
		log.debug("Calculated course HCP: " + courseHCP);

		return courseHCP;
	}

	private TournamentResult buildEmptyTournamentResult(Player player) {

		TournamentResult tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(1);
		tournamentResult.setPlayer(player);
		tournamentResult.setStbNet(0);
		tournamentResult.setStbGross(0);

		return tournamentResult;
	}

	// verifies if all score cards have all 18 holes filled
	private boolean verifyRoundCorecteness(Round round) {

		round.getPlayer().forEach(player -> {

			// calculate played holes
			int playedHoles = (int) round.getScoreCard().stream()
					.filter(scoreCard -> scoreCard.getPlayer().equals(player) && scoreCard.getStroke() > 0).count();
			if (playedHoles != TOURNAMENT_HOLES) {
				throw new TooFewHolesForTournamentException();
			}
		});
		return true;
	}
}
