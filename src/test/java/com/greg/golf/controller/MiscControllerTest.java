package com.greg.golf.controller;

import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = MiscController.class)
class MiscControllerTest {
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

	@Autowired
	public MiscControllerTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@BeforeAll
	public static void setup() {

		log.info("Set up completed");
	}

	@DisplayName("Should return version")
	@Test
	void getVersionThenReturns200() throws Exception {

		mockMvc.perform(get("/rest/Version")).andExpect(status().isOk());

	}

	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
