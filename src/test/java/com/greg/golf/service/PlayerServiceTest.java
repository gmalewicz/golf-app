package com.greg.golf.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import com.greg.golf.controller.dto.PlayerDto;
import com.greg.golf.error.PlayerNickInUseException;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.service.helpers.GolfUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class PlayerServiceTest {

	@SuppressWarnings("unused")
	@MockBean
	private JwtRequestFilter jwtRequestFilter;
	
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
		adminPlayer.setModified(false);
		admin = playerRepository.save(adminPlayer);

		log.info("Set up completed");
	}

	@DisplayName("Reset password with success")
	@Transactional
	@Test
	void changePasswordSuccessTest(@Autowired AuthenticationManager authenticationManager) {

		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.ADMIN));
		
		SecurityContextHolder.getContext().setAuthentication(
			        new UsernamePasswordAuthenticationToken("authorized", "fake", authorities));
		
		player.setPassword("test");
		playerService.resetPassword(player);
		player = playerService.getPlayer(player.getId()).orElseThrow();
		Assertions.assertDoesNotThrow(() -> authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(player.getNick(), "test")));
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
	
	@DisplayName("Attempt to change password for nonexistent user")
	@Transactional
	@Test
	void changePasswordForNonexistentUserTest(@Autowired PlayerRepository playerRepository) {
		
		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.ADMIN));
		
		SecurityContextHolder.getContext().setAuthentication(
			        new UsernamePasswordAuthenticationToken("authorized", "fake", authorities));

		Player nonexistentPlayer = new Player();
		nonexistentPlayer.setNick("unknown");
		nonexistentPlayer.setPassword("test");


		assertThrows(NoSuchElementException.class, () -> playerService.resetPassword(nonexistentPlayer));
	}

	@DisplayName("Process valid authentication")
	@Transactional
	@Test
	void authenticateValidPlayerTest() {

		Player player = new Player();
		player.setNick("golfer");
		player.setPassword("welcome");

		GolfUserDetails response = playerService.authenticatePlayer(player);
		Assertions.assertEquals(1L, response.getPlayer().getId());
	}

	@DisplayName("Process invalid password authentication")
	@Transactional
	@Test
	void authenticateInvalidPasswordTest() {

		Player player = new Player();
		player.setNick("golfer");
		player.setPassword("invalid");

		assertThrows(BadCredentialsException.class, () -> playerService.authenticatePlayer(player));
	}

	@DisplayName("Process invalid username authentication")
	@Transactional
	@Test
	void authenticateInvalidUserNameTest() {

		Player player = new Player();
		player.setNick("invalid");
		player.setPassword("welcome");

		assertThrows(BadCredentialsException.class, () -> playerService.authenticatePlayer(player));
	}

	@DisplayName("Add player test")
	@Transactional
	@Test
	void addPlayerTest() {

		Player player = new Player();
		player.setNick("test");
		player.setPassword("welcome");
		player.setCaptcha("ABCDE");
		player.setWhs(10.0F);
		player.setSex(Common.PLAYER_SEX_MALE);

		Assertions.assertDoesNotThrow(() -> playerService.addPlayer(player));

	}

	@DisplayName("Update player handicap")
	@Transactional
	@Test
	void updatePlayerWhsTest() {

		Player player = new Player();
		player.setWhs(10.0F);
		player.setId(1L);

		player = playerService.update(player);

		Assertions.assertEquals(10.0F, player.getWhs(), 0);
	}

	@DisplayName("Update player password")
	@Transactional
	@Test
	void updatePlayerPasswordTest(@Autowired PlayerRepository playerRepository) {

		String orgPlayerPwd = playerRepository.findById(1L).orElseThrow().getPassword();

		Player player = new Player();
		player.setPassword("newPassword");
		player.setId(1L);

		player = playerService.update(player);

		Assertions.assertNotSame(orgPlayerPwd, player.getPassword());
	}

	@DisplayName("Add player on behalf test")
	@Transactional
	@Test
	void addPlayerOnBehalfTest() {

		Player player = new Player();
		player.setNick("test");
		player.setPassword("welcome");
		player.setWhs(10.0F);
		player.setSex(Common.PLAYER_SEX_MALE);

		player = playerService.addPlayerOnBehalf(player);

		Assertions.assertNotNull(player.getId(), "Player id should not be null");
	}

	@DisplayName("Add player on behalf test which already exists")
	@Transactional
	@Test
	void addPlayerOnBehalfWhichAlreadyExistsTest() {

		Player player = new Player();
		player.setNick("golfer");
		player.setPassword("welcome");
		player.setWhs(10.0F);
		player.setSex(Common.PLAYER_SEX_MALE);

		assertThrows(PlayerNickInUseException.class, () -> playerService.addPlayerOnBehalf(player));
	}

	@AfterAll
	public static void done(@Autowired PlayerRepository playerRepository) {

		playerRepository.delete(admin);
		log.info("Clean up completed");

	}

}
