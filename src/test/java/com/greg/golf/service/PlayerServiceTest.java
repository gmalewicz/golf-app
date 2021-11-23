package com.greg.golf.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.util.GolfPostgresqlContainer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class PlayerServiceTest {
	
	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

	private static Player player;
	private static Player admin;

	@Autowired
	private PlayerService playerService;

	@BeforeAll
	public static void setup(@Autowired PlayerRepository playerRepository) {

		player = playerRepository.findById(1L).orElseThrow();

		Player adminPlayer = new Player();
		adminPlayer.setNick("admin");
		adminPlayer.setPassword(player.getPassword());
		adminPlayer.setRole(Common.ROLE_PLAYER_ADMIN);
		adminPlayer.setWhs(10f);
		adminPlayer.setSex(Common.PLAYER_SEX_MALE);
		admin = playerRepository.save(adminPlayer);

		log.info("Set up completed");
	}

	@DisplayName("Change password with success")
	@Transactional
	@Test
	void changePasswordSuccessTest() {

		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.ADMIN));
		
		SecurityContextHolder.getContext().setAuthentication(
			        new UsernamePasswordAuthenticationToken("authorized", "fake", authorities));
		
		player.setPassword("test");
		player = playerService.resetPassword(player);
		Assertions.assertEquals("test", player.getPassword());

	}

	@DisplayName("Unauthorized attempt to change password")
	@Transactional
	@Test
	void changePasswordUnauthorizedTest(@Autowired PlayerRepository playerRepository) {

		player.setPassword("test");
		
		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.PLAYER));
		
		SecurityContextHolder.getContext().setAuthentication(
			        new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));
		
		assertThrows(UnauthorizedException.class, () -> playerService.resetPassword(player));

	}
	
	@DisplayName("Attempt to change password for unexisting user")
	@Transactional
	@Test
	void changePasswordForunexistingUserTest(@Autowired PlayerRepository playerRepository) {
		
		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.ADMIN));
		
		SecurityContextHolder.getContext().setAuthentication(
			        new UsernamePasswordAuthenticationToken("authorized", "fake", authorities));

		Player unexistingPlayer = new Player();
		unexistingPlayer.setNick("unknown");


		assertThrows(NoSuchElementException.class, () -> playerService.resetPassword(unexistingPlayer));
	}

	@AfterAll
	public static void done(@Autowired PlayerRepository playerRepository) {

		playerRepository.delete(admin);
		log.info("Clean up completed");

	}

}
