package com.greg.golf.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.controller.dto.OnlineRoundDto;
import com.greg.golf.controller.dto.OnlineScoreCardDto;
import com.greg.golf.entity.OnlineRound;
import com.greg.golf.entity.OnlineScoreCard;
import com.greg.golf.entity.Player;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.service.OnlineRoundService;
import com.greg.golf.service.PlayerService;

@Slf4j
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = OnlineScoreCardController.class)
class OnlineScoreCardControllerTest {

	@SuppressWarnings("unused")
	@MockitoBean
	private OnlineRoundService onlineRoundService;

	@SuppressWarnings("unused")
	@MockitoBean
	private PlayerService playerService;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserService userService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtRequestFilter jwtRequestFilter;

	@SuppressWarnings("unused")
	@MockitoBean
	private PasswordEncoder bCryptPasswordEncoder;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@SuppressWarnings("unused")
	@MockitoBean
	private ModelMapper modelMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private SimpMessagingTemplate template;

	@SuppressWarnings("unused")
	@MockitoBean
	private GolfOAuth2UserService golfOAuth2UserService;

	@SuppressWarnings("unused")
	@MockitoBean
	private GolfAuthenticationSuccessHandler golfAuthenticationSuccessHandler;

	@SuppressWarnings("unused")
	@MockitoBean
	private GolfAuthenticationFailureHandler golfAuthenticationFailureHandler;

	private final MockMvc mockMvc;
	private final ObjectMapper objectMapper;

	@Autowired
	public OnlineScoreCardControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
	}

	@DisplayName("Should add online rounds with correct result")
	@Test
	void addOnlineRoundWhenValidInputThenReturns200() throws Exception {

		var input = new OnlineRoundDto();
		input.setTeeTime("10:00");
		input.setOwner(1L);
		input.setFinalized(false);
		input.setMatchPlay(false);

		var inputLst = new ArrayList<OnlineRoundDto>();
		inputLst.add(input);

		when(modelMapper.map(any(), any())).thenReturn(null);

		mockMvc.perform(post("/rest/OnlineRounds").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(inputLst))).andExpect(status().isOk()).andReturn();

	}
	
	@DisplayName("Should return all online rounds")
	@Test
	void getOnlineRoundsThenReturns200() throws Exception {
		
		var outputLst = new ArrayList<OnlineRound>();
		
		when(onlineRoundService.getOnlineRounds()).thenReturn(outputLst);

		mockMvc.perform(get("/rest/OnlineRound")).andExpect(status().isOk());

	}
	
	@DisplayName("Should return online score cards for online round")
	@Test
	void getOnlineScorecardsForRoundThenReturns200() throws Exception {
		
		var outputLst = new ArrayList<OnlineScoreCard>();
		
		
		when(onlineRoundService.getOnlineScoreCards(anyLong())).thenReturn(outputLst);

		mockMvc.perform(get("/rest/OnlineScoreCard/1")).andExpect(status().isOk());

	}
	
	@DisplayName("Should return online rounds for course")
	@Test
	void getOnlineRoundsForCourseThenReturns200() throws Exception {
		
		var outputLst = new ArrayList<OnlineRound>();
		
		
		when(onlineRoundService.getOnlineRoundsForCourse(anyLong())).thenReturn(outputLst);

		mockMvc.perform(get("/rest/OnlineRoundCourse/1")).andExpect(status().isOk());

	}

	@DisplayName("Should return player for nick")
	@Test
	void getPlayerForNickThenReturns200() throws Exception {
		
		
		when(playerService.getPlayerForNick(anyString())).thenReturn(new Player());

		mockMvc.perform(get("/rest/Player/test")).andExpect(status().isOk());

	}
	
	@DisplayName("Should delete online round for owner id")
	@Test
	void deleteOnlineRoundForOwnerThenReturns200() throws Exception {
		
		doNothing().when(onlineRoundService).deleteForOwner(anyLong());

		mockMvc.perform(delete("/rest/OnlineRoundForOwner/1")).andExpect(status().isOk());

	}
	
	@DisplayName("Should finalize online round for owner")
	@Test
	void finalizeOnlineRoundForOwnerThenReturns200() throws Exception {

		var input = 1;
		doNothing().when(onlineRoundService).finalizeForOwner(anyLong());
		
		mockMvc.perform(post("/rest/FinalizeOnlineOwnerRounds").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();

	}
	
	@DisplayName("Should return online rounds for owner")
	@Test
	void getOnlineRoundForOwnerThenReturns200() throws Exception {
				
		when(onlineRoundService.getOnlineRoundsForOwner(anyLong())).thenReturn(new ArrayList<>());

		mockMvc.perform(get("/rest/OnlineRoundOwner/1")).andExpect(status().isOk());

	}
	
	@DisplayName("Should save online scorecard")
	@Test
	void saveOnlineScorecard(@Autowired OnlineScoreCardController onlineScoreCardController) {
		
		var onlineScorecardDto = new OnlineScoreCardDto();
		onlineScorecardDto.setPenalty(0);
		onlineScorecardDto.setPutt(1);
		onlineScorecardDto.setStroke(3);
		onlineScorecardDto.setOrId(1);
		
		var onlineScorecard = new OnlineScoreCard();
		onlineScorecard.setPenalty(0);
		onlineScorecard.setPutt(1);
		onlineScorecard.setStroke(3);
		onlineScorecard.setOrId(1);
		onlineScorecard.setId(1L);

		when(modelMapper.map(any(),  any())).thenReturn(null);
		when(onlineRoundService.saveOnlineScoreCard(any())).thenReturn(new OnlineScoreCard());
		
		
		Assertions.assertNull(onlineScoreCardController.send(onlineScorecardDto));

	}

	@DisplayName("Should sync online scorecard with correct result")
	@Test
	void syncOnlineScoreCardWhenValidInputThenReturns200() throws Exception {

		var input = new OnlineRoundDto();
		input.setTeeTime("10:00");
		input.setOwner(1L);
		input.setFinalized(false);
		input.setMatchPlay(false);
		var inputLst = new ArrayList<OnlineRoundDto>();
		inputLst.add(input);

		doNothing().when(template).convertAndSend(anyString(), (OnlineScoreCardDto)any());

		mockMvc.perform(post("/rest/OnlineScoreCard").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(inputLst))).andExpect(status().isOk()).andReturn();

	}
}
