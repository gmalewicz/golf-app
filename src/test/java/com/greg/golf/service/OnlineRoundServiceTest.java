package com.greg.golf.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.CourseTee;
import com.greg.golf.entity.OnlineRound;
import com.greg.golf.entity.OnlineScoreCard;
import com.greg.golf.entity.Player;
import com.greg.golf.repository.OnlineRoundRepository;
import com.greg.golf.repository.OnlineScoreCardRepository;
import com.greg.golf.repository.RoundRepository;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class OnlineRoundServiceTest {

	// private static Course course;

	@Autowired
	private OnlineRoundService onlineRoundService;
	
	@Autowired 
	private OnlineRoundRepository onlineRoundRepository;
	
	@Autowired 
	private OnlineScoreCardRepository onlineScoreCardRepository;
	
	private static Course course;
	private static CourseTee courseTee;
	private static Player player;
	
	@BeforeAll
	public static void setup() {
		
		course = new Course();
		course.setId(1L);
		courseTee = new CourseTee();
		courseTee.setId(1L);
		player = new Player();
		player.setId(1L);
		log.info("Set up completed");
	}
	
	@DisplayName("Purge empty list of online score cards")
	@Transactional
	@Test
	void purgeEmptyTest() {
		
		onlineRoundService.purge();
		assertTrue(onlineRoundRepository.findAll().isEmpty());
		
	}
	
	@DisplayName("Purge non empty list of online score cards")
	@Transactional
	@Test
	void purgeNonEmptyTest() {
		
		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setCourse(course);
		onlineRound.setCourseTee(courseTee);
		onlineRound.setPlayer(player);
		onlineRound.setDate(new Date());
		onlineRound.setTeeTime("10:00");
		onlineRound.setOwner(player.getId());
		onlineRound.setFinalized(false);
		onlineRound.setMatchPlay(false);
		onlineRoundRepository.save(onlineRound);
		
		assertEquals(1, onlineRoundRepository.findAll().size());
		onlineRoundService.purge();
		assertTrue(onlineRoundRepository.findAll().isEmpty());
	}
	
	@DisplayName("Save online round")
	@Transactional
	@Test
	void saveOnlineRoundTest() {
		
		OnlineRound onlineRound = new OnlineRound();;
		onlineRound.setCourse(course);
		onlineRound.setCourseTee(courseTee);
		onlineRound.setPlayer(player);
		onlineRound.setTeeTime("10:00");
		onlineRound.setOwner(player.getId());
		onlineRound.setFinalized(false);
		onlineRound.setMatchPlay(false);
		onlineRoundService.save(onlineRound);
		
		assertEquals(1, onlineRoundRepository.findAll().size());
	}
	
	@DisplayName("Save online score card")
	@Transactional
	@Test
	void saveOnlineScoreCardRepositoryTest() {
		
		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setCourse(course);
		onlineRound.setCourseTee(courseTee);
		onlineRound.setPlayer(player);
		onlineRound.setDate(new Date());
		onlineRound.setTeeTime("10:00");
		onlineRound.setOwner(player.getId());
		onlineRound.setFinalized(false);
		onlineRound.setMatchPlay(false);
		onlineRoundRepository.save(onlineRound);
		
		OnlineScoreCard onlineScoreCard = new OnlineScoreCard();
		onlineScoreCard.setPlayer(player);
		onlineScoreCard.setHole(1);
		onlineScoreCard.setOnlineRound(onlineRound);
		onlineScoreCard.setStroke(1);
		onlineScoreCard.setUpdate(false);
		
		onlineRoundService.saveOnlineScoreCard(onlineScoreCard);
		
		assertEquals(1, onlineScoreCardRepository.findAll().size());
	}
	
	@DisplayName("Update online score card")
	@Transactional
	@Test
	void updateOnlineScoreCardRepositoryTest() {
		
		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setCourse(course);
		onlineRound.setCourseTee(courseTee);
		onlineRound.setPlayer(player);
		onlineRound.setDate(new Date());
		onlineRound.setTeeTime("10:00");
		onlineRound.setOwner(player.getId());
		onlineRound.setFinalized(false);
		onlineRound.setMatchPlay(false);
		onlineRoundRepository.save(onlineRound);
		
		OnlineScoreCard onlineScoreCard = new OnlineScoreCard();
		onlineScoreCard.setPlayer(player);
		onlineScoreCard.setHole(1);
		onlineScoreCard.setOnlineRound(onlineRound);
		onlineScoreCard.setStroke(1);
		onlineScoreCard.setUpdate(false);
		onlineScoreCardRepository.save(onlineScoreCard);
		
		assertEquals(1, onlineScoreCardRepository.findAll().size());
		
		onlineScoreCard.setUpdate(true);
		onlineScoreCard.setStroke(2);
		onlineRoundService.saveOnlineScoreCard(onlineScoreCard);
		
		assertEquals(2, onlineScoreCardRepository.findAll().get(0).getStroke().intValue());
	}
	
	@DisplayName("Get online rounds")
	@Transactional
	@Test
	void getOnlineRoundsTest() {
		
		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setCourse(course);
		onlineRound.setCourseTee(courseTee);
		onlineRound.setPlayer(player);
		onlineRound.setDate(new Date());
		onlineRound.setTeeTime("10:00");
		onlineRound.setOwner(player.getId());
		onlineRound.setFinalized(false);
		onlineRound.setMatchPlay(false);
		onlineRoundRepository.save(onlineRound);
		
		assertEquals(1, onlineRoundService.getOnlineRounds().size());
	}
	
	@DisplayName("Get online score cards")
	@Transactional
	@Test
	void getOnlineScoreCardsTest() {
		
		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setCourse(course);
		onlineRound.setCourseTee(courseTee);
		onlineRound.setPlayer(player);
		onlineRound.setDate(new Date());
		onlineRound.setTeeTime("10:00");
		onlineRound.setOwner(player.getId());
		onlineRound.setFinalized(false);
		onlineRound.setMatchPlay(false);
		onlineRoundRepository.save(onlineRound);
		
		OnlineScoreCard onlineScoreCard = new OnlineScoreCard();
		onlineScoreCard.setPlayer(player);
		onlineScoreCard.setHole(1);
		onlineScoreCard.setOnlineRound(onlineRound);
		onlineScoreCard.setStroke(1);
		onlineScoreCard.setUpdate(false);
		onlineScoreCardRepository.save(onlineScoreCard);
		
		assertEquals(1, onlineRoundService.getOnlineScoreCards(onlineRound.getId()).size());
	}
	
	@DisplayName("Delete online round")
	@Transactional
	@Test
	void deleteOnlineRoundTest() {
		
		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setCourse(course);
		onlineRound.setCourseTee(courseTee);
		onlineRound.setPlayer(player);
		onlineRound.setDate(new Date());
		onlineRound.setTeeTime("10:00");
		onlineRound.setOwner(player.getId());
		onlineRound.setFinalized(false);
		onlineRound.setMatchPlay(false);
		onlineRoundRepository.save(onlineRound);
		
		OnlineScoreCard onlineScoreCard = new OnlineScoreCard();
		onlineScoreCard.setPlayer(player);
		onlineScoreCard.setHole(1);
		onlineScoreCard.setOnlineRound(onlineRound);
		onlineScoreCard.setStroke(1);
		onlineScoreCard.setUpdate(false);
		onlineScoreCardRepository.save(onlineScoreCard);
		
		onlineRoundService.delete(onlineRound.getId());
		
		assertEquals(0, onlineRoundRepository.findAll().size());
	}
	
	@DisplayName("Finalize online round")
	@Transactional
	@Test
	void finalizeRoundTest(@Autowired RoundRepository roundRepository) {
		
		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setCourse(course);
		onlineRound.setCourseTee(courseTee);
		onlineRound.setPlayer(player);
		onlineRound.setDate(new Date());
		onlineRound.setTeeTime("10:00");
		onlineRound.setOwner(player.getId());
		onlineRound.setFinalized(false);
		onlineRound.setMatchPlay(false);
		onlineRoundRepository.save(onlineRound);
		
		OnlineScoreCard onlineScoreCard = new OnlineScoreCard();
		onlineScoreCard.setPlayer(player);
		onlineScoreCard.setHole(1);
		onlineScoreCard.setOnlineRound(onlineRound);
		onlineScoreCard.setStroke(1);
		onlineScoreCard.setUpdate(false);
		onlineScoreCardRepository.save(onlineScoreCard);
		
		onlineRoundService.finalize(onlineRound.getId());
		
		assertEquals(1, roundRepository.findAll().size());
	}
	
	@DisplayName("Finalize online round for owner")
	@Transactional
	@Test
	void finalizeOwnerRoundTest(@Autowired RoundRepository roundRepository) {
		
		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setCourse(course);
		onlineRound.setCourseTee(courseTee);
		onlineRound.setPlayer(player);
		onlineRound.setDate(new Date());
		onlineRound.setTeeTime("10:00");
		onlineRound.setOwner(player.getId());
		onlineRound.setFinalized(false);
		onlineRound.setMatchPlay(false);
		onlineRoundRepository.save(onlineRound);
		
		OnlineScoreCard onlineScoreCard = new OnlineScoreCard();
		onlineScoreCard.setPlayer(player);
		onlineScoreCard.setHole(1);
		onlineScoreCard.setOnlineRound(onlineRound);
		onlineScoreCard.setStroke(1);
		onlineScoreCard.setUpdate(false);
		onlineScoreCardRepository.save(onlineScoreCard);
				
		onlineRoundService.finalizeForOwner(1L);
		
		assertEquals(1, roundRepository.findAll().size());
	}
	
	@DisplayName("Save online rounds")
	@Transactional
	@Test
	void saveOnlineRoundsTest() {
		
		OnlineRound onlineRound = new OnlineRound();;
		onlineRound.setCourse(course);
		onlineRound.setCourseTee(courseTee);
		onlineRound.setPlayer(player);
		onlineRound.setTeeTime("10:00");
		onlineRound.setOwner(player.getId());
		onlineRound.setFinalized(false);
		onlineRound.setMatchPlay(false);
		List<OnlineRound> rounds = new ArrayList<OnlineRound>();
		rounds.add(onlineRound);
		
		onlineRoundService.save(onlineRound);
		
		assertEquals(1, onlineRoundRepository.findAll().size());
	}
	
	@AfterAll
	public static void done() {
		
		//onlineRoundRepository.deleteAll();
		log.info("Clean up completed");

	}
	
	

}
