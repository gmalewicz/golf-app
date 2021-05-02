package com.greg.golf.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.error.ApiErrorResponse;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;

import com.greg.golf.service.PlayerService;
import com.greg.golf.service.RoundService;
import com.greg.golf.service.ScoreCardService;

import lombok.extern.log4j.Log4j2;

import static org.mockito.BDDMockito.*;

@Log4j2
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = RoundController.class)
class RoundControllerMockTest {

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
	public RoundControllerMockTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
	}

	@BeforeAll
	public static void setup() {

		log.info("Set up completed");
	}
	
	// the test is artificial as it is not possible to pass null as an round id argument
	@DisplayName("Delete score card with null")
	@Test
	void deleteScorecard_whenInvalidInput_thenReturns400_2() throws Exception {

		doThrow(new InvalidDataAccessApiUsageException(null)).when(roundService).deleteScorecard(1l, 1l);
		MvcResult mvcResult = mockMvc.perform(delete("/rest/ScoreCard/1/1")).andExpect(status().isBadRequest()).andReturn();

		String actualResponseBody = mvcResult.getResponse().getContentAsString();

		assertThat(actualResponseBody)
				.isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(new ApiErrorResponse("17", "Incorrect parameter")));
	}
	
	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
