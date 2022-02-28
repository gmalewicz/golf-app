package com.greg.golf.repository;

import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.util.ArrayList;
import java.util.Date;

import com.greg.golf.security.JwtRequestFilter;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.Tournament;
import com.greg.golf.service.PlayerService;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TournamentRepositoryTest {

	@SuppressWarnings("unused")
	@MockBean
	private JwtRequestFilter jwtRequestFilter;

	private static Player player;

	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();
	
	@Autowired
	private TournamentRepository tournamentRepository;

	@BeforeAll
	public static void setup(@Autowired PlayerService ps) {
		
		player = ps.getPlayer(1L).orElseThrow();
	}
	
	@DisplayName("Add tournament")
	@Transactional
	@Test
	void addTournamentTest() {

		Tournament tournament = new Tournament();
		tournament.setName("Test tournament");
		tournament.setStartDate(new Date(1));
		tournament.setEndDate(new Date(1));
		var roundList = new ArrayList<Round>();
		// roundList.add(round);
		tournament.setRound(roundList);
		tournament.setPlayer(player);
		tournament.setBestRounds(0);
		tournament = tournamentRepository.save(tournament);
		assertNotNull("Id should not be null", tournament.getId());
	}

	@DisplayName("Get tournament")
	@Transactional
	@Test
	void getTournamentById() {

		Tournament tournament = new Tournament();
		tournament.setName("Test tournament");
		tournament.setStartDate(new Date(1));
		tournament.setEndDate(new Date(1));
		var roundList = new ArrayList<Round>();
		// roundList.add(round);
		tournament.setRound(roundList);
		tournament.setPlayer(player);
		tournament.setBestRounds(0);
		tournament = tournamentRepository.save(tournament);
		assertNotNull("Tournament should not be null", tournamentRepository.findById(tournament.getId()));
	}

	@DisplayName("Delete tournament")
	@Transactional
	@Test
	void deleteTournamentById() {

		Tournament tournament = new Tournament();
		tournament.setName("Test tournament"); 
		tournament.setStartDate(new Date(1));
		tournament.setEndDate(new Date(1));
		var roundList = new ArrayList<Round>();
		tournament.setRound(roundList);
		tournament.setPlayer(player);
		tournament.setBestRounds(0);
		tournament = tournamentRepository.save(tournament);
		tournamentRepository.deleteById(tournament.getId());
		assertTrue("Tournament should be null", tournamentRepository.findById(tournament.getId()).isEmpty());
	}
}
