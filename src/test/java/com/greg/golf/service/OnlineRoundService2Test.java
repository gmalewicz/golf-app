package com.greg.golf.service;

import java.util.Date;
import java.util.List;

import com.greg.golf.security.JwtRequestFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;

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
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class OnlineRoundService2Test {

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtRequestFilter jwtRequestFilter;

	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

	@Autowired
	private OnlineRoundService onlineRoundService;

    @BeforeAll
	public static void setup(@Autowired OnlineRoundRepository onlineRoundRepository, @Autowired  OnlineScoreCardRepository onlineScoreCardRepository) {

        Course course = new Course();
		course.setId(1L);
        CourseTee courseTee = new CourseTee();
		courseTee.setId(1L);
        Player player = new Player();
		player.setId(1L);
		
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
		
		log.info("Set up completed");
	}
	
	@DisplayName("Get online rounds for course")
	@Transactional
	@Test
	void getOnlineRoundsForCourseTest() {
		
		
		
		List<OnlineRound> rounds = onlineRoundService.getOnlineRoundsForCourse(1L);
		
		Assertions.assertEquals(1, rounds.size());
		
		Assertions.assertEquals(1, rounds.get(0).getScoreCardAPI().size());
	}
	
	@DisplayName("Get online rounds for owner")
	@Transactional
	@Test
	void getOnlineRoundsForOwnerTest() {
		
		
		
		List<OnlineRound> rounds = onlineRoundService.getOnlineRoundsForOwner(1L);
		
		Assertions.assertEquals(1, rounds.size());
		
		Assertions.assertEquals(1, rounds.get(0).getScoreCardAPI().size());
	}
	
	@DisplayName("Delete online rounds for owner")
	@Transactional
	@Test
	void deleteOnlineRoundsForOwnerTest() {
		
		
		onlineRoundService.deleteForOwner(1L);
		
		List<OnlineRound> rounds = onlineRoundService.getOnlineRounds();
		
		Assertions.assertEquals(0, rounds.size());
		
	}
	
	
	@AfterAll
	public static void done(@Autowired OnlineRoundRepository onlineRoundRepository) {
		
		
		onlineRoundRepository.deleteAll();
		log.info("Clean up completed");

	}
	
	

}
