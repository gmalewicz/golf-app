package com.greg.golf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.controller.dto.CycleTournamentDto;
import com.greg.golf.controller.dto.LimitedRoundDto;
import com.greg.golf.controller.dto.RoundDto;
import com.greg.golf.controller.dto.TournamentDto;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.Tournament;
import com.greg.golf.entity.TournamentResult;
import com.greg.golf.entity.TournamentRound;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.TournamentService;
import com.greg.golf.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = TournamentController.class)
class TournamentControllerTest {

	@SuppressWarnings("unused")
	@MockBean
	private JwtRequestFilter jwtRequestFilter;

	@SuppressWarnings("unused")
	@MockBean
	private TournamentService tournamentService;

	@SuppressWarnings("unused")
	@MockBean
	private ModelMapper modelMapper;

	@SuppressWarnings("unused")
	@MockBean
	private PasswordEncoder bCryptPasswordEncoder;

	@SuppressWarnings("unused")
	@MockBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@SuppressWarnings("unused")
	@MockBean
	private UserService userService;

	@SuppressWarnings("unused")
	@MockBean
	private GolfOAuth2UserService golfOAuth2UserService;

	@SuppressWarnings("unused")
	@MockBean
	private GolfAuthenticationSuccessHandler golfAuthenticationSuccessHandler;

	@SuppressWarnings("unused")
	@MockBean
	private GolfAuthenticationFailureHandler golfAuthenticationFailureHandler;

	private final MockMvc mockMvc;
	private final ObjectMapper objectMapper;

	@Autowired
	public TournamentControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
	}

	@BeforeAll
	public static void setup() {

		log.info("Set up completed");
	}

	@DisplayName("Should get all tournaments with correct result")
	@Test
	void getTournamentsAndReturn200() throws Exception {

		var outputLst = new ArrayList<Tournament>();

		when(tournamentService.findAllTournaments()).thenReturn(outputLst);
		mockMvc.perform(get("/rest/Tournament")).andExpect(status().isOk());

	}

	@DisplayName("Should get tournament results with correct result")
	@Test
	void getTournamentResultsTest() throws Exception {

		var outputLst = new ArrayList<TournamentResult>();

		when(tournamentService.findAllTournamentsResults(1L)).thenReturn(outputLst);
		mockMvc.perform(get("/rest/TournamentResult/1")).andExpect(status().isOk());
	}

	@DisplayName("Should get applicable rounds for tournament with correct result")
	@Test
	void getRoundsForTournamentTest() throws Exception {

		var outputLst = new ArrayList<Round>();

		when(tournamentService.getAllPossibleRoundsForTournament(1L)).thenReturn(outputLst);
		mockMvc.perform(get("/rest/TournamentRounds/1")).andExpect(status().isOk());
	}

	@DisplayName("Should add round to tournament with correct result")
	@Test
	void addRoundToTournamentWhenValidInputThenReturns200() throws Exception {

		var input = new LimitedRoundDto();
		input.setId(1L);

		when(modelMapper.map(any(), any())).thenReturn(null);

		mockMvc.perform(post("/rest/TournamentRound/1").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should add tournament with correct result")
	@Test
	void addTournamentWhenValidInputThenReturns200() throws Exception {

		var input = new TournamentDto();
		input.setId(1L);
		input.setName("Test");
		input.setStartDate(new Date(1));
		input.setEndDate(new Date(1));
		input.setBestRounds(0);

		when(modelMapper.map(any(), any())).thenReturn(null);

		mockMvc.perform(post("/rest/Tournament").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should get rounds added for tournament with correct result")
	@Test
	void getAddedRoundsForTournamentTest() throws Exception {

		var outputLst = new ArrayList<TournamentRound>();

		when(tournamentService.getTournamentRoundsForResult(1L)).thenReturn(outputLst);
		mockMvc.perform(get("/rest/TournamentResultRound/1")).andExpect(status().isOk());
	}

	@DisplayName("Should add round for tournament on behalf with correct result")
	@Test
	void addRoundOnBehalfWhenValidInputThenReturns200() throws Exception {

		var input = new RoundDto();
		input.setId(1L);

		var outputLst = new TournamentRound();

		when(tournamentService.addRoundOnBehalf(any(), any())).thenReturn(outputLst);


		when(modelMapper.map(any(), any())).thenReturn(null);

		mockMvc.perform(post("/rest/TournamentRoundOnBehalf/1").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should delete result from tournament")
	@Test
	void deleteResultFromTournamentWhenValidInputThenReturns200() throws Exception {

		doNothing().when(tournamentService).deleteResult(anyLong());
		mockMvc.perform(delete("/rest/TournamentResult/1")).andExpect(status().isOk());
	}

	@DisplayName("Should close tournament with correct result")
	@Test
	void closeTournamentWithValidInputThenReturns200() throws Exception {

		doNothing().when(tournamentService).closeTournament(any());

		mockMvc.perform(patch("/rest/TournamentClose/1")).andExpect(status().isOk()).andReturn();
	}

	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}
}
