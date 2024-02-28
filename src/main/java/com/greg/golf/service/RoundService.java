package com.greg.golf.service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.greg.golf.repository.PlayerRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.configurationproperties.RoundServiceConfig;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.PlayerRound;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.error.PlayerAlreadyHasThatRoundException;
import com.greg.golf.error.ScoreCardUpdateException;
import com.greg.golf.error.TooManyPlayersException;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.repository.RoundRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Slf4j
@Service("roundService")
public class RoundService {
	
	private final RoundServiceConfig roundServiceConfig;
	private final RoundRepository roundRepository;
	private final PlayerRoundRepository playerRoundRepository;
	private final PlayerRepository playerRepository;
	
	public Optional<Round> getWithPlayers (Long id) {
		return roundRepository.findById(id);
	}

	@Transactional(readOnly = true)
	public List<Round> findByDates(Date startDate, Date endDate) {
		return roundRepository.findByRoundDateBetween(startDate, endDate);
	}

	@Transactional 
	public Round saveRound(Round round) {

		var player = round.getPlayer().stream().findFirst().orElseThrow();

		// set player association to ScoreCard
		round.getScoreCard().forEach(card -> card.setPlayer(player));

		log.debug("start searching matching round");
		// search for a round on the same course, the same date and tee time
		log.debug(round.getRoundDate().toString());
		Optional<Round> matchingRound = roundRepository.findRoundByCourseAndRoundDate(round.getCourse(),
				round.getRoundDate());

		// check if number of players is ok
		if (matchingRound.isPresent() && matchingRound.get().getPlayer().size() > 3) {
			log.warn("Number of players for round exceeded");
			throw new TooManyPlayersException();
		}

		// check if not attempt to save the round for the same player twice
		if (matchingRound.isPresent() && matchingRound.get().getPlayer().stream().anyMatch(p -> p.equals(player))) {

			log.debug("Attempt to save the same round twice");
			throw new PlayerAlreadyHasThatRoundException();
		}

		// start saving the round
		matchingRound.ifPresentOrElse(existingRound -> {
			log.debug("Trying to update matching round");
			existingRound.getPlayer().add(player);
			round.getScoreCard().forEach(card -> card.setRound(existingRound));
			existingRound.getScoreCard().addAll(round.getScoreCard());
			roundRepository.save(existingRound);
			round.setId(existingRound.getId());
			playerRoundRepository.updatePlayerRoundInfo(player.getWhs(), 
														round.getCourse().getTees().get(0).getSr(),
														round.getCourse().getTees().get(0).getCr(),
														round.getCourse().getTees().get(0).getId(),
														round.getCourse().getTees().get(0).getTeeType(),
														player.getId(), 
														round.getId());
		}, () -> {
			log.debug("trying to add not matching round");
			round.getScoreCard().forEach(card -> card.setRound(round));
			roundRepository.save(round);
			playerRoundRepository.updatePlayerRoundInfo(player.getWhs(),
													round.getCourse().getTees().get(0).getSr(),
													round.getCourse().getTees().get(0).getCr(),
								  					round.getCourse().getTees().get(0).getId(),
								  					round.getCourse().getTees().get(0).getTeeType(),
								  					player.getId(), 
								  					round.getId());
		});

		return round;
	}
	
	@Transactional(readOnly = true)
	public List<Round> listByPlayerPageable(Player player, Integer pageNo) {

		return roundRepository.findByPlayerOrderByRoundDateDesc(player, PageRequest.of(pageNo, roundServiceConfig.getPageSize()));
	}
	
	@Transactional(readOnly = true)
	public List<Round> getRecentRounds(Integer pageNo) {

		// v2.3 modified to use 2 queries to get rid of HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!  
		// first get list of ids
		List<Long> ids = roundRepository.getIdsForPage(PageRequest.of(pageNo, roundServiceConfig.getPageSize()));
		// then return results only for selected page
		return roundRepository.getForIds(ids);

	}
	
	@Transactional
	public void deleteScorecard(Long playerId, Long roundId) {

		// get the round first
		var round = roundRepository.findById(roundId).orElseThrow();
		log.debug("Round found");

		// check if player scorecard exists in that round
		if (round.getPlayer().stream().noneMatch(p -> p.getId().equals(playerId))) {
			log.warn("Player " + playerId + " not found in round " + roundId);
			throw new NoSuchElementException();
		}

		log.debug("Player exists for a round");

		// check if only one player is in the round and if yes delete entire round
		int playerCnt = round.getPlayer().size();
		log.debug("Player cnt  is " + playerCnt);
		if (playerCnt == 1) {
			roundRepository.deleteById(roundId);
			log.debug("Round deleted");
			return;
		}

		// select player to remove
		Set<Player> playerToRemove = round.getPlayer().stream().filter(p -> p.getId().equals(playerId))
				.collect(Collectors.toSet());
		round.getPlayer().removeAll(playerToRemove);
		log.debug("Player deleted");
		// select scorecard to remove
		Set<ScoreCard> cardsToRemove = round.getScoreCard().stream()
				.filter(sc -> sc.getPlayer().getId().equals(playerId)).collect(Collectors.toSet());
		round.getScoreCard().removeAll(cardsToRemove);
		log.debug("ScoreCard deleted");

		// persists round
		roundRepository.save(round);
	}

	@Transactional
	public void updateScoreCard(Round updRound) {
		
		
		// get the round first
		var round = roundRepository.findById(updRound.getId()).orElseThrow();
		
		// verify if round contains one and only one player
		// also update the round which was assigned for tournament is not allowed
		if (updRound.getPlayer() == null || updRound.getPlayer().size() != 1) {
			throw new ScoreCardUpdateException();
		}
		
		// get first player from set
		var player = updRound.getPlayer().iterator().next();
		// remove scorecard object that matching player from round
		round.getScoreCard().removeAll((round.getScoreCard()
				.stream()
				.filter(sc -> sc.getPlayer().getId().equals(player.getId()))
				.toList()));

		updRound.getScoreCard().forEach(sc -> {
			sc.setRound(updRound);
			sc.setPlayer(player);
		});
		round.getScoreCard().addAll(updRound.getScoreCard());

		roundRepository.save(round);
		log.debug("Score card updated");
	}

	@Transactional(readOnly = true)
	public PlayerRound getForPlayerRoundDetails(Long playerId, Long roundId) {

		return playerRoundRepository.getForPlayerAndRound(playerId, roundId).orElseThrow();

	}
	
	@Transactional
	public List<PlayerRound> getByRoundId(Long roundId) {
		return playerRoundRepository.findByRoundIdOrderByPlayerId(roundId).orElseThrow();
	}

	@Transactional
	public void updateRoundWhs(Long playerId, Long roundId, Float whs) {
		playerRoundRepository.updatePlayerRoundWhs(whs, playerId, roundId);
	}

	@Transactional
	public void swapPlayer(Long oldPlayerId, Long newPlayerId, Long roundId) {

		// get data
		Round round = roundRepository.findById(roundId).orElseThrow();
		Player newPlayer = playerRepository.findById(newPlayerId).orElseThrow();

		round.getScoreCard().forEach(sc -> {

			if (sc.getPlayer().getId().equals(oldPlayerId)) {
				sc.setPlayer(newPlayer);
			}
		});
		//update round (score cards)
		roundRepository.save(round);

		//update player round (player id)
		PlayerRound playerRound = playerRoundRepository.getForPlayerAndRound(oldPlayerId, roundId).orElseThrow();
		playerRound.setPlayerId(newPlayerId);
		playerRoundRepository.save(playerRound);

	}

}
