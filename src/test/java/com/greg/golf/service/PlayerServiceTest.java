package com.greg.golf.service;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.PlayerNickInUseException;
import com.greg.golf.error.TooShortStringForSearchException;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.service.helpers.GolfUser;
import com.greg.golf.service.helpers.GolfUserDetails;
import com.greg.golf.util.GolfPostgresqlContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class PlayerServiceTest {

	@SuppressWarnings("unused")
	@MockitoBean
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
		adminPlayer.setType(Common.TYPE_PLAYER_LOCAL);
		admin = playerRepository.save(adminPlayer);

		log.info("Set up completed");
	}

	@DisplayName("Reset password with success")
	@Transactional
	@Test
	void changePasswordSuccessTest(@Autowired AuthenticationManager authenticationManager) {

		player.setPassword("test");
		playerService.resetPassword(player);
		player = playerService.getPlayer(player.getId()).orElseThrow();
		assertDoesNotThrow(() -> authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(player.getNick(), "test")));
	}

	@DisplayName("Attempt to change password for nonexistent user")
	@Transactional
	@Test
	void changePasswordForNonexistentUserTest() {

		Player nonexistentPlayer = new Player();
		nonexistentPlayer.setNick("unknown");
		nonexistentPlayer.setPassword("test");


		assertThrows(NoSuchElementException.class, () -> playerService.resetPassword(nonexistentPlayer));
	}

	@DisplayName("Process valid authentication")
	@Transactional
	@Test
	void authenticateValidPlayerTest() {

		Player plr = new Player();
		plr.setNick("golfer");
		plr.setPassword("welcome");

		GolfUserDetails response = playerService.authenticatePlayer(plr);
		Assertions.assertEquals(1L, response.getPlayer().getId());
	}

	@DisplayName("Process invalid password authentication")
	@Transactional
	@Test
	void authenticateInvalidPasswordTest() {

		Player plr = new Player();
		plr.setNick("golfer");
		plr.setPassword("invalid");

		assertThrows(BadCredentialsException.class, () -> playerService.authenticatePlayer(plr));
	}

	@DisplayName("Process invalid username authentication")
	@Transactional
	@Test
	void authenticateInvalidUserNameTest() {

		Player plr = new Player();
		plr.setNick("invalid");
		plr.setPassword("welcome");

		assertThrows(BadCredentialsException.class, () -> playerService.authenticatePlayer(plr));
	}

	@DisplayName("Update player handicap")
	@Transactional
	@Test
	void updatePlayerWhsTest() {

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		Player updPlayer = new Player();
		updPlayer.setWhs(10.0F);
		updPlayer.setId(1L);

		updPlayer = playerService.update(updPlayer);

		Assertions.assertEquals(10.0F, updPlayer.getWhs(), 0);
	}

	@DisplayName("Update player password")
	@Transactional
	@Test
	void updatePlayerPasswordTest() {

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		String orgPlayerPwd = player.getPassword();

		Player updPlayer = new Player();
		updPlayer.setPassword("newPassword");
		updPlayer.setId(1L);

		updPlayer = playerService.update(updPlayer);

		Assertions.assertNotSame(orgPlayerPwd, updPlayer.getPassword());
	}

	@DisplayName("Add player on behalf test")
	@Transactional
	@Test
	void addPlayerOnBehalfTest() {

		Player plr = new Player();
		plr.setNick("test");
		plr.setPassword("welcome");
		plr.setWhs(10.0F);
		plr.setSex(Common.PLAYER_SEX_MALE);

		plr = playerService.addPlayerOnBehalf(plr);

		Assertions.assertNotNull(plr.getId(), "Player id should not be null");
	}

	@DisplayName("Add player on behalf test which already exists")
	@Transactional
	@Test
	void addPlayerOnBehalfWhichAlreadyExistsTest() {

		Player plr = new Player();
		plr.setNick("golfer");
		plr.setPassword("welcome");
		plr.setWhs(10.0F);
		plr.setSex(Common.PLAYER_SEX_MALE);

		assertThrows(PlayerNickInUseException.class, () -> playerService.addPlayerOnBehalf(plr));
	}

	@DisplayName("Generate tokens")
	@Transactional
	@Test
	void generateTokensTest() {

		Player plr = new Player();
		plr.setRole(Common.ROLE_PLAYER_REGULAR);
		plr.setId(1L);

		GolfUserDetails userDetails = new GolfUser("test", "welcome", new ArrayList<>(), plr);

		assertDoesNotThrow(() -> playerService.generateJwtToken(userDetails));
		assertDoesNotThrow(() -> playerService.generateRefreshToken(userDetails));
	}

	@DisplayName("Delete player test")
	@Transactional
	@Test
	void deletePlayerTest(@Autowired PlayerRepository playerRepository) {

		playerService.delete(1L);

		Assertions.assertEquals(1, playerRepository.findAll().size());
	}

	@DisplayName("Load user by Id test")
	@Transactional
	@Test
	void loadUserByIdTest() {

		GolfUserDetails userDetails = playerService.loadUserById(1L);

		Assertions.assertEquals("golfer", userDetails.getUsername());
	}

	@DisplayName("Update player on behalf test")
	@Transactional
	@Test
	void updatePlayerOnBehalfTest(@Autowired PlayerRepository playerRepository) {

		Player plr = new Player();
		plr.setId(1L);
		plr.setWhs(33.3F);
		plr.setNick("Test");
		plr.setSex(!Common.PLAYER_SEX_MALE);
		playerService.updatePlayerOnBehalf(plr, false);

		Player persistedPlayer = playerRepository.findById(1L).orElseThrow();

		Assertions.assertEquals("Test", persistedPlayer.getNick());
		Assertions.assertEquals(33.3F, persistedPlayer.getWhs());
		Assertions.assertEquals(!Common.PLAYER_SEX_MALE, persistedPlayer.getSex());
	}

	@DisplayName("Get player for nick test")
	@Transactional
	@Test
	void getPlayerForNickTest() {

		Player persistedPlayer = playerService.getPlayerForNick("golfer");

		Assertions.assertEquals(1L, persistedPlayer.getId());
	}

	@DisplayName("Process Oauth post login for unknown player test")
	@Transactional
	@Test
	void getProcessOauthUnknownPlayerTest() {

		String str = playerService.processOAuthPostLogin("Test", "Player", Common.TYPE_PLAYER_FACEBOOK);
		Player persistedPlayer = playerService.getPlayerForNick("Test.Pl");

		Assertions.assertTrue(str.contains("&new_player=true"));
		Assertions.assertNotNull(persistedPlayer);
	}

	@DisplayName("Process Oauth post login for known player test")
	@Transactional
	@Test
	void getProcessOauthKnownPlayerTest(@Autowired PlayerRepository playerRepository) {

		Player adminPlayer = new Player();
		adminPlayer.setNick("Test.Pl");
		adminPlayer.setPassword(player.getPassword());
		adminPlayer.setRole(Common.ROLE_PLAYER_ADMIN);
		adminPlayer.setWhs(10f);
		adminPlayer.setSex(Common.PLAYER_SEX_MALE);
		adminPlayer.setModified(false);
		adminPlayer.setType(Common.TYPE_PLAYER_FACEBOOK);
		playerRepository.save(adminPlayer);


		String retStr = playerService.processOAuthPostLogin("Test", "Player", Common.TYPE_PLAYER_FACEBOOK);

		Assertions.assertFalse(retStr.contains("&new_player=true"));
	}

	@DisplayName("Process Oauth post login for known player and invalid type test")
	@Transactional
	@Test
	void getProcessOauthKnownPlayerInvalidTypeTest(@Autowired PlayerRepository playerRepository) {

		Player adminPlayer = new Player();
		adminPlayer.setNick("Test.Pl");
		adminPlayer.setPassword(player.getPassword());
		adminPlayer.setRole(Common.ROLE_PLAYER_ADMIN);
		adminPlayer.setWhs(10f);
		adminPlayer.setSex(Common.PLAYER_SEX_MALE);
		adminPlayer.setModified(false);
		adminPlayer.setType(Common.TYPE_PLAYER_LOCAL);
		playerRepository.save(adminPlayer);

		String retStr = playerService.processOAuthPostLogin("Test", "Player", Common.TYPE_PLAYER_FACEBOOK);

		Assertions.assertNull(retStr);
	}

	@DisplayName("Process valid authentication with modified flag set")
	@Transactional
	@Test
	void authenticateValidPlayerWithModifiedFlagTest(@Autowired PlayerRepository playerRepository) {

		Player adminPlayer = new Player();
		adminPlayer.setNick("Test.Pl");
		adminPlayer.setPassword(player.getPassword());
		adminPlayer.setRole(Common.ROLE_PLAYER_ADMIN);
		adminPlayer.setWhs(10f);
		adminPlayer.setSex(Common.PLAYER_SEX_MALE);
		adminPlayer.setModified(true);
		adminPlayer.setType(Common.TYPE_PLAYER_LOCAL);
		playerRepository.save(adminPlayer);

		Player player = new Player();
		player.setNick("Test.Pl");
		player.setPassword("welcome");

		player = playerRepository.findById(admin.getId()).orElseThrow();

		Assertions.assertFalse(player.getModified());

	}

	@DisplayName("Search for player with too short string")
	@Transactional
	@Test
	void searchForPlayerWithTooShortStringTest() {

		assertThrows(TooShortStringForSearchException.class, () -> this.playerService.searchForPlayer("G", 0));
	}

	@DisplayName("Search for player with correct result")
	@Transactional
	@Test
	void searchForPlayerWithCorrectResultTest() {

		var retLst = this.playerService.searchForPlayer("Gol", 0);
		Assertions.assertEquals(1, retLst.size());

	}


	@DisplayName("Should update email by authorized user")
	@Transactional
	@Test
	void updateEmailByAuthorizedUserTest() {

		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.PLAYER));

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("1", "fake", authorities));

		var playerEmail = new Player();
		playerEmail.setId(1L);
		playerEmail.setEmail("mail@test.com");

		playerService.update(playerEmail);

		assertDoesNotThrow(() -> {
			Assertions.assertEquals("mail@test.com", playerService.getEmail(1L));
		});
	}


	@DisplayName("Delete email")
	@Transactional
	@Test
	void deleteEmailTest(@Autowired PlayerRepository playerRepository) {

		UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<SimpleGrantedAuthority>());

		var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		player.setEmail("test");
		playerRepository.save(player);
		playerService.deleteEmail();

		var newPlayer = playerRepository.findById(1L).orElseThrow();
		Assertions.assertNull(newPlayer.getEmail());
	}

	@AfterAll
	public static void done(@Autowired PlayerRepository playerRepository) {

		playerRepository.delete(admin);
		log.info("Clean up completed");

	}
}
