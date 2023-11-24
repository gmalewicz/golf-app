package com.greg.golf.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.greg.golf.configurationproperties.JwtConfig;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.service.helpers.GolfUserDetails;

import io.jsonwebtoken.Claims;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public abstract class TokenUtil {
	
	protected static final String ROLES = "roles";

	protected final JwtConfig jwtConfig;

	// retrieve user id from jwt token
	public String getUserIdFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	// retrieve expiration date from jwt token
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final var claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	// for retrieving any information from token we will need the secret key
	protected abstract Claims getAllClaimsFromToken(String token);

	// check if the token has expired
	private Boolean isTokenExpired(String token) {
		final var expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	// generate token for user
	public String generateToken(GolfUserDetails userDetails) {
		
		String authorities = "";
		
		if (userDetails.getPlayer().getRole() == Common.ROLE_PLAYER_ADMIN) {
			authorities += Common.ADMIN + ",";
		} 
		authorities += Common.PLAYER;
				
		Map<String, Object> claims = new HashMap<>();
		claims.put(ROLES, authorities);
		return doGenerateToken(claims, userDetails.getPlayer().getId().toString());
	}

	// generate token for user
	public String generateToken(String playerId) {
		Map<String, Object> claims = new HashMap<>();
		return doGenerateToken(claims, playerId);
	}

	// while creating the token -
	// 1. Define claims of the token, like Issuer, Expiration, Subject, and the ID
	// 2. Sign the JWT using the HS512 algorithm and secret key.
	// 3. According to JWS Compact
	// Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	// compaction of the JWT to a URL-safe string
	protected abstract String doGenerateToken(Map<String, Object> claims, String subject);

	// validate token
	public boolean validateToken(String token, UserDetails userDetails) {
		final String userId = getUserIdFromToken(token);
		return (userId.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	protected Key getSigningKey(String secret) {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
