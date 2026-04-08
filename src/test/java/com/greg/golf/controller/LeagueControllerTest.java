package com.greg.golf.controller;

import com.greg.golf.controller.dto.*;
import com.greg.golf.entity.League;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.oauth.*;
import com.greg.golf.service.LeagueService;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;



import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
class LeagueControllerTest {

	private final WebTestClient webTestClient;

	/* === Mocked collaborators === */

	@SuppressWarnings("unused")
	@MockitoBean private PlayerService playerService;
	@SuppressWarnings("unused")
	@MockitoBean private LeagueService leagueService;
	@SuppressWarnings("unused")
	@MockitoBean private UserService userService;
	@SuppressWarnings("unused")
	@MockitoBean private ModelMapper modelMapper;
	@SuppressWarnings("unused")
	@MockitoBean private PasswordEncoder bCryptPasswordEncoder;
	@SuppressWarnings("unused")
	@MockitoBean private JwtRequestFilter jwtRequestFilter;
	@SuppressWarnings("unused")
	@MockitoBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	@SuppressWarnings("unused")
	@MockitoBean private GolfOAuth2UserService golfOAuth2UserService;
	@SuppressWarnings("unused")
	@MockitoBean private GolfAuthenticationSuccessHandler golfAuthenticationSuccessHandler;
	@SuppressWarnings("unused")
	@MockitoBean private GolfAuthenticationFailureHandler golfAuthenticationFailureHandler;

	@Autowired
	public LeagueControllerTest(WebTestClient webTestClient) {
		this.webTestClient = webTestClient;
	}

	/* === Tests === */

	@DisplayName("Should add league with correct result")
	@Test
	void addLeagueWhenValidInputThenReturns200() {

		var input = new LeagueDto();
		input.setName("Test league");
		input.setStatus(League.STATUS_OPEN);

		var playerDto = new PlayerDto();
		playerDto.setId(1L);
		input.setPlayer(playerDto);

		doNothing().when(leagueService).addLeague(any());

		webTestClient.post()
				.uri("/rest/League")
				.bodyValue(input)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should return all leagues")
	@Test
	void getLeaguesThenReturns200() {

		when(leagueService.findAllLeaguesPageable(any()))
				.thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/League/0")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should add player to league with correct result")
	@Test
	void addPlayerWhenValidInputThenReturns200() {

		var input = new LeaguePlayerDto();
		input.setPlayerId(1L);
		input.setNick("Greg");

		var leagueDto = new LeagueDto();
		leagueDto.setName("Test league");
		leagueDto.setStatus(false);

		var playerDto = new PlayerDto();
		playerDto.setId(1L);

		leagueDto.setPlayer(playerDto);
		input.setLeague(leagueDto);

		doNothing().when(leagueService).addPlayer(any());

		webTestClient.post()
				.uri("/rest/LeaguePlayer")
				.bodyValue(input)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should delete league single player")
	@Test
	void deleteLeaguePlayerWhenValidInputThenReturns200() {

		doNothing().when(leagueService).deletePlayer(anyLong(), anyLong());

		webTestClient.delete()
				.uri("/rest/LeaguePlayer/1/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should get all players belonging to league")
	@Test
	void getAllLeaguePlayersTest() {

		when(leagueService.getLeaguePlayers(any()))
				.thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/LeaguePlayer/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should close league with correct result")
	@Test
	void closeLeagueWithValidInputThenReturns200() {

		doNothing().when(leagueService).closeLeague(any());

		webTestClient.patch()
				.uri("/rest/LeagueClose/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should get all league matches")
	@Test
	void getAllLeagueMatchesTest() {

		when(leagueService.getMatches(any()))
				.thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/LeagueMatch/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should add match to league")
	@Test
	void addMatchWhenValidInputThenReturns200() {

		var input = new LeagueMatchDto();
		var leagueDto = new LeagueDto();
		leagueDto.setName("Test league");
		leagueDto.setStatus(false);

		input.setLeague(leagueDto);
		input.setResult("A/S");
		input.setLooserId(1L);
		input.setWinnerId(2L);

		doNothing().when(leagueService).addMatch(any());

		webTestClient.post()
				.uri("/rest/LeagueMatch")
				.bodyValue(input)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should delete match")
	@Test
	void deleteLeagueMatchWhenValidInputThenReturns200() {

		doNothing().when(leagueService).deleteMatch(anyLong(), anyLong(), anyLong());

		webTestClient.delete()
				.uri("/rest/LeagueMatch/1/1/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should delete league")
	@Test
	void deleteLeagueWhenValidInputThenReturns200() {

		doNothing().when(leagueService).deleteLeague(anyLong());

		webTestClient.delete()
				.uri("/rest/League/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should process notification")
	@Test
	void processNotificationWhenValidInputThenReturns200() {

		when(leagueService.processNotifications(any(), any()))
				.thenReturn(0);

		var input = new LeagueResultDto();
		input.setNick("Test");
		input.setBig(1);
		input.setSmall(1);
		input.setMatchesPlayed(1);

		webTestClient.post()
				.uri("/rest/League/Notification/1")
				.bodyValue(new LeagueResultDto[]{input})
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should add notification")
	@Test
	void addNotificationWhenValidInputThenReturns200() {

		doNothing().when(leagueService).addNotification(any());

		webTestClient.post()
				.uri("/rest/League/AddNotification/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should remove notification")
	@Test
	void removeNotificationWhenValidInputThenReturns200() {

		doNothing().when(leagueService).removeNotification(any());

		webTestClient.post()
				.uri("/rest/League/RemoveNotification/1")
				.exchange()
				.expectStatus().isOk();
	}
}