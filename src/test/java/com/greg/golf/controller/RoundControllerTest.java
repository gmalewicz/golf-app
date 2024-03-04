package com.greg.golf.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import com.greg.golf.controller.dto.RoundWhsDto;
import com.greg.golf.controller.dto.SwapPlrRndDto;
import com.greg.golf.entity.*;
import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.error.ApiErrorResponse;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.RoundService;
import com.greg.golf.service.ScoreCardService;

@Slf4j
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
class RoundControllerTest {

	@SuppressWarnings("unused")
	@MockBean
	private RoundService roundService;

	@SuppressWarnings("unused")
	@MockBean
	private ScoreCardService scoreCardService;

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
	public RoundControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
	}

	@BeforeAll
	public static void setup() {

		log.info("Set up completed");
	}

	@DisplayName("Gets round for id")
	@Test
	void getsRoundForIdThenReturns200() throws Exception {

		when(roundService.getWithPlayers(1L)).thenReturn(java.util.Optional.of(new Round()));
		mockMvc.perform(get("/rest/Round/1")).andExpect(status().isOk());
	}


	@DisplayName("Add round with correct result")
	@Test
	void addRoundWhenValidInputThenReturns200() throws Exception {

		var valueCapture = ArgumentCaptor.forClass(Round.class);

		when(roundService.saveRound(valueCapture.capture())).thenReturn(null);

		String str = "{\"matchPlay\":false,\"roundDate\":\"2020/06/12 06:59\",\"course\":{\"id\":1,\"tees\":[{\"id\":1}]},\"player\":[{\"id\":1,\"whs\":32.1}],\"scoreCard\":[{\"hole\":1,\"stroke\":5,\"pats\":0,\"penalty\":0}]}";

		mockMvc.perform(post("/rest/Round").contentType("application/json").characterEncoding("utf-8").content(str))
				.andExpect(status().isOk()).andReturn();

	}

	@DisplayName("Gets rounds for player")
	@Test
	void getsRoundsForPlayerThenReturns200() throws Exception {

		var round = new Round();
		round.setId(1L);
		var lst = new ArrayList<Round>();
		lst.add(round);

		var playerCapture = ArgumentCaptor.forClass(Player.class);
		var pageCapture = ArgumentCaptor.forClass(Integer.class);

		when(roundService.listByPlayerPageable(playerCapture.capture(), pageCapture.capture())).thenReturn(lst);

		mockMvc.perform(get("/rest/Rounds/1/1")).andExpect(status().isOk());

	}

	@DisplayName("Gets recent rounds")
	@Test
	void getsRecentRoundsThenReturns200() throws Exception {

		var round = new Round();
		round.setId(1L);
		var lst = new ArrayList<Round>();
		lst.add(round);

		var pageCapture = ArgumentCaptor.forClass(Integer.class);

		when(roundService.getRecentRounds(pageCapture.capture())).thenReturn(lst);

		mockMvc.perform(get("/rest/RecentRounds/1")).andExpect(status().isOk());

	}

	@DisplayName("Gets score cards for round")
	@Test
	void getsScoreCardsForRoundThenReturns200() throws Exception {

		var scoreCard = new ScoreCard();
		scoreCard.setId(1L);
		var lst = new ArrayList<ScoreCard>();
		lst.add(scoreCard);

		var roundCapture = ArgumentCaptor.forClass(Round.class);

		when(scoreCardService.listByRound(roundCapture.capture())).thenReturn(lst);

		mockMvc.perform(get("/rest/ScoreCard/1")).andExpect(status().isOk());

	}

	@DisplayName("Delete score card with success")
	@Test
	void deleteScorecardThenReturns200() throws Exception {

		doNothing().when(roundService).deleteScorecard(anyLong(), anyLong());

		mockMvc.perform(delete("/rest/ScoreCard/1/1")).andExpect(status().isOk());
	}
	
	@DisplayName("Update scorecard with success")
	@Test
	void updateRoundThenReturns200() throws Exception {

		var roundCapture = ArgumentCaptor.forClass(Round.class);
	
		doNothing().when(roundService).updateScoreCard(roundCapture.capture());

		String str = "{\"matchPlay\":false,\"roundDate\":\"2020/06/12 06:59\",\"course\":{\"id\":1,\"tees\":[{\"id\":1}]},\"player\":[{\"id\":1,\"whs\":32.1}],\"scoreCard\":[{\"hole\":1,\"stroke\":5,\"pats\":0,\"penalty\":0}]}";

		mockMvc.perform(patch("/rest/ScoreCard").contentType("application/json").characterEncoding("utf-8").content(str))
				.andExpect(status().isOk()).andReturn();
	}
	
	@DisplayName("Get data for handicap calculation for a player")
	@Test
	void getHandicapDataForPlayerThenReturns200() throws Exception {

		var playerRound = new PlayerRound();
		playerRound.setId(1L);
		
		when(roundService.getForPlayerRoundDetails(anyLong(), anyLong())).thenReturn(playerRound);

		mockMvc.perform(get("/rest/RoundPlayerDetails/1/1")).andExpect(status().isOk());
	}
	
	@DisplayName("Get data for handicap calculation for all players")
	@Test
	void getHandicapDataForAllPlayersThenReturns200() throws Exception {

		var playerRound = new PlayerRound();
		playerRound.setId(1L);
		var lst = new ArrayList<PlayerRound>();
		lst.add(playerRound);
		
		when(roundService.getByRoundId(anyLong())).thenReturn(lst);

		mockMvc.perform(get("/rest/RoundPlayersDetails/1")).andExpect(status().isOk());
	}
	

	// the test is artificial as it is not possible to pass null as a round id
	// argument
	@DisplayName("Delete score card with null")
	@Test
	void deleteScorecard_whenInvalidInput_thenReturns400_2() throws Exception {

		doThrow(new InvalidDataAccessApiUsageException("")).when(roundService).deleteScorecard(1L, 1L);
		MvcResult mvcResult = mockMvc.perform(delete("/rest/ScoreCard/1/1")).andExpect(status().isBadRequest())
				.andReturn();

		String actualResponseBody = mvcResult.getResponse().getContentAsString();

		assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
				objectMapper.writeValueAsString(new ApiErrorResponse("17", "Incorrect parameter")));
	}

	@DisplayName("Should update player hcp for round with correct result")
	@Test
	void updPlrHcpForRoundWhenValidInputThenReturns200() throws Exception {

		var input = new RoundWhsDto();
		input.setRoundId(1L);
		input.setPlayerId(1L);
		input.setWhs(20.1F);

		doNothing().when(roundService).updateRoundWhs(any(), any(), any());

		mockMvc.perform(patch("/rest/UpdatePlayerRound").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Get player round count statistic")
	@Test
	void getPlayerRoundStatisticThenReturns200() throws Exception {

		when(playerService.getPlayerRoundCnt()).thenReturn(new ArrayList<>());

		mockMvc.perform(get("/rest/PlayerRoundCnt")).andExpect(status().isOk());
	}

	@DisplayName("Should swap players for round with correct result")
	@Test
	@WithMockUser(username="admin",roles={"USER","ADMIN"})
	void swapPlayerForRoundRoundWhenValidInputThenReturns200() throws Exception {

		var input = new SwapPlrRndDto();

		input.setRoundId(1L);
		input.setOldPlrId(2L);
		input.setNewPlrId(3L);

		doNothing().when(roundService).swapPlayer(any(), any(), any());

		mockMvc.perform(patch("/rest/SwapPlrRnd").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
