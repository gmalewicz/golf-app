package com.greg.golf.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.ClassRule;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;

import com.greg.golf.configurationproperties.JwtConfig;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.service.PlayerService;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class JwtRequestFilterTest {

	@ClassRule
	public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
			.getInstance();

	@Autowired
	private PlayerService playerService;

	@Autowired
	JwtConfig jwtConfig;

	private static JwtTokenUtil jwtTokenUtil;
	private static RefreshTokenUtil refreshTokenUtil;
	private static HttpServletRequest request;
	private static HttpServletResponse response;
	private static FilterChain filterChain;

	@BeforeAll
	public static void setup(@Autowired JwtConfig jwtConfig) {

		jwtTokenUtil = new JwtTokenUtil(jwtConfig);
		refreshTokenUtil = new RefreshTokenUtil(jwtConfig);
		response = mock(HttpServletResponse.class);

		filterChain = mock(FilterChain.class);

		log.info("Set up completed");
	}

	@BeforeEach
	public void setupBeforeEach() {

		request = mock(HttpServletRequest.class);
		SecurityContextHolder.getContext().setAuthentication(null);
		log.debug("Set up before each completed");
	}

	@DisplayName("Should process request without token not throwing any exception")
	@Transactional
	@Test
	void requestWithoutTokenTest() {

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}

	@DisplayName("Should process request with JWT token in header")
	@Transactional
	@Test
	void requestWithTokenInHeaderTest() {

		String jwtToken = jwtTokenUtil.generateToken("1");

		when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}

	@DisplayName("Should process request with JWT token in parameter")
	@Transactional
	@Test
	void requestWithTokenInParameterTest() {

		String jwtToken = jwtTokenUtil.generateToken("1");

		when(request.getHeader("Authorization")).thenReturn(null);
		when(request.getParameter("token")).thenReturn(jwtToken);

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}

	@DisplayName("Should process request with invalid header")
	@Transactional
	@Test
	void requestWithInvalidHeaderTest() {

		when(request.getHeader("Authorization")).thenReturn("invalid");

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}

	@DisplayName("Should process request with invalid token")
	@Transactional
	@Test
	void requestWithInvalidTokenTest() {

		when(request.getHeader("Authorization")).thenReturn("Bearer 1234");

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}

	@DisplayName("Should process request with expired token")
	@Transactional
	@Test
	void requestWithExpiredTokenTest() {
		
		when(request.getRequestURI()).thenReturn("test");

		when(request.getHeader("Authorization")).thenReturn(
				"Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNjIyNzQ3NDQ4LCJpYXQiOjE2MjI3MTg2NDh9.W5ZGbvT4pSr7lZBVuUBNhhuBSH0GC0LExwkvI29RU8rCOMPIjnOqWOO4wG56fzwy2McYnq7E0FkWdh-4sh0TVg");

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}
	
	@DisplayName("Should process request with expired token and refresh token")
	@Transactional
	@Test
	void requestWithExpiredTokenAndRefreshTokenTest() {
		
		String refreshToken = refreshTokenUtil.generateToken("1");
		
		when(request.getRequestURI()).thenReturn("Refresh");

		when(request.getHeader("Authorization")).thenReturn(
				"Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNjIyNzQ3NDQ4LCJpYXQiOjE2MjI3MTg2NDh9.W5ZGbvT4pSr7lZBVuUBNhhuBSH0GC0LExwkvI29RU8rCOMPIjnOqWOO4wG56fzwy2McYnq7E0FkWdh-4sh0TVg");

		when(request.getHeader("Refresh")).thenReturn(refreshToken);
		
		
		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}
	
	@DisplayName("Should throw excpetion if incorrect player in token")
	@Transactional
	@Test
	void requestWithTokenWithIncorrectUserTest() {

		String jwtToken = jwtTokenUtil.generateToken("11");

		when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
		

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
		
		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
		
	}
	
	@DisplayName("Should process request with JWT token in parameter for non admin player")
	@Transactional
	@Test
	void requestWithTokenInParameterForNonAdminTest(@Autowired PlayerRepository playerRepository) {

		Player regularPlayer = new Player();
		regularPlayer.setNick("RegularPlayer");
		regularPlayer.setPassword("welcome");
		regularPlayer.setRole(Common.ROLE_PLAYER_REGULAR);
		regularPlayer.setWhs(10f);
		regularPlayer.setSex(Common.PLAYER_SEX_MALE);
		playerRepository.save(regularPlayer);
		
		String jwtToken = jwtTokenUtil.generateToken(String.valueOf(regularPlayer.getId()));

		when(request.getHeader("Authorization")).thenReturn(null);
		when(request.getParameter("token")).thenReturn(jwtToken);

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}

	@AfterAll
	public static void done() {

		// favouriteCourseRepository.deleteAll();

		log.info("Clean up completed");

	}

}
