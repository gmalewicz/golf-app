package com.greg.golf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.controller.dto.CycleDto;
import com.greg.golf.controller.dto.CycleTournamentDto;
import com.greg.golf.entity.Cycle;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.service.CycleService;
import com.greg.golf.service.PlayerService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = CycleController.class)
class CycleControllerTest {

	@MockBean
	private PlayerService playerService;

	@MockBean
	private JwtRequestFilter jwtRequestFilter;

	@MockBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@MockBean
	private CycleService cycleService;

	@MockBean
	private ModelMapper modelMapper;

	private final MockMvc mockMvc;
	private final ObjectMapper objectMapper;

	@Autowired
	public CycleControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
	}

	@BeforeAll
	public static void setup() {

		log.info("Set up completed");
	}

	@DisplayName("Should add cycle with correct result")
	@Test
	void addCycleWhenValidInputThenReturns200() throws Exception {

		var input = new CycleDto();
		input.setName("Test cycle");
		input.setStatus(Cycle.STATUS_OPEN);

		when(modelMapper.map(any(), any())).thenReturn(null);

		mockMvc.perform(post("/rest/Cycle").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should add cycle tournament with correct result")
	@Test
	void addCycleTournamentWhenValidInputThenReturns200() throws Exception {

		var input = new CycleTournamentDto();
		input.setName("Test cycle tournament");
		input.setBestOf(false);
		input.setRounds(1);

		when(modelMapper.map(any(), any())).thenReturn(null);

		mockMvc.perform(post("/rest/CycleTournament").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
