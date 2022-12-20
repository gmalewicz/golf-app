package com.greg.golf.captcha;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.greg.golf.error.ReCaptchaInvalidException;
import com.greg.golf.error.ReCaptchaUnavailableException;

@Slf4j
@Service("captchaService")
public class CaptchaService extends AbstractCaptchaService {

	public CaptchaService(HttpServletRequest request, CaptchaSettings captchaSettings,
						  ReCaptchaAttemptService reCaptchaAttemptService, @Lazy RestOperations restTemplate) {
		super(request, captchaSettings, reCaptchaAttemptService, restTemplate);
	}

	@Override
	public void processResponse(final String response) {
		securityCheck(response);

		final var verifyUri = URI
				.create(String.format(RECAPTCHA_URL_TEMPLATE, getReCaptchaSecret(), response, getClientIP()));
		try {
			final var googleResponse = restTemplate.getForObject(verifyUri, GoogleResponse.class);

			if (googleResponse == null) {
				throw new RestClientException("Google response not available");
			}

			if (!googleResponse.isSuccess()) {
				log.warn("Google's response failed");
				if (googleResponse.hasClientError()) {
					reCaptchaAttemptService.reCaptchaFailed(getClientIP());
				}
				throw new ReCaptchaInvalidException("reCaptcha was not successfully validated");
			}
			log.debug("Google's response: {} ", googleResponse);
		} catch (RestClientException rce) {
			throw new ReCaptchaUnavailableException("Registration unavailable at this time.  Please try again later.",
					rce);
		}
		reCaptchaAttemptService.reCaptchaSucceeded(getClientIP());
	}
}
