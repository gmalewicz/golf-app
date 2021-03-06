package com.greg.golf.controller;

import static org.assertj.core.api.Assertions.assertThat;
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.PlayerRound;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.error.ApiErrorResponse;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.RoundService;
import com.greg.golf.service.ScoreCardService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = RoundController.class)
class RoundControllerTest {

	@MockBean
	private RoundService roundService;

	@MockBean
	private ScoreCardService scoreCardService;

	@MockBean
	private PlayerService playerService;

	@MockBean
	private JwtRequestFilter jwtRequestFilter;

	@MockBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@MockBean
	private ModelMapper modelMapper;

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
		round.setId(1l);
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
		round.setId(1l);
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
		scoreCard.setId(1l);
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
		playerRound.setId(1l);
		
		when(roundService.getForPlayerRoundDetails(anyLong(), anyLong())).thenReturn(playerRound);

		mockMvc.perform(get("/rest/RoundPlayerDetails/1/1")).andExpect(status().isOk());
	}
	
	@DisplayName("Get data for handicap calculation for all players")
	@Test
	void getHandicapDataForAllPlayersThenReturns200() throws Exception {

		var playerRound = new PlayerRound();
		playerRound.setId(1l);
		var lst = new ArrayList<PlayerRound>();
		lst.add(playerRound);
		
		when(roundService.getByRoundId(anyLong())).thenReturn(lst);

		mockMvc.perform(get("/rest/RoundPlayersDetails/1")).andExpect(status().isOk());
	}
	

	// the test is artificial as it is not possible to pass null as an round id
	// argument
	@DisplayName("Delete score card with null")
	@Test
	void deleteScorecard_whenInvalidInput_thenReturns400_2() throws Exception {

		doThrow(new InvalidDataAccessApiUsageException(null)).when(roundService).deleteScorecard(1l, 1l);
		MvcResult mvcResult = mockMvc.perform(delete("/rest/ScoreCard/1/1")).andExpect(status().isBadRequest())
				.andReturn();

		String actualResponseBody = mvcResult.getResponse().getContentAsString();

		assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
				objectMapper.writeValueAsString(new ApiErrorResponse("17", "Incorrect parameter")));
	}

	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
