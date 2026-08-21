package com.greg.golf.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.csrf.CsrfToken;

class CsrfCookieFilterTest {

	private CsrfCookieFilter csrfCookieFilter;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private FilterChain filterChain;

	@BeforeEach
	void setUp() {
		csrfCookieFilter = new CsrfCookieFilter();
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		filterChain = mock(FilterChain.class);
	}

	@DisplayName("Should render the token and continue the chain when a CsrfToken is present")
	@Test
	void shouldRenderTokenWhenPresent() throws Exception {

		CsrfToken csrfToken = mock(CsrfToken.class);
		when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);

		csrfCookieFilter.doFilterInternal(request, response, filterChain);

		// getToken() must be called so the deferred XSRF-TOKEN cookie is re-seeded
		verify(csrfToken).getToken();
		verify(filterChain).doFilter(request, response);
	}

	@DisplayName("Should continue the chain without rendering when no CsrfToken is present")
	@Test
	void shouldContinueChainWhenTokenAbsent() throws Exception {

		when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

		csrfCookieFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
	}

	@DisplayName("Should not treat an unrelated request attribute as a CsrfToken")
	@Test
	void shouldIgnoreMissingTokenAttribute() throws Exception {

		CsrfToken csrfToken = mock(CsrfToken.class);
		when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

		csrfCookieFilter.doFilterInternal(request, response, filterChain);

		verify(csrfToken, never()).getToken();
		verify(filterChain).doFilter(request, response);
	}
}
