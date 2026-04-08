package com.greg.golf.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import com.greg.golf.controller.dto.*;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.OnlineRoundService;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OnlineScoreCardControllerTest {

	@SuppressWarnings("unused")
	@MockitoBean private OnlineRoundService onlineRoundService;
	@SuppressWarnings("unused")
	@MockitoBean private PlayerService playerService;
	@SuppressWarnings("unused")
	@MockitoBean private UserService userService;
	@SuppressWarnings("unused")
	@MockitoBean private JwtRequestFilter jwtRequestFilter;
	@SuppressWarnings("unused")
	@MockitoBean private PasswordEncoder bCryptPasswordEncoder;
	@SuppressWarnings("unused")
	@MockitoBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	@SuppressWarnings("unused")
	@MockitoBean private ModelMapper modelMapper;
	@SuppressWarnings("unused")
	@MockitoBean private SimpMessagingTemplate template;
	@SuppressWarnings("unused")
	@MockitoBean private GolfOAuth2UserService golfOAuth2UserService;
	@SuppressWarnings("unused")
	@MockitoBean private GolfAuthenticationSuccessHandler golfAuthenticationSuccessHandler;
	@SuppressWarnings("unused")
	@MockitoBean private GolfAuthenticationFailureHandler golfAuthenticationFailureHandler;

	@Autowired
	private final WebTestClient webTestClient;

	@Autowired
	OnlineScoreCardControllerTest(WebTestClient webTestClient) {
		this.webTestClient = webTestClient;
	}

	@DisplayName("Should add online rounds with correct result")
	@Test
	void addOnlineRoundWhenValidInputThenReturns200() {

		var input = new OnlineRoundDto();
		input.setTeeTime("10:00");
		input.setCourse(new CourseDto());
		input.setPlayer(new PlayerDto());
		input.setOwner(1L);
		input.setIdentifier(1);
		input.setFinalized(false);
		input.setCourseTee(new CourseTeeDto());
		input.setFormat(Common.STROKE_PLAY_FORMAT);

		var inputLst = new ArrayList<OnlineRoundDto>();
		inputLst.add(input);

		when(modelMapper.map(any(), any())).thenReturn(null);

		webTestClient.post()
				.uri("/rest/OnlineRounds")
				.bodyValue(inputLst)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should return all online rounds")
	@Test
	void getOnlineRoundsThenReturns200() {
		when(onlineRoundService.getOnlineRounds())
				.thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/OnlineRound/all")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should return online score cards for online round")
	@Test
	void getOnlineScorecardsForRoundThenReturns200() {
		when(onlineRoundService.getOnlineScoreCards(anyLong()))
				.thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/OnlineScoreCard/{id}", 1)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should return online rounds for course")
	@Test
	void getOnlineRoundsForCourseThenReturns200() {
		when(onlineRoundService.getOnlineRoundsForCourse(anyLong()))
				.thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/OnlineRoundCourse/{id}", 1)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should return player for nick")
	@Test
	void getPlayerForNickThenReturns200() {
		when(playerService.getPlayerForNick(anyString()))
				.thenReturn(new Player());

		webTestClient.get()
				.uri("/rest/Player/{nick}", "test")
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should delete online round for identifier")
	@Test
	void deleteOnlineRoundForIdentifierThenReturns200() {
		doNothing().when(onlineRoundService).deleteForIdentifier(anyInt());

		webTestClient.delete()
				.uri("/rest/OnlineRound/{id}", 1)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should finalize online round for identifier")
	@Test
	void finalizeOnlineRoundForIdentifierThenReturns200() {
		doNothing().when(onlineRoundService).finish(anyInt());

		webTestClient.post()
				.uri("/rest/OnlineRound")
				.bodyValue(1)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should return online rounds for identifier")
	@Test
	void getOnlineRoundForIdentifierThenReturns200() {
		when(onlineRoundService.getOnlineRoundsForIdentifier(anyInt()))
				.thenReturn(new ArrayList<>());

		webTestClient.get()
				.uri("/rest/OnlineRound/Identifier/{id}", 1)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Should sync online scorecard with correct result")
	@Test
	void syncOnlineScoreCardWhenValidInputThenReturns200() {

		var input = new OnlineRoundDto();
		input.setTeeTime("10:00");
		input.setOwner(1L);
		input.setFinalized(false);
		input.setFormat(Common.STROKE_PLAY_FORMAT);

		var inputLst = new ArrayList<OnlineRoundDto>();
		inputLst.add(input);

		doNothing().when(template)
				.convertAndSend(anyString(), any(OnlineScoreCardDto.class));

		webTestClient.post()
				.uri("/rest/OnlineScoreCard")
				.bodyValue(inputLst)
				.exchange()
				.expectStatus().isOk();
	}
}