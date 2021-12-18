package com.greg.golf.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
import com.greg.golf.service.events.RoundEvent;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service("tournamentService")
@RequiredArgsConstructor
public class TournamentService {

	private static final int TOURNAMENT_HOLES = 18;

	private final TournamentResultRepository tournamentResultRepository;
	private final TournamentRepository tournamentRepository;
	private final RoundService roundService;
	private final CourseService courseService;
	private final PlayerRoundRepository playerRoundRepository;
	private final TournamentRoundRepository tournamentRoundRepository;

	@Transactional
	public List<Tournament> findAllTournaments() {
		return tournamentRepository.findAll(Sort.by(Sort.Direction.DESC, "endDate"));
	}

	@Transactional
	public List<TournamentResult> findAllTournamentsResults(Tournament tournament) {
		return tournamentResultRepository.findByTournamentOrderByPlayedRoundsDescStbNetDesc(tournament);
	}

	@Transactional
	public Tournament addTournament(Tournament tournament) {

		// just to make the time adjustment
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(tournament.getEndDate());
		calendar.roll(Calendar.DATE, 1);
		calendar.roll(Calendar.HOUR, 23);

		tournament.setEndDate(calendar.getTime());

		return tournamentRepository.save(tournament);
	}

	@Transactional
	public Tournament addRound(Long tournamentId, Long roundId, boolean updateResults) {
		
		// first find the round in database
		var round = roundService.getWithPlayers(roundId).orElseThrow();

		// get tournament object
		var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

		// next check if round is not already added to that tournament
		if (tournament.getRound().contains(round)) {
			log.warn("Attempt to add twice the same round to tournament");
			throw new RoundAlreadyAddedToTournamentException();
		}
	
		// add round to tournament
		tournament.addRound(round);
		tournamentRepository.save(tournament);
		
		// update tournament result
		if (updateResults) {
			updateTournamentResult(round);
		}
		return tournament;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@EventListener
	public void handleRoundEvent(RoundEvent roundEvent) {
		log.debug("Handling round event...");
		updateTournamentResult(roundEvent.getRound());
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateTournamentResult(Round round) {

		// first verify if round has 18 holes played for each player
		verifyRoundCorecteness(round);

		// iterate through round players and check if they already added
		round.getPlayer().forEach(player -> {

			var playerRound = roundService.getForPlayerRoundDetails(player.getId(), round.getId());

			Optional<TournamentResult> tournamentResultOpt = tournamentResultRepository
					.findByPlayerAndTournament(player, round.getTournament());
			tournamentResultOpt.ifPresentOrElse(tournamentResult -> {
				log.debug("Attempting to update tournament result");
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
							getScoreDifferential(playerRound, round, player), round.getCourse().getName(),
							tournamentResult);

				} else {
					log.warn("Attempt to update round which is already part of the tournament");
				}

			}, () -> {
				log.debug("Attempting to add the new round to tournament result");
				// if it is the first record to be added to result than create it
				var tournamentResult = buildEmptyTournamentResult(player);
				// get gross and net strokes
				tournamentResult.setStrokesBrutto(getGrossStrokes(player, round));
				tournamentResult.setStrokesNetto(
						getNetStrokes(player, round, tournamentResult.getStrokesBrutto(), playerRound));
				tournamentResult.setTournament(round.getTournament());
				// update stb results
				List<Integer> stb = updateSTB(tournamentResult, round, playerRound, player);
				// save entity
				tournamentResultRepository.save(tournamentResult);

				addTournamentRound(stb.get(1), stb.get(0), tournamentResult.getStrokesBrutto(),
						tournamentResult.getStrokesNetto(), getScoreDifferential(playerRound, round, player),
						round.getCourse().getName(), tournamentResult);

			});

			// set tournament id in player_round
			playerRound.setTournamentId(round.getTournament().getId());
			playerRoundRepository.save(playerRound);

		});
	}

	public TournamentRound addTournamentRound(int stbGross, int stbNet, int strokesGross, int strokesNet, float scrDiff,
			String courseName, TournamentResult tournamentResult) {

		var tournamentRound = new TournamentRound();
		tournamentRound.setCourseName(courseName);
		tournamentRound.setScrDiff(scrDiff);
		tournamentRound.setStbGross(stbGross);
		tournamentRound.setStbNet(stbNet);
		tournamentRound.setStrokesBrutto(strokesGross);
		tournamentRound.setStrokesNetto(strokesNet);
		tournamentRound.setTournamentResult(tournamentResult);

		tournamentRound = tournamentRoundRepository.save(tournamentRound);

		return tournamentRound;
	}

	public List<TournamentRound> getTournamentRoundsForResult(Long resultId) {

		var tr = new TournamentResult();
		tr.setId(resultId);

		return tournamentRoundRepository.findByTournamentResultOrderByIdAsc(tr);

	}

	// calculate score differential
	public float getScoreDifferential(PlayerRound playerRound, Round round, Player player) {

		return (113 / (float) playerRound.getSr()) * (getCorrectedStrokes(player, round) - playerRound.getCr());

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
	public List<Integer> updateSTB(TournamentResult tournamentResult, Round round, PlayerRound playerRound,
			Player player) {

		// create List of ret values
		List<Integer> retStb = new ArrayList<>();

		// calculate course HCP
		int courseHCP = getCourseHCP(playerRound, round, player);

		// calculate hole HCP for player
		int hcpAll = (int) Math.floor((double) courseHCP / 18);
		int hcpIncMaxHole = courseHCP - (hcpAll * 18);
		log.debug("hcpAll " + hcpAll);
		log.debug("hcpIncMaxHole " + hcpIncMaxHole);

		// fill all holes with hcpAll value or initialize it with 0 if hcpAll is 0
		round.getScoreCard().forEach(scoreCard -> scoreCard.setHcp(hcpAll));
		
		List<Hole> holes = round.getCourse().getHoles();
		// get list of scorecard for player
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
			log.debug(scoreCard.getHole() + " " + scoreCard.getStbNet());
			// update STB gross for each hole
			scoreCard.setStbGross(holes.get(scoreCard.getHole() - 1).getPar() - scoreCard.getStroke() + 2);
			if (scoreCard.getStbGross() < 0) {
				scoreCard.setStbGross(0);
			}
		});

		retStb.add(playerScoreCard.stream().mapToInt(ScoreCard::getStbNet).sum());
		tournamentResult.setStbNet(tournamentResult.getStbNet() + retStb.get(0));

		retStb.add(playerScoreCard.stream().mapToInt(ScoreCard::getStbGross).sum());
		tournamentResult.setStbGross(tournamentResult.getStbGross() + retStb.get(1));

		return retStb;
	}

	@Transactional
	public List<Round> getAllPossibleRoundsForTournament(Long tournamentId) {

		var tournament = tournamentRepository.findById(tournamentId).orElseThrow();
		return roundService.findByDates(tournament.getStartDate(), tournament.getEndDate());

	}

	private int getCourseHCP(PlayerRound playerRound, Round round, Player player) {

		if (playerRound == null) {

			playerRound = roundService.getForPlayerRoundDetails(player.getId(), round.getId());
		}

		var courseTee = courseService.getTeeById(playerRound.getTeeId()).orElseThrow();

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

		var tournamentResult = new TournamentResult();
		tournamentResult.setPlayedRounds(1);
		tournamentResult.setPlayer(player);
		tournamentResult.setStbNet(0);
		tournamentResult.setStbGross(0);

		return tournamentResult;
	}

	// verifies if all scorecards have all 18 holes filled
	private void verifyRoundCorecteness(Round round) {

		round.getPlayer().forEach(player -> {

			// calculate played holes
			int playedHoles = (int) round.getScoreCard().stream()
					.filter(scoreCard -> scoreCard.getPlayer().equals(player) && scoreCard.getStroke() > 0).count();
			log.debug("Number of holes: " + playedHoles);
			if (playedHoles != TOURNAMENT_HOLES) {
				throw new TooFewHolesForTournamentException();
			}
		});
	}
}
