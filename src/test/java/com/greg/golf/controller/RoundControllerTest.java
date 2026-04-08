package com.greg.golf.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import com.greg.golf.controller.dto.RoundWhsDto;
import com.greg.golf.controller.dto.SwapPlrRndDto;
import com.greg.golf.entity.*;
import com.greg.golf.error.ApiErrorResponse;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.oauth.*;
import com.greg.golf.service.*;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@SpringBootTest
class RoundControllerTest {

	@SuppressWarnings("unused")
	@MockitoBean private RoundService roundService;
	@SuppressWarnings("unused")
	@MockitoBean private ScoreCardService scoreCardService;
	@SuppressWarnings("unused")
	@MockitoBean private PlayerService playerService;
	@SuppressWarnings("unused")
	@MockitoBean private JwtRequestFilter jwtRequestFilter;
	@SuppressWarnings("unused")
	@MockitoBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	@SuppressWarnings("unused")
	@MockitoBean private ModelMapper modelMapper;
	@SuppressWarnings("unused")
	@MockitoBean private PasswordEncoder bCryptPasswordEncoder;
	@SuppressWarnings("unused")
	@MockitoBean private UserService userService;
	@SuppressWarnings("unused")
	@MockitoBean private GolfOAuth2UserService golfOAuth2UserService;
	@SuppressWarnings("unused")
	@MockitoBean private GolfAuthenticationSuccessHandler golfAuthenticationSuccessHandler;
	@SuppressWarnings("unused")
	@MockitoBean private GolfAuthenticationFailureHandler golfAuthenticationFailureHandler;

	@Autowired
	private final WebTestClient webTestClient;

	@Autowired
	RoundControllerTest(WebTestClient webTestClient) {
		this.webTestClient = webTestClient;
	}

	@DisplayName("Gets round for id")
	@Test
	void getsRoundForIdThenReturns200() {
		when(roundService.getWithPlayers(1L))
				.thenReturn(java.util.Optional.of(new Round()));

		webTestClient.get()
				.uri("/rest/Round/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Add round with correct result")
	@Test
	void addRoundWhenValidInputThenReturns200() {
		ArgumentCaptor<Round> captor = ArgumentCaptor.forClass(Round.class);
		when(roundService.saveRound(captor.capture())).thenReturn(null);

		String json = """  
            {
              "matchPlay": false,
              "roundDate": "2020/06/12 06:59",
              "course": { "id": 1, "tees": [{ "id": 1 }] },
              "player": [{ "id": 1, "whs": 32.1 }],
              "scoreCard": [{ "hole": 1, "stroke": 5, "pats": 0, "penalty": 0 }]
            }
            """;

		webTestClient.post()
				.uri("/rest/Round")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.bodyValue(json)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Gets rounds for player")
	@Test
	void getsRoundsForPlayerThenReturns200() {
		var round = new Round();
		round.setId(1L);

		when(roundService.listByPlayerPageable(any(Player.class), anyInt()))
				.thenReturn(new ArrayList<>(java.util.List.of(round)));

		webTestClient.get()
				.uri("/rest/Rounds/1/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Gets recent rounds")
	@Test
	void getsRecentRoundsThenReturns200() {
		var round = new Round();
		round.setId(1L);

		when(roundService.getRecentRounds(anyInt()))
				.thenReturn(new ArrayList<>(java.util.List.of(round)));

		webTestClient.get()
				.uri("/rest/RecentRounds/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Gets score cards for round")
	@Test
	void getsScoreCardsForRoundThenReturns200() {
		var sc = new ScoreCard();
		sc.setId(1L);

		when(scoreCardService.listByRound(any(Round.class)))
				.thenReturn(new ArrayList<>(java.util.List.of(sc)));

		webTestClient.get()
				.uri("/rest/ScoreCard/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Delete score card with success")
	@Test
	void deleteScorecardThenReturns200() {
		doNothing().when(roundService).deleteScorecard(anyLong(), anyLong());

		webTestClient.delete()
				.uri("/rest/ScoreCard/1/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Delete score card with invalid input returns 400")
	@Test
	void deleteScorecard_whenInvalidInput_thenReturns400() {
		doThrow(new InvalidDataAccessApiUsageException(""))
				.when(roundService).deleteScorecard(1L, 1L);

		webTestClient.delete()
				.uri("/rest/ScoreCard/1/1")
				.exchange()
				.expectStatus().isBadRequest()
				.expectBody(ApiErrorResponse.class)
				.value(resp ->
						assertThat(resp)
								.usingRecursiveComparison()
								.isEqualTo(new ApiErrorResponse("17", "Incorrect parameter"))
				);
	}

	@DisplayName("Update player WHS for round")
	@Test
	void updPlrHcpForRoundWhenValidInputThenReturns200() {
		var dto = new RoundWhsDto();
		dto.setRoundId(1L);
		dto.setPlayerId(1L);
		dto.setWhs(20.1F);

		doNothing().when(roundService).updateRoundWhs(any(), any(), any());

		webTestClient.patch()
				.uri("/rest/UpdatePlayerRound")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.bodyValue(dto)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Get player round count statistic")
	@Test
	void getPlayerRoundStatisticThenReturns200() {
		when(playerService.getPlayerRoundCnt()).thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/PlayerRoundCnt")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Swap players for round")
	@Test
	@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
	void swapPlayerForRoundRoundWhenValidInputThenReturns200() {
		var dto = new SwapPlrRndDto();
		dto.setRoundId(1L);
		dto.setOldPlrId(2L);
		dto.setNewPlrId(3L);

		doNothing().when(roundService).swapPlayer(any(), any(), any());

		webTestClient.patch()
				.uri("/rest/SwapPlrRnd")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.bodyValue(dto)
				.exchange()
				.expectStatus().isOk();
	}
}