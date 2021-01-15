package com.greg.golf.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import com.greg.golf.controller.dto.PlayerDto;
import com.greg.golf.entity.Player;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.repository.PlayerRepository;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class AccessControllerTest {

	@Autowired
	private AccessController accessController;

	@BeforeAll
	public static void setup() {

		log.info("Set up completed");
	}

	@DisplayName("Process valid authentication")
	@Transactional
	@Test
	void authenticateValidPlayerTest() {

		PlayerDto playerDto = new PlayerDto();
		playerDto.setNick("golfer");
		playerDto.setPassword("welcome");

		ResponseEntity<PlayerDto> response = this.accessController.authenticatePlayer(playerDto);
		assertEquals(1L, response.getBody().getId().longValue());
	}

	@DisplayName("Process invalid password authentication")
	@Transactional
	@Test
	void authenticateInvalidPasswordTest() {

		PlayerDto playerDto = new PlayerDto();
		playerDto.setNick("golfer");
		playerDto.setPassword("invalid");

		assertThrows(BadCredentialsException.class, () -> this.accessController.authenticatePlayer(playerDto));
	}

	@DisplayName("Process invalid username authentication")
	@Transactional
	@Test
	void authenticateInvalidUserNameTest() {

		PlayerDto playerDto = new PlayerDto();
		playerDto.setNick("invalid");
		playerDto.setPassword("welcome");

		assertThrows(BadCredentialsException.class, () -> this.accessController.authenticatePlayer(playerDto));
	}

	@DisplayName("Add player test")
	@Transactional
	@Test
	void addPlayerTest() {

		PlayerDto playerDto = new PlayerDto();
		playerDto.setNick("test");
		playerDto.setPassword("welcome");
		playerDto.setCaptcha("ABCDE");
		playerDto.setWhs(10f);

		HttpStatus status = this.accessController.addPlayer(playerDto);

		assertEquals(HttpStatus.OK, status);
	}

	@DisplayName("Update player whs")
	@Transactional
	@Test
	void updatePlayerWhsTest(@Autowired PlayerRepository playerRepository) {

		PlayerDto playerDto = new PlayerDto();
		playerDto.setWhs(10f);
		playerDto.setId(1l);

		this.accessController.updatePlayer(playerDto);

		Optional<Player> player = playerRepository.findById(1l);

		assertEquals(10f, player.orElseThrow().getWhs().floatValue(), 0);
	}

	@DisplayName("Update player password")
	@Transactional
	@Test
	void updatePlayerPasswordTest(@Autowired PlayerRepository playerRepository) {

		String orgPlayerPwd = playerRepository.findById(1l).orElseThrow().getPassword();
		
		PlayerDto playerDto = new PlayerDto();
		playerDto.setPassword("newPassword");
		playerDto.setId(1l);

		this.accessController.updatePlayer(playerDto);

		Optional<Player> updPlayer = playerRepository.findById(1l);

		assertNotSame(orgPlayerPwd, updPlayer.orElseThrow().getPassword());
	}
	
	@DisplayName("Reset password by privileged user")
	@Transactional
	@Test
	void resetPlayerPasswordByPrivilagedUserTest(@Autowired PlayerRepository playerRepository) {

		String orgPlayerPwd = playerRepository.findById(1l).orElseThrow().getPassword();
		
		PlayerDto playerDto = new PlayerDto();
		playerDto.setPassword("newPassword");
		playerDto.setNick("golfer");
		playerDto.setId(1l);

		this.accessController.resetPassword(1l, playerDto);

		Optional<Player> updPlayer = playerRepository.findById(1l);

		assertNotSame(orgPlayerPwd, updPlayer.orElseThrow().getPassword());
	}
	
	@DisplayName("Reset password by unathorized user")
	@Transactional
	@Test
	void resetPlayerPasswordByUnauthorizedUserTest(@Autowired PlayerRepository playerRepository) {

		Player orgPlayer = playerRepository.findById(1l).orElseThrow();
		orgPlayer.setRole(1);
		playerRepository.save(orgPlayer);
		
		PlayerDto playerDto = new PlayerDto();
		playerDto.setPassword("newPassword");
		playerDto.setNick("golfer");
		playerDto.setId(1l);

		assertThrows(UnauthorizedException.class, () -> this.accessController.resetPassword(1l, playerDto));
	}
	
	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
