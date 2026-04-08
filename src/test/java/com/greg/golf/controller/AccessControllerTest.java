package com.greg.golf.controller;

import com.greg.golf.controller.dto.*;
import com.greg.golf.entity.Player;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.JwtTokenUtil;
import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.UserService;
import com.greg.golf.service.helpers.GolfUser;
import com.greg.golf.service.helpers.GolfUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.MediaType;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
class AccessControllerTest {

	@SuppressWarnings("unused")
	@MockitoBean
	private PlayerService playerService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtRequestFilter jwtRequestFilter;

	@SuppressWarnings("unused")
	@MockitoBean
	private ModelMapper modelMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private PasswordEncoder bCryptPasswordEncoder;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserService userService;

	@SuppressWarnings("unused")
	@MockitoBean
	private GolfOAuth2UserService golfOAuth2UserService;

	@SuppressWarnings("unused")
	@MockitoBean
	private GolfAuthenticationSuccessHandler golfAuthenticationSuccessHandler;

	@SuppressWarnings("unused")
	@MockitoBean
	private GolfAuthenticationFailureHandler golfAuthenticationFailureHandler;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtTokenUtil jwtTokenUtil;

	private final WebTestClient webTestClient;

	@Autowired
	public AccessControllerTest(WebTestClient webTestClient) {
		this.webTestClient = webTestClient;
	}

	@DisplayName("Should authenticate with correct result")
	@Test
	void processAuthenticationWhenValidInputThenReturns200() {

		var input = new PlayerCredentialsDto();
		input.setNick("Test");
		input.setPassword("Password");

		Player player = new Player();
		GolfUserDetails userDetails = new GolfUser("test", "welcome", new ArrayList<>(), player);

		when(playerService.authenticatePlayer(any())).thenReturn(userDetails);
		when(playerService.generateJwtToken(any())).thenReturn("jwtToken");
		when(playerService.generateRefreshToken(any())).thenReturn("refreshToken");

		webTestClient.post()
				.uri("/rest/Authenticate")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should add player with correct result")
	@Test
	void processAddPlayerWhenValidInputThenReturns200() {

		var input = new PlayerRegistrationDto();
		input.setNick("Test");
		input.setPassword("Password");
		input.setSex(false);
		input.setCaptcha("captcha");
		input.setWhs(1.0F);

		doNothing().when(playerService).addPlayer(any());

		webTestClient.post()
				.uri("/rest/AddPlayer")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should update player with correct result")
	@Test
	void processUpdatePlayerWhenValidInputThenReturns200() {

		var input = new PlayerUpdateDto();
		input.setId(1L);
		input.setPassword("Password");

		when(playerService.update(any())).thenReturn(null);

		webTestClient.patch()
				.uri("/rest/PatchPlayer")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should reset password by privileged user with correct result")
	@Test
	@WithMockUser(username="admin",roles={"USER","ADMIN"})
	void processPasswordByPrivilegedUserWhenValidInputThenReturns200() {

		var input = new PlayerCredentialsDto();
		input.setNick("Test");
		input.setPassword("Password");

		doNothing().when(playerService).resetPassword(any());

		webTestClient.patch()
				.uri("/rest/ResetPassword")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should add player on behalf with correct result")
	@Test
	void processAddPlayerOnBehalfWhenValidInputThenReturns200() {

		var output = new Player();
		output.setNick("Test");
		output.setSex(false);
		output.setWhs(1.0F);

		when(playerService.addPlayerOnBehalf(any())).thenReturn(output);

		webTestClient.post()
				.uri("/rest/AddPlayerOnBehalf")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(output)
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should attempt to refresh token without header with correct result")
	@Test
	void processAttemptRefreshTokenWithoutHeaderThenReturns200() {

		webTestClient.get()
				.uri("/rest/Refresh/1")
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should refresh token with correct result")
	@Test
	void processRefreshTokenThenReturns200() {

		Player player = new Player();
		GolfUserDetails userDetails = new GolfUser("test", "welcome", new ArrayList<>(), player);

		when(playerService.loadUserById(any())).thenReturn(userDetails);
		when(playerService.generateJwtToken(any())).thenReturn("jwtToken");
		when(playerService.generateRefreshToken(any())).thenReturn("refreshToken");

		webTestClient.get()
				.uri("/rest/Refresh/1")
				.attribute("refreshToken", "exists")
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should delete with correct result")
	@Test
	@WithMockUser(username="admin",roles={"USER","ADMIN"})
	void processDeletePlayerWhenValidInputThenReturns200() {

		var input = new PlayerDto();
		input.setNick("Test");
		input.setId(1L);

		doNothing().when(playerService).delete(any());

		webTestClient.post()
				.uri("/rest/DeletePlayer")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should update player on behalf with correct result")
	@Test
	void processUpdatePlayerOnBehalfWhenValidInputThenReturns200() {

		var input = new PlayerDto();
		input.setNick("Test");
		input.setPassword("Password");

		doNothing().when(playerService).updatePlayerOnBehalf(any(), anyBoolean());

		webTestClient.patch()
				.uri("/rest/UpdatePlayerOnBehalf")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should update player on behalf with social media flag set and correct result")
	@Test
	void processUpdatePlayerOnBehalfWithSocialMediaFlagSetWhenValidInputThenReturns200() {

		var input = new PlayerDto();
		input.setNick("Test");
		input.setPassword("Password");
		input.setUpdateSocial(true);

		doNothing().when(playerService).updatePlayerOnBehalf(any(), anyBoolean());

		webTestClient.patch()
				.uri("/rest/UpdatePlayerOnBehalf")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should update player on behalf with social media flag set false and correct result")
	@Test
	void processUpdatePlayerOnBehalfWithSocialMediaFlagSetFalseWhenValidInputThenReturns200() {

		var input = new PlayerDto();
		input.setNick("Test");
		input.setPassword("Password");
		input.setUpdateSocial(false);

		doNothing().when(playerService).updatePlayerOnBehalf(any(), anyBoolean());

		webTestClient.patch()
				.uri("/rest/UpdatePlayerOnBehalf")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should request social media player data")
	@Test
	void requestSocialMediaPlayerDataWhenValidInputThenReturns200() {

		Player player = new Player();
		player.setNick("test");
		GolfUserDetails gu = new GolfUser("test", "test", new ArrayList<>(), player);

		when(jwtTokenUtil.getUserIdFromToken(any())).thenReturn("1");
		when(playerService.loadUserById(any())).thenReturn(gu);
		when(playerService.generateRefreshToken(any())).thenReturn("1");

		webTestClient.get()
				.uri("/rest/GetSocialPlayer")
				.header("Authorization","Bearer 1234")
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should search for player with correct result")
	@Test
	void searchForPlayerWhenValidInputThenReturns200() {

		var input = new PlayerNickDto();
		input.setNick("Test");
		input.setPage(1);

		var output = new ArrayList<Player>();

		when(playerService.searchForPlayer(any(), any())).thenReturn(output);

		webTestClient.post()
				.uri("/rest/SearchForPlayer")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus()
				.isOk();
	}

	@DisplayName("Should remove email")
	@Test
	void removeEmailThenReturns200() {

		doNothing().when(playerService).deleteEmail();

		webTestClient.post()
				.uri("/rest/DeletePlayerEmail")
				.exchange()
				.expectStatus()
				.isOk();
	}
}
