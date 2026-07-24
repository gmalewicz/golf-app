package com.greg.golf.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.greg.golf.repository.PlayerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Testcontainers
class JwtRequestFilterTest {

	@Container
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

		assertDoesNotThrow(() -> jwtRequestFilter.doFilter(request, response, filterChain));
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

		assertDoesNotThrow(() -> jwtRequestFilter.doFilter(request, response, filterChain));
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

		assertDoesNotThrow(() -> jwtRequestFilter.doFilter(request, response, filterChain));
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

		assertDoesNotThrow(() -> jwtRequestFilter.doFilter(request, response, filterChain));
	}

	@DisplayName("Should renew token on refresh when only the refresh cookie is present (access cookie expired and dropped)")
	@Transactional
	@Test
	void requestRefreshWithOnlyRefreshCookieTest(@Autowired PlayerRepository playerRepository) {

		String refreshToken = refreshTokenUtil.generateToken("1");
		var player = playerRepository.findById(1L).orElseThrow();
		player.setRefresh(refreshToken);
		playerRepository.save(player);

		// No access token cookie at all — it shares the access token lifetime and has been
		// deleted by the browser. Only the longer-lived refresh cookie remains.
		Cookie refresh = new Cookie("refreshToken", refreshToken);
		when(request.getCookies()).thenReturn(new Cookie[]{refresh});
		when(request.getRequestURI()).thenReturn("/rest/Refresh/1");

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		assertDoesNotThrow(() -> jwtRequestFilter.doFilter(request, response, filterChain));

		// the refresh must be authorized: the verified id is exposed to the controller
		// and the security context is populated so the endpoint is reachable
		verify(request).setAttribute(JwtRequestFilter.VERIFIED_USER_ID, 1L);
		Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@DisplayName("Should not authorize refresh when the refresh token is expired")
	@Transactional
	@Test
	void requestRefreshWithExpiredRefreshTokenTest() {

		// build a refresh token signed with the refresh secret but already expired,
		// so getUserIdFromToken throws ExpiredJwtException inside handleRefreshRequest
		SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getRefresh().getBytes(StandardCharsets.UTF_8));
		String expiredRefresh = Jwts.builder().subject("1")
				.issuedAt(new Date(System.currentTimeMillis() - 10_000))
				.expiration(new Date(System.currentTimeMillis() - 5_000))
				.signWith(key).compact();

		Cookie refresh = new Cookie("refreshToken", expiredRefresh);
		when(request.getCookies()).thenReturn(new Cookie[]{refresh});
		when(request.getRequestURI()).thenReturn("/rest/Refresh/1");

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		assertDoesNotThrow(() -> jwtRequestFilter.doFilter(request, response, filterChain));

		// the request must not be authorized and no verified id may be exposed
		verify(request, never()).setAttribute(eq(JwtRequestFilter.VERIFIED_USER_ID), any());
		Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@DisplayName("Should not authorize refresh when the refresh token is malformed")
	@Transactional
	@Test
	void requestRefreshWithMalformedRefreshTokenTest() {

		// a token that cannot be parsed throws a (non-expiry) JwtException,
		// exercising the generic catch block in handleRefreshRequest
		Cookie refresh = new Cookie("refreshToken", "this-is-not-a-valid-jwt");
		when(request.getCookies()).thenReturn(new Cookie[]{refresh});
		when(request.getRequestURI()).thenReturn("/rest/Refresh/1");

		JwtRequestFilter jwtRequestFilter = new JwtRequestFilter(playerService, jwtTokenUtil, refreshTokenUtil);

		assertDoesNotThrow(() -> jwtRequestFilter.doFilter(request, response, filterChain));

		// the request must not be authorized and no verified id may be exposed
		verify(request, never()).setAttribute(eq(JwtRequestFilter.VERIFIED_USER_ID), any());
		Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
	}
}
