package com.greg.golf.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.CourseTee;
import com.greg.golf.entity.OnlineRound;
import com.greg.golf.entity.OnlineScoreCard;
import com.greg.golf.entity.Player;
import com.greg.golf.repository.OnlineRoundRepository;
import com.greg.golf.repository.OnlineScoreCardRepository;
import com.greg.golf.repository.RoundRepository;
import com.greg.golf.util.GolfPostgresqlContainer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class OnlineRoundServiceTest {

	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

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
		Assertions.assertTrue(onlineRoundRepository.findAll().isEmpty());
		
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
		
		Assertions.assertEquals(1, onlineRoundRepository.findAll().size());
		onlineRoundService.purge();
		Assertions.assertTrue(onlineRoundRepository.findAll().isEmpty());
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
		onlineScoreCard.setTime("10:00");
		
		onlineRoundService.saveOnlineScoreCard(onlineScoreCard);
		
		Assertions.assertEquals(1, onlineScoreCardRepository.findAll().size());
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
		onlineScoreCard.setTime("10:00");
		onlineScoreCardRepository.save(onlineScoreCard);
		
		Assertions.assertEquals(1, onlineScoreCardRepository.findAll().size());
		
		onlineScoreCard.setUpdate(true);
		onlineScoreCard.setStroke(2);
		onlineRoundService.saveOnlineScoreCard(onlineScoreCard);
		
		Assertions.assertEquals(2, onlineScoreCardRepository.findAll().get(0).getStroke().intValue());
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
		
		Assertions.assertEquals(1, onlineRoundService.getOnlineRounds().size());
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
		onlineScoreCard.setTime("10:00");
		onlineScoreCardRepository.save(onlineScoreCard);
		
		Assertions.assertEquals(1, onlineRoundService.getOnlineScoreCards(onlineRound.getId()).size());
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
		
		OnlineScoreCard onlineScoreCard = new OnlineScoreCard();
		onlineScoreCard.setPlayer(player);
		onlineScoreCard.setHole(1);
		onlineScoreCard.setOnlineRound(onlineRound);
		onlineScoreCard.setStroke(1);
		onlineScoreCard.setPutt(0);
		onlineScoreCard.setPenalty(0);
		onlineScoreCard.setUpdate(false);
		onlineScoreCard.setTime("10:00");
		
		onlineRound.setScoreCard(new ArrayList<>());
		onlineRound.getScoreCard().add(onlineScoreCard);
		
		
		onlineRoundRepository.save(onlineRound);
		// onlineScoreCardRepository.save(onlineScoreCard);
				
		onlineRoundService.finalizeForOwner(1L);
		
		Assertions.assertEquals(1, roundRepository.findAll().size());
	}
	
	@DisplayName("Save online rounds")
	@Transactional
	@Test
	void saveOnlineRoundsTest() {
		
		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setCourse(course);
		onlineRound.setCourseTee(courseTee);
		onlineRound.setPlayer(player);
		onlineRound.setTeeTime("10:00");
		onlineRound.setOwner(player.getId());
		onlineRound.setFinalized(false);
		onlineRound.setMatchPlay(false);
		List<OnlineRound> rounds = new ArrayList<>();
		rounds.add(onlineRound);
		
		onlineRoundService.save(rounds);
		
		Assertions.assertEquals(1, onlineRoundRepository.findAll().size());
	}
	
	@AfterAll
	public static void done() {
		
		//onlineRoundRepository.deleteAll();
		log.info("Clean up completed");

	}
	
	

}
