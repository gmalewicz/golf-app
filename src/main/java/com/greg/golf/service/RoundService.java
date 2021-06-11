package com.greg.golf.service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
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
import com.greg.golf.service.events.RoundEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
@Service("roundService")
public class RoundService {
	
	private final RoundServiceConfig roundServiceConfig;
	private final RoundRepository roundRepository;
	private final PlayerRoundRepository playerRoundRepository;
	private final ApplicationEventPublisher applicationEventPublisher;
	
	public Optional<Round> getWithPlayers (Long id) {
		return roundRepository.findById(id);
	}

	@Transactional(readOnly = true)
	public List<Round> findByDates(Date startDate, Date endDate) {
		return roundRepository.findByTournamentIsNullAndRoundDateBetween(startDate, endDate);
	}

	@Transactional 
	public Round saveRound(Round round) {

		var player = round.getPlayer().stream().findFirst().orElseThrow();

		// set player association to ScoreCard
		round.getScoreCard().stream().forEach(card -> card.setPlayer(player));

		log.debug("start searching matchin round");
		// search for a round on the same course, the same date and tee time
		log.debug(round.getRoundDate());
		Optional<Round> matchingRound = roundRepository.findRoundByCourseAndRoundDate(round.getCourse(),
				round.getRoundDate());

		// check if number of players is ok
		if (matchingRound.isPresent() && matchingRound.get().getPlayer().size() > 3) {
			log.warn("Number of players for round exeeded");
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
			round.getScoreCard().stream().forEach(card -> card.setRound(existingRound));
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
			
			// verify if tournament shall be updated (only if the round is already assigned to tournament)
			if (existingRound.getTournament() != null) {
				log.info("Tournament round sent for checking if tournament result update shall be done");
				var roundEvent = new RoundEvent(this, existingRound);
				applicationEventPublisher.publishEvent(roundEvent);
			}

		}, () -> {
			log.debug("trying to add not matching round");
			round.getScoreCard().stream().forEach(card -> card.setRound(round));
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

		return roundRepository.findByOrderByRoundDateDescPlayerAsc(PageRequest.of(pageNo, roundServiceConfig.getPageSize()));
	}
	
	@Transactional
	public void deleteScorecard(Long playerId, Long roundId) {

		// get the round first
		var round = roundRepository.findById(roundId).orElseThrow();
		log.debug("Round found");

		// check if player score card exists in that round
		if (round.getPlayer().stream().filter(p -> p.getId().equals(playerId)).count() == 0) {
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
		// select score card to remove
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
		if (updRound.getPlayer() == null || updRound.getPlayer().size() != 1 || 
				round.getTournament() != null) {
			throw new ScoreCardUpdateException();
		}
		
		// get first player from set
		var player = updRound.getPlayer().iterator().next();
		// remove score card object that matching player from round
		round.getScoreCard().removeAll((round.getScoreCard().stream()
				.filter(sc -> sc.getPlayer().getId().equals(player.getId())).collect(Collectors.toList())));

		updRound.getScoreCard().stream().forEach(sc -> {
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
}
