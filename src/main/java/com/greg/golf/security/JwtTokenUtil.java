package com.greg.golf.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.greg.golf.entity.helpers.Common;
import org.springframework.stereotype.Component;

import com.greg.golf.configurationproperties.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;

@Component
public class JwtTokenUtil extends TokenUtil implements Serializable {

	public JwtTokenUtil(JwtConfig jwtConfig) {
		super(jwtConfig);
	}

	@Serial
	private static final long serialVersionUID = -2550185165626007488L;

	// while creating the token -
	// 1. Define claims of the token, like Issuer, Expiration, Subject, and the ID
	// 2. Sign the JWT using the HS512 algorithm and secret key.
	// 3. According to JWS Compact
	// Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	// compaction of the JWT to a URL-safe string
	protected String doGenerateToken(Map<String, Object> claims, String subject) {

		return Jwts.builder().claims(claims).subject(subject).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + Common.ACCESS_TOKEN_LIFETIME * 1000))
				.signWith(getSigningKey(jwtConfig.getSecret())).compact();
	}

	// for retrieving any information from token we will need the secret key
	protected Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().verifyWith((SecretKey)getSigningKey(jwtConfig.getSecret())).build().parseSignedClaims(token).getPayload();
	}
}
