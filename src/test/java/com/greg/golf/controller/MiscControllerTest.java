package com.greg.golf.controller;

import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.UserService;
import com.greg.golf.util.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(MiscController.class)
@Import(CacheConfig.class)
@AutoConfigureMockMvc(addFilters = false)
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

	@SuppressWarnings("unused")
	@Autowired
	private MockMvc mockMvc;

	@DisplayName("Should return version")
	@Test
	void getVersionThenReturns200() throws Exception {

		mockMvc.perform(get("/rest/Version")).andExpect(status().isOk());

	}
}
