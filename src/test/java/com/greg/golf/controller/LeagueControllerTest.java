package com.greg.golf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.controller.dto.LeagueDto;
import com.greg.golf.controller.dto.LeagueMatchDto;
import com.greg.golf.controller.dto.LeaguePlayerDto;
import com.greg.golf.controller.dto.PlayerDto;
import com.greg.golf.entity.League;
import com.greg.golf.entity.LeagueMatch;
import com.greg.golf.entity.LeaguePlayer;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.LeagueService;
import com.greg.golf.service.PlayerService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = LeagueController.class)
class LeagueControllerTest {
	@SuppressWarnings("unused")
	@MockBean
	private PlayerService playerService;
	@SuppressWarnings("unused")
	@MockBean
	private JwtRequestFilter jwtRequestFilter;
	@SuppressWarnings("unused")
	@MockBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	@SuppressWarnings("unused")
	@MockBean
	private LeagueService leagueService;
	@SuppressWarnings("unused")
	@MockBean
	private ModelMapper modelMapper;

	@SuppressWarnings("unused")
	@MockBean
	private PasswordEncoder bCryptPasswordEncoder;

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
	public LeagueControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
	}

	@BeforeAll
	public static void setup() {

		log.info("Set up completed");
	}

	@DisplayName("Should add league with correct result")
	@Test
	void addLeagueWhenValidInputThenReturns200() throws Exception {

		var input = new LeagueDto();
		input.setName("Test league");
		input.setStatus(League.STATUS_OPEN);

		var playerDto = new PlayerDto();
		playerDto.setId(1L);

		input.setPlayer(playerDto);

		//when(modelMapper.map(any(), any())).thenReturn(null);
		doNothing().when(leagueService).addLeague(any());

		mockMvc.perform(post("/rest/League").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should return all leagues")
	@Test
	void getLeaguesThenReturns200() throws Exception {

		var outputLst = new ArrayList<League>();

		when(leagueService.findAllLeaguesPageable(any())).thenReturn(outputLst);

		mockMvc.perform(get("/rest/League/0")).andExpect(status().isOk());

	}

	@DisplayName("Should add player to league with correct result")
	@Test
	void addPlayerWhenValidInputThenReturns200() throws Exception {

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

		mockMvc.perform(post("/rest/LeaguePlayer").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should delete league single player")
	@Test
	void deleteLeaguePlayerWhenValidInputThenReturns200() throws Exception {

		doNothing().when(leagueService).deletePlayer(anyLong(), anyLong());
		mockMvc.perform(delete("/rest/LeaguePlayer/1/1")).andExpect(status().isOk());
	}

	@DisplayName("Should get all players belonging to league with correct result")
	@Test
	void getAllLeaguePlayersTest() throws Exception {

		var outputLst = new ArrayList<LeaguePlayer>();

		when(leagueService.getLeaguePlayers(any())).thenReturn(outputLst);
		mockMvc.perform(get("/rest/LeaguePlayer/1")).andExpect(status().isOk());
	}

	@DisplayName("Should close league with correct result")
	@Test
	void closeLeagueWithValidInputThenReturns200() throws Exception {

		doNothing().when(leagueService).closeLeague(any());

		mockMvc.perform(patch("/rest/LeagueClose/1")).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should get all league matches with correct result")
	@Test
	void getAllLeagueMatchesTest() throws Exception {

		var outputLst = new ArrayList<LeagueMatch>();

		when(leagueService.getMatches(any())).thenReturn(outputLst);
		mockMvc.perform(get("/rest/LeagueMatch/1")).andExpect(status().isOk());
	}

	@DisplayName("Should add match to league with correct result")
	@Test
	void addMatchWhenValidInputThenReturns200() throws Exception {

		var input = new LeagueMatchDto();

		var leagueDto = new LeagueDto();
		leagueDto.setName("Test league");
		leagueDto.setStatus(false);
		input.setLeague(leagueDto);

		input.setResult("A/S");
		input.setLooserId(1L);
		input.setWinnerId(2L);

		doNothing().when(leagueService).addMatch(any());

		mockMvc.perform(post("/rest/LeagueMatch").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should delete match")
	@Test
	void deleteLeagueMatchWhenValidInputThenReturns200() throws Exception {

		doNothing().when(leagueService).deleteMatch(anyLong(), anyLong(), anyLong());
		mockMvc.perform(delete("/rest/LeagueMatch/1/1/1")).andExpect(status().isOk());
	}

	@DisplayName("Should delete league")
	@Test
	void deleteLeagueWhenValidInputThenReturns200() throws Exception {

		doNothing().when(leagueService).deleteLeague(anyLong());
		mockMvc.perform(delete("/rest/League/1")).andExpect(status().isOk());
	}

	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
