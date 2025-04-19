package com.greg.golf.captcha;

import com.greg.golf.error.ReCaptchaInvalidException;

public interface ICaptchaService {

	default void processResponse(final String response) throws ReCaptchaInvalidException {
	}

	@SuppressWarnings("unused")
	default void processResponse(final String response, String action) throws ReCaptchaInvalidException {
	}

	@SuppressWarnings("unused")
	String getReCaptchaSite();

	String getReCaptchaSecret();

}
