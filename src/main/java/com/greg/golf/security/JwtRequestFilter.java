package com.greg.golf.security;

import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private PlayerService playerService;

	private final JwtTokenUtil jwtTokenUtil;
	private final RefreshTokenUtil refreshTokenUtil;

	private static final String REFRESH = "Refresh";
	private static final String REFRESH_TOKEN = "refreshToken";

	@Override
	protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
			throws ServletException, IOException {

		String userId = null;
		String jwtToken = null;
		String requestTokenHeader = request.getHeader("Authorization");
		String refreshToken = request.getHeader(REFRESH);

		if (requestTokenHeader == null) {
			jwtToken = request.getParameter("token");
		}
		log.debug("token: " + requestTokenHeader);

		// JWT Token is in the form "Bearer token". Remove Bearer word and get
		// only the Token
		if (jwtToken == null && requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
			jwtToken = requestTokenHeader.substring(7);
		}

		if (jwtToken != null) {

			try {
				userId = jwtTokenUtil.getUserIdFromToken(jwtToken);

			} catch (ExpiredJwtException e) {
				log.info("JWT Token has expired for player: " + e.getClaims().getSubject());
				jwtToken = processRefreshRequest(request, e, refreshToken);
				if (jwtToken != null) {
					userId = e.getClaims().getSubject();
				}
			} catch (Exception e) {
				log.error("Unable to get JWT Token");
			}
		}

		// Once we get the token validate it.
		validateToken(userId, request);

		chain.doFilter(request, response);

	}

	private void validateToken(String userId, HttpServletRequest request) {

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
					return;
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

				UserDetails userDetails = new User(player.getId().toString(), player.getPassword(), new ArrayList<>());

				// if positive generate the new JWT token and replace it in the request

				if (refreshTokenUtil.validateToken(refreshToken, userDetails)) {

					jwtToken = jwtTokenUtil.generateToken(userDetails.getUsername());
					request.setAttribute(REFRESH_TOKEN, jwtToken);
				}
			} catch (Exception ex) {
				log.info("Refresh token expired or not available: " + ex.getClass());
			}
		}

		return jwtToken;
	}
}
