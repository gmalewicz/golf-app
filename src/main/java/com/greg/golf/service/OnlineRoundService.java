package com.greg.golf.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.CourseTee;
import com.greg.golf.entity.OnlineRound;
import com.greg.golf.entity.OnlineScoreCard;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.repository.OnlineRoundRepository;
import com.greg.golf.repository.OnlineScoreCardRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("onlineRoundService")
public class OnlineRoundService {

	@Autowired
	private OnlineRoundRepository onlineRoundRepository;

	@Autowired
	private OnlineScoreCardRepository onlineScoreCardRepository;

	@Autowired
	private RoundService roundService;

	@Scheduled(cron = "0 0 0 * * * ")
	@Transactional
	public void purge() {
		log.info("Purge job for online rounds executed");
		onlineRoundRepository.deleteAll();
	}

	@Transactional
	public OnlineRound save(OnlineRound onlineRound) {

		GregorianCalendar gc = new GregorianCalendar();
		onlineRound.setDate(gc.getTime());

		return onlineRoundRepository.save(onlineRound);

	}

	@Transactional
	public List<OnlineRound> save(List<OnlineRound> onlineRounds) {

		GregorianCalendar gc = new GregorianCalendar();
		onlineRounds.forEach(or -> or.setDate(gc.getTime()));

		return onlineRoundRepository.saveAll(onlineRounds);

	}

	@Transactional
	public OnlineScoreCard saveOnlineScoreCard(OnlineScoreCard onlineScoreCard) {

		if (onlineScoreCard.isUpdate()) {
			log.debug("Update of the score card executed: " + onlineScoreCard);
			OnlineScoreCard updatedScoreCard = onlineScoreCardRepository
					.findByOnlineRoundAndHole(onlineScoreCard.getOnlineRound(), onlineScoreCard.getHole())
					.orElseThrow();
			updatedScoreCard.setStroke(onlineScoreCard.getStroke());
			updatedScoreCard.setPutt(onlineScoreCard.getPutt());
			updatedScoreCard.setPenalty(onlineScoreCard.getPenalty());
			return onlineScoreCardRepository.save(updatedScoreCard);
		}

		log.debug("Adding of the score card executed: " + onlineScoreCard);
		return onlineScoreCardRepository.save(onlineScoreCard);

	}

	@Transactional(readOnly = true)
	public List<OnlineRound> getOnlineRounds() {

		return onlineRoundRepository.findAll();

	}

	@Transactional(readOnly = true)
	public List<OnlineScoreCard> getOnlineScoreCards(Long onlineRoundId) {

		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setId(onlineRoundId);

		return onlineScoreCardRepository.getByOnlineRound(onlineRound);

	}

	@Transactional
	public void delete(Long id) {

		onlineRoundRepository.deleteById(id);

	}

	@Transactional
	public void deleteForOwner(Long ownerId) {

		onlineRoundRepository.deleteByOwnerAndFinalized(ownerId, false);

	}

	@Transactional
	public Round finalizeById(Long id) {

		Round retRound = null;

		// get the online round from db
		OnlineRound onlineRound = onlineRoundRepository.findById(id).orElseThrow();

		retRound = buildRound(onlineRound);

		// at the end delete the online entries
		onlineRoundRepository.deleteById(id);

		return retRound;

	}

	private Round buildRound(OnlineRound onlineRound) {

		// create Round object and fill it in
		Round round = new Round();

		Course course = new Course();
		List<CourseTee> tees = new ArrayList<>();
		tees.add(onlineRound.getCourseTee());
		course.setTees(tees);
		course.setHoles(onlineRound.getCourse().getHoles());
		course.setId(onlineRound.getCourse().getId());
		round.setCourse(course);

		Set<Player> players = new HashSet<>();
		players.add(onlineRound.getPlayer());
		round.setPlayer(players);
		round.setMatchPlay(onlineRound.getMatchPlay());
		round.setMpFormat(onlineRound.getMpFormat());
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(onlineRound.getDate());

		gc.set(Calendar.HOUR_OF_DAY, Integer.parseInt(onlineRound.getTeeTime().substring(0, 2)) + 2);
		gc.set(Calendar.MINUTE, Integer.parseInt(onlineRound.getTeeTime().substring(3, 5)));
		gc.set(Calendar.MILLISECOND, gc.get(Calendar.MILLISECOND));	

		round.setRoundDate(gc.getTime());

		// create map of score cards that has been passed

		Map<Integer, ScoreCard> holes = new HashMap<>();

		for (OnlineScoreCard os : onlineRound.getScoreCard()) {
			ScoreCard sc = new ScoreCard();
			sc.setHole(os.getHole());
			sc.setPats(os.getPutt());
			sc.setPenalty(os.getPenalty());
			sc.setStroke(os.getStroke());
			sc.setPlayer(onlineRound.getPlayer());
			holes.put(os.getHole(), sc);
		}

		// create 18 score cards and fill not used using empty score cards

		List<ScoreCard> scoreCards = new ArrayList<>();
		for (int i = 0; i < 18; i++) {

			if (holes.containsKey(i + 1)) {
				scoreCards.add(holes.get(i + 1));
			} else {
				ScoreCard sc = new ScoreCard();
				sc.setHole(i + 1);
				sc.setPats(0);
				sc.setPenalty(0);
				sc.setStroke(0);
				sc.setPlayer(onlineRound.getPlayer());
				scoreCards.add(sc);
			}

		}

		round.setScoreCard(scoreCards);

		return roundService.saveRound(round);
	}

	@Transactional
	public void finalizeForOwner(Long ownerId) {

		// get the online rounds from db
		List<OnlineRound> onlineRounds = onlineRoundRepository.findByOwnerAndFinalized(ownerId, false);

		// for now it is assumed that children are retrieved

		onlineRounds.forEach(onlineRound -> {

			buildRound(onlineRound);

			onlineRound.setFinalized(true);
			onlineRoundRepository.save(onlineRound);

		});
	}

	@Transactional(readOnly = true)
	public List<OnlineRound> getOnlineRoundsForCourse(Long courseId) {

		Course course = new Course();
		course.setId(courseId);
		List<OnlineRound> onlineRounds = onlineRoundRepository.findByCourse(course);

		onlineRounds.stream().forEach(or -> or.setScoreCardAPI(or.getScoreCard()));

		return onlineRounds;
	}

	@Transactional(readOnly = true)
	public List<OnlineRound> getOnlineRoundsForOwner(Long ownerId) {

		List<OnlineRound> onlineRounds = onlineRoundRepository.findByOwner(ownerId);

		onlineRounds.stream().forEach(or -> or.setScoreCardAPI(or.getScoreCard()));

		return onlineRounds;
	}
}
