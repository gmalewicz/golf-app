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
				jwtToken = processRefreshRequest(request, e, refreshToken);
				if (jwtToken != null) {
					userId = e.getClaims().getSubject();
				}
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

	private String processRefreshRequest(HttpServletRequest request, ExpiredJwtException e, String refreshToken) {

		String jwtToken = null;

		// verify if it is refresh request
		if (request.getRequestURI().contains(REFRESH)) {

			log.info("Start generating renewed token");

			try {

				Player player = playerService.getPlayer(Long.valueOf(e.getClaims().getSubject())).orElseThrow();
				playerService.cacheEvict(player);
				player = playerService.getPlayer(Long.valueOf(e.getClaims().getSubject())).orElseThrow();

				UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<>());

				// verify if token exists in database
				// each time the token is used, it is replaced in database
				if (player.getRefresh() == null || !player.getRefresh().equals(refreshToken)) {
					log.error("Attempt to use refresh token the second time - player id: " + player.getId());
					throw new IllegalArgumentException("Attempt to use refresh token the second time");
				}

				// if positive generate the new JWT token and replace it in the request
				if (refreshTokenUtil.validateToken(refreshToken, userDetails)) {

					jwtToken = jwtTokenUtil.generateToken(userDetails.getUsername());
					request.setAttribute(REFRESH_TOKEN, jwtToken);
					// store the verified player id so the controller can assert the path variable matches
					request.setAttribute(VERIFIED_USER_ID, player.getId());
				}
			} catch (Exception ex) {
				log.info("Refresh token expired or not available: " + ex.getClass());
			}
		}

		return jwtToken;
	}

	private boolean unsecuredUrls(HttpServletRequest request) {
        return request.getRequestURI().contains("/rest/Authenticate") ||
                (request.getRequestURI().contains("/rest/AddPlayer") && !request.getRequestURI().contains("/rest/AddPlayerOnBehalf")) ||
                request.getRequestURI().contains("/actuator/") ||
                request.getRequestURI().contains("/api") ||
                request.getRequestURI().contains("/oauth2/");
    }
}
