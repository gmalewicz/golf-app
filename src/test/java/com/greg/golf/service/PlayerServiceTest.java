package com.greg.golf.service;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.greg.golf.entity.Player;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.repository.PlayerRepository;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class PlayerServiceTest {

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
		adminPlayer.setRole(Player.ROLE_PLAYER_ADMIN);
		adminPlayer.setWhs(10f);
		admin = playerRepository.save(adminPlayer);

		log.info("Set up completed");
	}

	@DisplayName("Change password with success")
	@Transactional
	@Test
	void changePasswordSuccessTest() {

		player.setPassword("test");
		player = playerService.resetPassword(admin.getId(), player);
		assertEquals("test", player.getPassword());

	}

	@DisplayName("Unauthorized attempt to change password")
	@Transactional
	@Test
	void changePasswordUnauthorizedTest(@Autowired PlayerRepository playerRepository) {

		player.setPassword("test");
		admin.setRole(Player.ROLE_PLAYER_REGULAR);
		admin = playerRepository.save(admin);
		Long adminId = admin.getId();

		assertThrows(UnauthorizedException.class, () -> playerService.resetPassword(adminId, player));

	}

	@DisplayName("Attempt to change password by user who does not exist")
	@Transactional
	@Test
	void changePasswordAdminNoExistsTest(@Autowired PlayerRepository playerRepository) {

		player.setPassword("test");
		// admin.setRole(Player.ROLE_PLAYER_REGULAR);
		// admin = playerRepository.save(admin);

		assertThrows(EntityNotFoundException.class, () -> {
			playerService.resetPassword(1500L, player);
		});
	}

	@DisplayName("Attempt to change password for unexisting user")
	@Transactional
	@Test
	void changePasswordForunexistingUserTest(@Autowired PlayerRepository playerRepository) {

		Player unexistingPlayer = new Player();
		unexistingPlayer.setNick("unknown");
		// admin.setRole(Player.ROLE_PLAYER_REGULAR);
		Long adminId = admin.getId();

		assertThrows(NoSuchElementException.class, () -> playerService.resetPassword(adminId, unexistingPlayer));
	}

	@AfterAll
	public static void done(@Autowired PlayerRepository playerRepository) {

		playerRepository.delete(admin);
		log.info("Clean up completed");

	}

}
