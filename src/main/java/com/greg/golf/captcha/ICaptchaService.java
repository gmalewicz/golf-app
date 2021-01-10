package com.greg.golf.captcha;

import com.greg.golf.error.ReCaptchaInvalidException;

public interface ICaptchaService {

	default void processResponse(final String response) throws ReCaptchaInvalidException {
	}

	default void processResponse(final String response, String action) throws ReCaptchaInvalidException {
	}

	String getReCaptchaSite();

	String getReCaptchaSecret();

}
