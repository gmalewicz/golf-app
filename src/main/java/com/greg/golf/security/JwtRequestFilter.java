package com.greg.golf.security;

import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.service.PlayerService;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	private final PlayerService playerService;

	private final JwtTokenUtil jwtTokenUtil;
	private final RefreshTokenUtil refreshTokenUtil;

	private static final String REFRESH = "Refresh";
	private static final String REFRESH_TOKEN = "refreshToken";
	private static final String ACCESS_TOKEN = "accessToken";
	public static final String VERIFIED_USER_ID = "verifiedUserId";

	private static final String HCP_HEADER = "hcp";
	private static final String SEX_HEADER = "sex";

	// RFC 6750 — standard Bearer error codes delivered via WWW-Authenticate header
	private static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
	// access token expired but a refresh token is present — client should call /rest/Refresh
	static final String TOKEN_EXPIRED_VALUE = "Bearer error=\"token_expired\"";
	// no usable token at all / refresh also expired — client must re-authenticate
	static final String INVALID_TOKEN_VALUE = "Bearer error=\"invalid_token\"";

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
			throws ServletException, IOException {

		// skip unsecured urls from processing
		if (unsecuredUrls(request)) {
			chain.doFilter(request, response);
			return;
		}

		log.debug("Processing started");

		String userId;
		String jwtToken = null;
		String refreshToken = null;

		if(request.getCookies() != null){
			for(Cookie cookie: request.getCookies()){
				if(cookie.getName().equals(ACCESS_TOKEN)){
					jwtToken = cookie.getValue();
				}
				if(cookie.getName().equals(REFRESH_TOKEN)){
					refreshToken = cookie.getValue();
				}
			}
		}

		log.debug("token from cookie: " + jwtToken);
		log.debug("refresh token from cookie: " + refreshToken);

		userId = getUserId(jwtToken, refreshToken, response, request);

		// Once we get the token validate it.
		validateToken(userId, request, response);

		chain.doFilter(request, response);

	}

	private String getUserId(String jwtToken, String refreshToken, HttpServletResponse response, HttpServletRequest request) {

		// A refresh request is authorized purely on the strength of the refresh token cookie.
		// It must NOT depend on the access token: the access token JWT and its cookie share the
		// same lifetime, so by the time renewal is needed the access token is usually already gone.
		if (request.getRequestURI().contains(REFRESH)) {
			return handleRefreshRequest(refreshToken, response, request);
		}

		String userId = null;

		if (jwtToken != null || refreshToken != null) {

			if (jwtToken == null) {
				// Only a refresh cookie is present — no access token to validate.
				// Treat as expired so the client is directed to call /rest/Refresh.
				log.info("No access token cookie present; only refresh token available");
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setHeader(WWW_AUTHENTICATE_HEADER, TOKEN_EXPIRED_VALUE);
				return null;
			}

			try {
				userId = jwtTokenUtil.getUserIdFromToken(jwtToken);

			} catch (ExpiredJwtException e) {
				log.info("JWT Token has expired for player: " + e.getClaims().getSubject());
				// Signal the client to call /rest/Refresh using the standard 401 + WWW-Authenticate
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setHeader(WWW_AUTHENTICATE_HEADER, TOKEN_EXPIRED_VALUE);
			} catch (Exception e) {
				log.error("Unable to get JWT Token");
			}
		} else {
			// No cookies at all — session is dead, client must log in again
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setHeader(WWW_AUTHENTICATE_HEADER, INVALID_TOKEN_VALUE);
		}
		return userId;
	}

	private void validateToken(String userId, HttpServletRequest request, HttpServletResponse response) {

		if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			log.debug("Start processing the token for user: " + userId);

			try {

				Player player = this.playerService.getPlayer(Long.valueOf(userId)).orElseThrow();

				// do not authorize player who has been modified by admin
				// modified flag will be removed after sign on
				if (Boolean.TRUE.equals(player.getModified())) {

					// clear cache for the player that has been modified
					this.playerService.cacheEvict(player);
					log.debug("Modifications detected for player " + player.getNick());
				}

				// if token is valid configure Spring Security to manually set
				// authentication
				var authorities = new ArrayList<SimpleGrantedAuthority>();
				if (player.getRole() == Common.ROLE_PLAYER_ADMIN) {
					authorities.add(new SimpleGrantedAuthority(Common.ADMIN));
				} else {
					authorities.add(new SimpleGrantedAuthority(Common.PLAYER));
				}

				UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), authorities);

				//add whs to the response header to be used by frontend to updated player data
				response.addHeader(HCP_HEADER, player.getWhs().toString());
				response.addHeader(SEX_HEADER, player.getSex().toString());

				var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
						userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				// After setting the Authentication in the context, we specify
				// that the current user is authenticated. So it passes the
				// Spring Security Configurations successfully.
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			} catch (Exception e) {
				log.info("Error authentication for user: " + userId);
			}

		}
	}

	// Authorizes a /rest/Refresh request based solely on the refresh token cookie.
	// Returns the verified player id (so the caller can establish the security context)
	// or null after setting the appropriate 401 response when the refresh cannot proceed.
	private String handleRefreshRequest(String refreshToken, HttpServletResponse response, HttpServletRequest request) {

		if (refreshToken == null) {
			// No refresh cookie — the session is dead, the client must log in again.
			log.info("Refresh requested but no refresh token cookie present");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setHeader(WWW_AUTHENTICATE_HEADER, INVALID_TOKEN_VALUE);
			return null;
		}

		log.info("Start generating renewed token");

		try {

			String userId = refreshTokenUtil.getUserIdFromToken(refreshToken);

			Player player = playerService.getPlayer(Long.valueOf(userId)).orElseThrow();
			playerService.cacheEvict(player);
			player = playerService.getPlayer(Long.valueOf(userId)).orElseThrow();

			UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<>());

			// verify the presented refresh token is the one currently stored for the player;
			// each use rotates it in the database, so an old token indicates a replay attempt
			if (player.getRefresh() == null || !player.getRefresh().equals(refreshToken)) {
				log.error("Attempt to reuse an old refresh token - player id: " + player.getId());
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setHeader(WWW_AUTHENTICATE_HEADER, INVALID_TOKEN_VALUE);
				return null;
			}

			if (refreshTokenUtil.validateToken(refreshToken, userDetails)) {
				// store the verified player id so the controller can assert the path variable matches
				request.setAttribute(VERIFIED_USER_ID, player.getId());
				return userId;
			}

			// refresh token itself is expired — force re-authentication
			log.info("Refresh token expired for player id: " + player.getId());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setHeader(WWW_AUTHENTICATE_HEADER, INVALID_TOKEN_VALUE);
			return null;

		} catch (ExpiredJwtException e) {
			log.info("Refresh token expired");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setHeader(WWW_AUTHENTICATE_HEADER, INVALID_TOKEN_VALUE);
			return null;
		} catch (Exception e) {
			log.info("Unable to process refresh request: " + e.getClass());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setHeader(WWW_AUTHENTICATE_HEADER, INVALID_TOKEN_VALUE);
			return null;
		}
	}

	private boolean unsecuredUrls(HttpServletRequest request) {
        return request.getRequestURI().contains("/rest/Authenticate") ||
                (request.getRequestURI().contains("/rest/AddPlayer") && !request.getRequestURI().contains("/rest/AddPlayerOnBehalf")) ||
                request.getRequestURI().contains("/actuator/") ||
                request.getRequestURI().contains("/api") ||
                request.getRequestURI().contains("/oauth2/");
    }
}
