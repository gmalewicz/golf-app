package com.greg.golf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.controller.dto.*;
import com.greg.golf.entity.Player;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.JwtTokenUtil;
import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.UserService;
import com.greg.golf.service.helpers.GolfUser;
import com.greg.golf.service.helpers.GolfUserDetails;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AccessController.class)
class AccessControllerTest {

	@SuppressWarnings("unused")
	@MockitoBean
	private PlayerService playerService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtRequestFilter jwtRequestFilter;

	@SuppressWarnings("unused")
	@MockitoBean
	private ModelMapper modelMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private PasswordEncoder bCryptPasswordEncoder;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

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
	@MockitoBean
	private JwtTokenUtil jwtTokenUtil;

	private final MockMvc mockMvc;
	private final ObjectMapper objectMapper;

	@Autowired
	public AccessControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
	}

	@DisplayName("Should authenticate with correct result")
	@Test
	void processAuthenticationWhenValidInputThenReturns200() throws Exception {

		var input = new PlayerCredentialsDto();
		input.setNick("Test");
		input.setPassword("Password");

		Player player = new Player();
		GolfUserDetails userDetails = new GolfUser("test", "welcome", new ArrayList<>(), player);

		when(playerService.authenticatePlayer(any())).thenReturn(userDetails);
		when(playerService.generateJwtToken(any())).thenReturn("jwtToken");
		when(playerService.generateRefreshToken(any())).thenReturn("refreshToken");

		mockMvc.perform(post("/rest/Authenticate").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should add player with correct result")
	@Test
	void processAddPlayerWhenValidInputThenReturns200() throws Exception {

		var input = new PlayerRegistrationDto();
		input.setNick("Test");
		input.setPassword("Password");
		input.setSex(false);
		input.setCaptcha("captcha");
		input.setWhs(1.0F);

		doNothing().when(playerService).addPlayer(any());

		mockMvc.perform(post("/rest/AddPlayer").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should update player with correct result")
	@Test
	void processUpdatePlayerWhenValidInputThenReturns200() throws Exception {

		var input = new PlayerUpdateDto();
		input.setId(1L);
		input.setPassword("Password");

		when(playerService.update(any())).thenReturn(null);

		mockMvc.perform(patch("/rest/PatchPlayer").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should reset password by privileged user with correct result")
	@Test
	@WithMockUser(username="admin",roles={"USER","ADMIN"})
	void processPasswordByPrivilegedUserWhenValidInputThenReturns200() throws Exception {

		var input = new PlayerCredentialsDto();
		input.setNick("Test");
		input.setPassword("Password");

		doNothing().when(playerService).resetPassword(any());

		mockMvc.perform(patch("/rest/ResetPassword").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should add player on behalf with correct result")
	@Test
	void processAddPlayerOnBehalfWhenValidInputThenReturns200() throws Exception {

		var output = new Player();
		output.setNick("Test");
		output.setSex(false);
		output.setWhs(1.0F);

		when(playerService.addPlayerOnBehalf(any())).thenReturn(output);

		mockMvc.perform(post("/rest/AddPlayerOnBehalf").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(output))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should attempt to refresh token without header with correct result")
	@Test
	void processAttemptRefreshTokenWithoutHeaderThenReturns200() throws Exception {

		mockMvc.perform(get("/rest/Refresh/1")).andExpect(status().isOk());
	}

	@DisplayName("Should refresh token with correct result")
	@Test
	void processRefreshTokenThenReturns200() throws Exception {

		Player player = new Player();
		GolfUserDetails userDetails = new GolfUser("test", "welcome", new ArrayList<>(), player);

		when(playerService.loadUserById(any())).thenReturn(userDetails);
		when(playerService.generateJwtToken(any())).thenReturn("jwtToken");
		when(playerService.generateRefreshToken(any())).thenReturn("refreshToken");

		mockMvc.perform(get("/rest/Refresh/1").requestAttr("refreshToken", "exists")).andExpect(status().isOk());
	}

	@DisplayName("Should delete with correct result")
	@Test
	@WithMockUser(username="admin",roles={"USER","ADMIN"})
	void processDeletePlayerWhenValidInputThenReturns200() throws Exception {

		var input = new PlayerDto();
		input.setNick("Test");
		input.setId(1L);

		doNothing().when(playerService).delete(any());

		mockMvc.perform(post("/rest/DeletePlayer").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should update player on behalf with correct result")
	@Test
	void processUpdatePlayerOnBehalfWhenValidInputThenReturns200() throws Exception {

		var input = new PlayerDto();
		input.setNick("Test");
		input.setPassword("Password");

		doNothing().when(playerService).updatePlayerOnBehalf(any(), anyBoolean());

		mockMvc.perform(patch("/rest/UpdatePlayerOnBehalf").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should update player on behalf with social media flag set and correct result")
	@Test
	void processUpdatePlayerOnBehalfWithSocialMediaFlagSetWhenValidInputThenReturns200() throws Exception {

		var input = new PlayerDto();
		input.setNick("Test");
		input.setPassword("Password");
		input.setUpdateSocial(true);

		doNothing().when(playerService).updatePlayerOnBehalf(any(), anyBoolean());

		mockMvc.perform(patch("/rest/UpdatePlayerOnBehalf").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should update player on behalf with social media flag set false and correct result")
	@Test
	void processUpdatePlayerOnBehalfWithSocialMediaFlagSetFalseWhenValidInputThenReturns200() throws Exception {

		var input = new PlayerDto();
		input.setNick("Test");
		input.setPassword("Password");
		input.setUpdateSocial(false);

		doNothing().when(playerService).updatePlayerOnBehalf(any(), anyBoolean());

		mockMvc.perform(patch("/rest/UpdatePlayerOnBehalf").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should request social media player data")
	@Test
	void requestSocialMediaPlayerDataWhenValidInputThenReturns200() throws Exception {

		Player player = new Player();
		player.setNick("test");
		GolfUserDetails gu = new GolfUser("test", "test", new ArrayList<>(), player);

		when(jwtTokenUtil.getUserIdFromToken(any())).thenReturn("1");
		when(playerService.loadUserById(any())).thenReturn(gu);
		when(playerService.generateRefreshToken(any())).thenReturn("1");

		mockMvc.perform(get("/rest/GetSocialPlayer").header("Authorization","Bearer 1234")).andExpect(status().isOk());
	}

	@DisplayName("Should search for player with correct result")
	@Test
	void searchForPlayerWhenValidInputThenReturns200() throws Exception {

		var input = new PlayerNickDto();
		input.setNick("Test");
		input.setPage(1);

		var output = new ArrayList<Player>();

		when(playerService.searchForPlayer(any(), any())).thenReturn(output);

		mockMvc.perform(post("/rest/SearchForPlayer").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@DisplayName("Should remove email")
	@Test
	void removeEmailThenReturns200() throws Exception {

		doNothing().when(playerService).deleteEmail();

		mockMvc.perform(post("/rest/DeletePlayerEmail").contentType("application/json"))
				.andExpect(status().isOk()).andReturn();
	}
}
