package com.greg.golf.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.greg.golf.entity.Player;
import com.greg.golf.service.PlayerService;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
	
	private UserDetails userDetails = null;

	private final PlayerService playerService;
	private final JwtTokenUtil jwtTokenUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		
		String userId = null;
		String jwtToken = null;
		String requestTokenHeader = request.getHeader("Authorization");
		
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
			} catch (IllegalArgumentException e) {
				log.error("Unable to get JWT Token");
			} catch (ExpiredJwtException e) {
				log.error("JWT Token has expired");
			}
		}

		// Once we get the token validate it.
		if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			logger.debug("Start processing the token for user: " + userId);
			Optional<Player> player = this.playerService.getPlayer(Long.valueOf(userId));

			// if token is valid configure Spring Security to manually set
			// authentication
			player.ifPresent(p -> userDetails = new User(p.getId().toString(), p.getPassword(), new ArrayList<>()));

			if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				// After setting the Authentication in the context, we specify
				// that the current user is authenticated. So it passes the
				// Spring Security Configurations successfully.
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}
		}

		chain.doFilter(request, response);

	}
}
