package com.greg.golf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.controller.dto.LeagueDto;
import com.greg.golf.entity.League;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = LeagueController.class)
class LeagueControllerTest {

	@MockBean
	private PlayerService playerService;

	@MockBean
	private JwtRequestFilter jwtRequestFilter;

	@MockBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@MockBean
	private LeagueService leagueService;

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

		when(modelMapper.map(any(), any())).thenReturn(null);

		mockMvc.perform(post("/rest/League").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should return all leagues")
	@Test
	void getLeaguesThenReturns200() throws Exception {

		var outputLst = new ArrayList<League>();

		when(leagueService.findAllLeagues()).thenReturn(outputLst);

		mockMvc.perform(get("/rest/League")).andExpect(status().isOk());

	}


	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
