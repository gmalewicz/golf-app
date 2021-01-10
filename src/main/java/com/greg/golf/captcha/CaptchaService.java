package com.greg.golf.captcha;

import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.greg.golf.error.ReCaptchaInvalidException;
import com.greg.golf.error.ReCaptchaUnavailableException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("captchaService")
public class CaptchaService extends AbstractCaptchaService {

    @Override
    public void processResponse(final String response) {
        securityCheck(response);

        final URI verifyUri = URI.create(String.format(RECAPTCHA_URL_TEMPLATE, getReCaptchaSecret(), response, getClientIP()));
        try {
            final GoogleResponse googleResponse = restTemplate.getForObject(verifyUri, GoogleResponse.class);
            log.debug("Google's response: {} ", googleResponse.toString());

            if (!googleResponse.isSuccess()) {
                if (googleResponse.hasClientError()) {
                    reCaptchaAttemptService.reCaptchaFailed(getClientIP());
                }
                throw new ReCaptchaInvalidException("reCaptcha was not successfully validated");
            }
        } catch (RestClientException rce) {
            throw new ReCaptchaUnavailableException("Registration unavailable at this time.  Please try again later.", rce);
        }
        reCaptchaAttemptService.reCaptchaSucceeded(getClientIP());
    }
}
