package com.greg.golf.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.junit.ClassRule;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.greg.golf.configurationproperties.JwtConfig;
import com.greg.golf.controller.dto.PlayerDto;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.PlayerNickInUseException;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.security.JwtTokenUtil;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class AccessControllerTest {

	@ClassRule
	public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
			.getInstance();

	private final AccessController accessController;
	private static JwtTokenUtil jwtTokenUtil;

	@Autowired
	public AccessControllerTest(AccessController accessController) {

		this.accessController = accessController;
	}

	@BeforeAll
	public static void setup(@Autowired JwtConfig jwtConfig) {

		jwtTokenUtil = new JwtTokenUtil(jwtConfig);
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
		Assertions.assertEquals(1L, Objects.requireNonNull(response.getBody()).getId().longValue());
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
		playerDto.setSex(Common.PLAYER_SEX_MALE);

		HttpStatus status = this.accessController.addPlayer(playerDto);

		Assertions.assertEquals(HttpStatus.OK, status);
	}

	@DisplayName("Update player whs")
	@Transactional
	@Test
	void updatePlayerWhsTest(@Autowired PlayerRepository playerRepository) {

		PlayerDto playerDto = new PlayerDto();
		playerDto.setWhs(10f);
		playerDto.setId(1L);

		this.accessController.updatePlayer(playerDto);

		Optional<Player> player = playerRepository.findById(1L);

		Assertions.assertEquals(10f, player.orElseThrow().getWhs(), 0);
	}

	@DisplayName("Update player password")
	@Transactional
	@Test
	void updatePlayerPasswordTest(@Autowired PlayerRepository playerRepository) {

		String orgPlayerPwd = playerRepository.findById(1L).orElseThrow().getPassword();

		PlayerDto playerDto = new PlayerDto();
		playerDto.setPassword("newPassword");
		playerDto.setId(1L);

		this.accessController.updatePlayer(playerDto);

		Optional<Player> updPlayer = playerRepository.findById(1L);

		Assertions.assertNotSame(orgPlayerPwd, updPlayer.orElseThrow().getPassword());
	}

	@DisplayName("Reset password by privileged user")
	@Transactional
	@Test
	void resetPlayerPasswordByPrivilegedUserTest(@Autowired PlayerRepository playerRepository) {

		String orgPlayerPwd = playerRepository.findById(1L).orElseThrow().getPassword();
		
		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.ADMIN));
		
		SecurityContextHolder.getContext().setAuthentication(
			        new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

		PlayerDto playerDto = new PlayerDto();
		playerDto.setPassword("newPassword");
		playerDto.setNick("golfer");
		playerDto.setId(1L);

		this.accessController.resetPassword(playerDto);

		Optional<Player> updPlayer = playerRepository.findById(1L);

		Assertions.assertNotSame(orgPlayerPwd, updPlayer.orElseThrow().getPassword());
	}

	@DisplayName("Reset password by unauthorized user")
	@Transactional
	@Test
	void resetPlayerPasswordByUnauthorizedUserTest(@Autowired PlayerRepository playerRepository) {
		
		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.PLAYER));
		
		SecurityContextHolder.getContext().setAuthentication(
			        new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));

		

		PlayerDto playerDto = new PlayerDto();
		playerDto.setPassword("newPassword");
		playerDto.setNick("golfer");
		playerDto.setId(1L);

		assertThrows(UnauthorizedException.class, () -> this.accessController.resetPassword(playerDto));
	}

	@DisplayName("Add player on behalf test")
	@Transactional
	@Test
	void addPlayeronBehalfTest() {

		PlayerDto playerDto = new PlayerDto();
		playerDto.setNick("test");
		playerDto.setPassword("welcome");
		playerDto.setWhs(10f);
		playerDto.setSex(Common.PLAYER_SEX_MALE);

		ResponseEntity<PlayerDto> response = this.accessController.addPlayerOnBehalf(playerDto);

		Assertions.assertNotNull(Objects.requireNonNull(response.getBody()).getId(), "Player id should not be null");
	}

	@DisplayName("Add player on behalf test which already exists")
	@Transactional
	@Test
	void addPlayerOnBehalfWhichAlredyExistsTest() {

		PlayerDto playerDto = new PlayerDto();
		playerDto.setNick("golfer");
		playerDto.setPassword("welcome");
		playerDto.setWhs(10f);
		playerDto.setSex(Common.PLAYER_SEX_MALE);

		assertThrows(PlayerNickInUseException.class, () -> this.accessController.addPlayerOnBehalf(playerDto));
	}

	@DisplayName("Should attempt refresh player token unsuccessfully")
	@Transactional
	@Test
	void attemptToRefreshPlayerTokenTest() {

		HttpServletRequest request = mock(HttpServletRequest.class);
		try {

			accessController.refreshToken(request, 1L);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}

	}

	@DisplayName("Should attempt refresh player token successfully")
	@Transactional
	@Test
	void attemptToRefreshPlayerTokenSuccessfullyTest() {

		String jwtToken = jwtTokenUtil.generateToken("1");

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getAttribute("refreshToken")).thenReturn(jwtToken);
		try {

			accessController.refreshToken(request, 1L);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}

	}

	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
