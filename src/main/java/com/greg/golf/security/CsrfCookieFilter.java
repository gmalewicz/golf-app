package com.greg.golf.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

// Forces the deferred CsrfToken to render on every response so the XSRF-TOKEN cookie is
// always re-seeded; prevents a lost/evicted cookie (e.g. iOS Safari) from causing a 403.
public final class CsrfCookieFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		if (csrfToken != null) {
			csrfToken.getToken();
		}
		filterChain.doFilter(request, response);
	}
}
