package com.greg.golf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.controller.dto.CycleDto;
import com.greg.golf.controller.dto.CycleTournamentDto;
import com.greg.golf.entity.Cycle;
import com.greg.golf.entity.CycleResult;
import com.greg.golf.entity.CycleTournament;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@Slf4j
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = CycleController.class)
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

	private final MockMvc mockMvc;
	private final ObjectMapper objectMapper;

	@Autowired
	public CycleControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
	}

	@DisplayName("Should add cycle with correct result")
	@Test
	@WithMockUser(username="admin",roles={"USER","ADMIN"})
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
	@WithMockUser(username="admin",roles={"USER","ADMIN"})
	void addCycleTournamentWhenValidInputThenReturns200() throws Exception {

		var input = new CycleTournamentDto();
		input.setName("Test cycle tournament");
		input.setBestOf(false);
		input.setRounds(1);

		when(modelMapper.map(any(), any())).thenReturn(null);

		mockMvc.perform(post("/rest/CycleTournament").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should return all cycles")
	@Test
	void getCyclesThenReturns200() throws Exception {

		var outputLst = new ArrayList<Cycle>();

		when(cycleService.findAllCycles()).thenReturn(outputLst);

		mockMvc.perform(get("/rest/Cycle")).andExpect(status().isOk());

	}

	@DisplayName("Should return all cycle tournaments")
	@Test
	void getCycleTournamentsThenReturns200() throws Exception {

		var outputLst = new ArrayList<CycleTournament>();

		when(cycleService.findAllCycleTournaments(1L)).thenReturn(outputLst);

		mockMvc.perform(get("/rest/CycleTournament/1")).andExpect(status().isOk());

	}

	@DisplayName("Should return cycle results")
	@Test
	void getCycleResultsThenReturns200() throws Exception {

		var outputLst = new ArrayList<CycleResult>();

		when(cycleService.findCycleResults(1L)).thenReturn(outputLst);

		mockMvc.perform(get("/rest/CycleResult/1")).andExpect(status().isOk());

	}

	@DisplayName("Should close cycle with correct result")
	@Test
	@WithMockUser(username="admin",roles={"USER","ADMIN"})
	void closeCycleWithValidInputThenReturns200() throws Exception {

		doNothing().when(cycleService).closeCycle(any());

		mockMvc.perform(patch("/rest/CycleClose/1")).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should delete cycle tournament with correct result")
	@Test
	@WithMockUser(username="admin",roles={"USER","ADMIN"})
	void deleteCycleTournamentWhenValidInputThenReturns200() throws Exception {

		var input = new CycleDto();
		input.setName("Test cycle tournament");
		input.setStatus(Cycle.STATUS_OPEN);
		input.setBestRounds(1);
		input.setMaxWhs(12.0F);

		when(modelMapper.map(any(), any())).thenReturn(null);

		mockMvc.perform(post("/rest/DeleteCycleTournament").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should delete cycle with correct result")
	@Test
	@WithMockUser(username="admin",roles={"USER","ADMIN"})
	void deleteCycleWhenValidInputThenReturns200() throws Exception {

		doNothing().when(cycleService).deleteCycle(any());
		mockMvc.perform(delete("/rest/Cycle/1")).andExpect(status().isOk()).andReturn();
	}
}
