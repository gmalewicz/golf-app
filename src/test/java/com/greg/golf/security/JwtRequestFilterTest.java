package com.greg.golf.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import com.greg.golf.configurationproperties.JwtConfig;
import com.greg.golf.service.PlayerService;
import com.greg.golf.util.GolfPostgresqlContainer;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
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

	@DisplayName("Should bypass filter in case of not secured URL")
	@Transactional
	@Test
	void requestDorNotSecuredURL() {

		when(request.getRequestURI()).thenReturn("/rest/Authenticate");
		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}

	@DisplayName("Should process request with JWT token in cookie")
	@Transactional
	@Test
	void requestWithTokenInCookieTest() {

		String jwtToken = jwtTokenUtil.generateToken("1");


		Cookie c = new Cookie("accessToken", jwtToken);
		when(request.getCookies()).thenReturn(new Cookie[]{c});
		when(request.getRequestURI()).thenReturn("/test");

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}
	/*
	@DisplayName("Should process request with invalid cookie")
	@Transactional
	@Test
	void requestWithInvalidHeaderTest() {

		Cookie c = new Cookie("accessToken", "invalid");
		when(request.getCookies()).thenReturn(new Cookie[]{c});
		when(request.getRequestURI()).thenReturn("/test");

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

		Cookie c = new Cookie("accessToken", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNjIyNzQ3NDQ4LCJpYXQiOjE2MjI3MTg2NDh9.W5ZGbvT4pSr7lZBVuUBNhhuBSH0GC0LExwkvI29RU8rCOMPIjnOqWOO4wG56fzwy2McYnq7E0FkWdh-4sh0TVg");
		when(request.getCookies()).thenReturn(new Cookie[]{c});
		when(request.getRequestURI()).thenReturn("/test");

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

	@DisplayName("Should throw exception if incorrect player in token")
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
		regularPlayer.setModified(false);
		regularPlayer.setType(Common.TYPE_PLAYER_LOCAL);
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
*/
	@AfterAll
	public static void done() {

		// favouriteCourseRepository.deleteAll();

		log.info("Clean up completed");

	}

}
