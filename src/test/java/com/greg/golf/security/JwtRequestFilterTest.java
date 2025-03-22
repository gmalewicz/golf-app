package com.greg.golf.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.greg.golf.repository.PlayerRepository;
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

	@SuppressWarnings("unused")
	@Autowired
	private PlayerService playerService;

	@SuppressWarnings("unused")
	@Autowired
	JwtConfig jwtConfig;

	private static JwtTokenUtil jwtTokenUtil;
	private static RefreshTokenUtil refreshTokenUtil;
	private static HttpServletRequest request;
	private static HttpServletResponse response;
	private static FilterChain filterChain;

	@BeforeAll
	static void setup(@Autowired JwtConfig jwtConfig) {

		jwtTokenUtil = new JwtTokenUtil(jwtConfig);
		refreshTokenUtil = new RefreshTokenUtil(jwtConfig);
		response = mock(HttpServletResponse.class);

		filterChain = mock(FilterChain.class);

		log.info("Set up completed");
	}

	@BeforeEach
	void setupBeforeEach() {

		request = mock(HttpServletRequest.class);
		SecurityContextHolder.getContext().setAuthentication(null);
		log.debug("Set up before each completed");
	}

	@DisplayName("Should bypass filter in case of not secured URL")
	@Transactional
	@Test
	void requestForNotSecuredURL() {

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

	@DisplayName("Should process request with expired access cookie and valid refresh cookie but refresh does not exists in db")
	@Transactional
	@Test
	void requestWithExpiredAccessValidRefreshNotInDbTest() {

		String refreshToken = refreshTokenUtil.generateToken("1");

		Cookie access = new Cookie("accessToken", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzE2Mjg2NDY2LCJleHAiOjE3MTYyODY0OTZ9.YkiszSNS94jXZaKIIC7Y_k1r-S7AxZTXgTgpIp-VyerIijAoIMkR360G_l-TSHHsJlJIEadWE-bdSPvqg7K3iA");
		Cookie refresh = new Cookie("refreshToken", refreshToken);
		when(request.getCookies()).thenReturn(new Cookie[]{access, refresh});
		when(request.getRequestURI()).thenReturn("/Refresh");

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}

	@DisplayName("Should process request with expired access cookie and valid refresh cookie existing in db")
	@Transactional
	@Test
	void requestWithExpiredAccessValidRefreshTest(@Autowired PlayerRepository playerRepository) {

		String refreshToken = refreshTokenUtil.generateToken("1");
		var player = playerRepository.findById(1L).orElseThrow();
		player.setRefresh(refreshToken);
		player.setModified(true);
		playerRepository.save(player);

		Cookie access = new Cookie("accessToken", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzE2Mjg2NDY2LCJleHAiOjE3MTYyODY0OTZ9.YkiszSNS94jXZaKIIC7Y_k1r-S7AxZTXgTgpIp-VyerIijAoIMkR360G_l-TSHHsJlJIEadWE-bdSPvqg7K3iA");
		Cookie refresh = new Cookie("refreshToken", refreshToken);
		when(request.getCookies()).thenReturn(new Cookie[]{access, refresh});
		when(request.getRequestURI()).thenReturn("/Refresh");

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		try {

			jwtRequestFilter.doFilter(request, response, filterChain);
		} catch (Exception e) {
			Assertions.fail("Should not have thrown any exception");
		}
	}
}
