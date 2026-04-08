package com.greg.golf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.controller.dto.*;
import com.greg.golf.error.ApiErrorResponse;
import com.greg.golf.error.DeleteTournamentPlayerException;
import com.greg.golf.error.DuplicatePlayerInTournamentException;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.TournamentService;
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
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class TournamentControllerTest {

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtRequestFilter jwtRequestFilter;
	@SuppressWarnings("unused")
	@MockitoBean
	private TournamentService tournamentService;
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

	@Autowired
	private final WebTestClient webTestClient;

	@Autowired
	private final ObjectMapper objectMapper;

	@Autowired
	TournamentControllerTest(WebTestClient webTestClient, ObjectMapper objectMapper) {
		this.webTestClient = webTestClient;
		this.objectMapper = objectMapper;

	}

	// -------------------- BASIC GETS --------------------

	@Test
	@DisplayName("Should get all tournaments with correct result")
	void getTournamentsAndReturn200() {
		when(tournamentService.findAllTournamentsPageable(any()))
				.thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/Tournament/1")
				.exchange()
				.expectStatus().isOk();
	}

	@Test
	@DisplayName("Should get tournament results with correct result")
	void getTournamentResultsTest() {
		when(tournamentService.findAllTournamentsResults(1L))
				.thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/TournamentResult/1")
				.exchange()
				.expectStatus().isOk();
	}

	// -------------------- POST --------------------

	@Test
	@DisplayName("Should add tournament with correct result")
	void addTournamentWhenValidInputThenReturns200() throws Exception {
		var input = new TournamentDto();
		input.setId(1L);
		input.setName("Test");
		input.setStartDate(new Date(1));
		input.setEndDate(new Date(1));
		input.setBestRounds(0);

		when(modelMapper.map(any(), any())).thenReturn(null);

		webTestClient.post()
				.uri("/rest/Tournament")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.bodyValue(objectMapper.writeValueAsString(input))
				.exchange()
				.expectStatus().isOk();
	}

	@Test
	@DisplayName("Should add player participant to tournament")
	void addTournamentPlayerWhenValidInputThenReturns200() throws Exception {
		var input = new TournamentPlayerDto();
		input.setPlayerId(1L);
		input.setTournamentId(1L);

		doNothing().when(tournamentService).addPlayer(any());

		webTestClient.post()
				.uri("/rest/TournamentPlayer")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.bodyValue(objectMapper.writeValueAsString(input))
				.exchange()
				.expectStatus().isOk();
	}

	// -------------------- DELETE --------------------

	@Test
	@DisplayName("Should delete tournament")
	void deleteTournamentWhenValidInputThenReturns200() {
		doNothing().when(tournamentService).deleteTournament(anyLong());

		webTestClient.delete()
				.uri("/rest/Tournament/1")
				.exchange()
				.expectStatus().isOk();
	}

	// -------------------- ERROR CASES --------------------

	@Test
	@DisplayName("Delete player with results")
	void deletePlayer_withResults_thenReturns405() throws Exception {
		doThrow(new DeleteTournamentPlayerException())
				.when(tournamentService).deletePlayers(1L);

		var result = webTestClient.delete()
				.uri("/rest/TournamentPlayer/1")
				.exchange()
				.expectStatus().isEqualTo(405)
				.expectBody(String.class)
				.returnResult();

		String expected = objectMapper.writeValueAsString(
				new ApiErrorResponse("19",
						"Unable to delete player from tournament. Remove results first.")
		);

		assertThat(result.getResponseBody())
				.isEqualToIgnoringWhitespace(expected);
	}

	@Test
	@DisplayName("Attempt to add the player twice")
	void addPlayerTwice_thenReturns405() throws Exception {
		var input = new TournamentPlayerDto();
		input.setPlayerId(1L);
		input.setTournamentId(1L);

		doThrow(new DuplicatePlayerInTournamentException())
				.when(tournamentService).addPlayer(any());

		var result = webTestClient.post()
				.uri("/rest/TournamentPlayer")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.bodyValue(objectMapper.writeValueAsString(input))
				.exchange()
				.expectStatus().isEqualTo(405)
				.expectBody(String.class)
				.returnResult();

		String expected = objectMapper.writeValueAsString(
				new ApiErrorResponse("20",
						"Player already added to the tournament.")
		);

		assertThat(result.getResponseBody())
				.isEqualToIgnoringWhitespace(expected);
	}
}