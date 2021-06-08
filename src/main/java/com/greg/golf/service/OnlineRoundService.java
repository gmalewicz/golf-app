package com.greg.golf.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
@Service("onlineRoundService")
public class OnlineRoundService {

	private final OnlineRoundRepository onlineRoundRepository;
	private final OnlineScoreCardRepository onlineScoreCardRepository;
	private final RoundService roundService;

	@Scheduled(cron = "0 0 0 * * * ")
	@Transactional
	public void purge() {
		log.info("Purge job for online rounds executed");
		onlineRoundRepository.deleteAll();
	}

	@Transactional
	public List<OnlineRound> save(List<OnlineRound> onlineRounds) {

		var gc = new GregorianCalendar();
		onlineRounds.forEach(or -> or.setDate(gc.getTime()));

		return onlineRoundRepository.saveAll(onlineRounds);

	}

	@Transactional
	public OnlineScoreCard saveOnlineScoreCard(OnlineScoreCard onlineScoreCard) {
		
		var onlineRound = new OnlineRound();
		onlineRound.setId(onlineScoreCard.getOrId());
		onlineScoreCard.setOnlineRound(onlineRound);

		if (onlineScoreCard.isUpdate()) {
			log.debug("Update of the score card executed: " + onlineScoreCard);
			var updatedScoreCard = onlineScoreCardRepository
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

		var onlineRound = new OnlineRound();
		onlineRound.setId(onlineRoundId);

		return onlineScoreCardRepository.getByOnlineRound(onlineRound);

	}

	@Transactional
	public void deleteForOwner(Long ownerId) {

		onlineRoundRepository.deleteByOwnerAndFinalized(ownerId, false);

	}

	private Round buildRound(OnlineRound onlineRound) {

		// create Round object and fill it in
		var round = new Round();

		var course = new Course();
		var tees = new ArrayList<CourseTee>();
		tees.add(onlineRound.getCourseTee());
		course.setTees(tees);
		course.setHoles(onlineRound.getCourse().getHoles());
		course.setId(onlineRound.getCourse().getId());
		round.setCourse(course);

		var players = new TreeSet<Player>();
		players.add(onlineRound.getPlayer());
		round.setPlayer(players);
		round.setMatchPlay(onlineRound.getMatchPlay());
		round.setMpFormat(onlineRound.getMpFormat());
		var gc = new GregorianCalendar();
		gc.setTime(onlineRound.getDate());

		gc.set(Calendar.HOUR_OF_DAY, Integer.parseInt(onlineRound.getTeeTime().substring(0, 2)) + 2);
		gc.set(Calendar.MINUTE, Integer.parseInt(onlineRound.getTeeTime().substring(3, 5)));
		gc.set(Calendar.MILLISECOND, gc.get(Calendar.MILLISECOND));

		round.setRoundDate(gc.getTime());

		// create map of score cards that has been passed

		var holes = new HashMap<Integer, ScoreCard>();

		for (OnlineScoreCard os : onlineRound.getScoreCard()) {
			var sc = new ScoreCard();
			sc.setHole(os.getHole());
			sc.setPats(os.getPutt());
			sc.setPenalty(os.getPenalty());
			sc.setStroke(os.getStroke());
			sc.setPlayer(onlineRound.getPlayer());
			holes.put(os.getHole(), sc);
		}

		// create 18 score cards and fill not used using empty score cards

		var scoreCards = new ArrayList<ScoreCard>();
		for (var i = 0; i < 18; i++) {

			if (holes.containsKey(i + 1)) {
				scoreCards.add(holes.get(i + 1));
			} else {
				var sc = new ScoreCard();
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
		var onlineRounds = onlineRoundRepository.findByOwnerAndFinalized(ownerId, false);

		// for now it is assumed that children are retrieved

		onlineRounds.forEach(onlineRound -> {

			buildRound(onlineRound);

			onlineRound.setFinalized(true);
			onlineRoundRepository.save(onlineRound);

		});
	}

	@Transactional(readOnly = true)
	public List<OnlineRound> getOnlineRoundsForCourse(Long courseId) {

		var course = new Course();
		course.setId(courseId);
		var onlineRounds = onlineRoundRepository.findByCourse(course);

		onlineRounds.stream().forEach(or -> or.setScoreCardAPI(or.getScoreCard()));

		return onlineRounds;
	}

	@Transactional(readOnly = true)
	public List<OnlineRound> getOnlineRoundsForOwner(Long ownerId) {

		var onlineRounds = onlineRoundRepository.findByOwner(ownerId);

		onlineRounds.stream().forEach(or -> or.setScoreCardAPI(or.getScoreCard()));

		return onlineRounds;
	}
}
