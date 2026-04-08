package com.greg.golf.controller;

import com.greg.golf.controller.dto.CycleDto;
import com.greg.golf.controller.dto.CycleTournamentDto;
import com.greg.golf.entity.Cycle;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.CycleService;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.UserService;
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

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
class CycleControllerTest {

	@SuppressWarnings("unused")
	@MockitoBean
	private PlayerService playerService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtRequestFilter jwtRequestFilter;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@SuppressWarnings("unused")
	@MockitoBean
	private CycleService cycleService;

	@SuppressWarnings("unused")
	@MockitoBean
	private ModelMapper modelMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private PasswordEncoder bCryptPasswordEncoder;

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
	public CycleControllerTest(WebTestClient webTestClient) {
		this.webTestClient = webTestClient;
	}

	@DisplayName("Should add cycle with correct result")
	@Test
	@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
	void addCycleWhenValidInputThenReturns200() {

		var input = new CycleDto();
		input.setName("Test cycle");
		input.setStatus(Cycle.STATUS_OPEN);

		when(modelMapper.map(any(), any())).thenReturn(null);

		webTestClient.post()
				.uri("/rest/Cycle")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should add cycle tournament with correct result")
	@Test
	@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
	void addCycleTournamentWhenValidInputThenReturns200() {

		var input = new CycleTournamentDto();
		input.setName("Test cycle tournament");
		input.setBestOf(false);
		input.setRounds(1);

		when(modelMapper.map(any(), any())).thenReturn(null);

		webTestClient.post()
				.uri("/rest/CycleTournament")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should return all cycles")
	@Test
	void getCyclesThenReturns200() {

		when(cycleService.findAllCycles()).thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/Cycle")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should return all cycle tournaments")
	@Test
	void getCycleTournamentsThenReturns200() {

		when(cycleService.findAllCycleTournaments(1L))
				.thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/CycleTournament/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should return cycle results")
	@Test
	void getCycleResultsThenReturns200() {

		when(cycleService.findCycleResults(1L))
				.thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/CycleResult/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should close cycle with correct result")
	@Test
	@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
	void closeCycleWithValidInputThenReturns200() {

		doNothing().when(cycleService).closeCycle(any());

		webTestClient.patch()
				.uri("/rest/CycleClose/1")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should delete cycle tournament with correct result")
	@Test
	@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
	void deleteCycleTournamentWhenValidInputThenReturns200() {

		var input = new CycleDto();
		input.setName("Test cycle tournament");
		input.setStatus(Cycle.STATUS_OPEN);
		input.setBestRounds(1);
		input.setMaxWhs(12.0F);

		when(modelMapper.map(any(), any())).thenReturn(null);

		webTestClient.post()
				.uri("/rest/DeleteCycleTournament")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should delete cycle with correct result")
	@Test
	@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
	void deleteCycleWhenValidInputThenReturns200() {

		doNothing().when(cycleService).deleteCycle(any());

		webTestClient.delete()
				.uri("/rest/Cycle/1")
				.exchange()
				.expectStatus().isOk();
	}
}