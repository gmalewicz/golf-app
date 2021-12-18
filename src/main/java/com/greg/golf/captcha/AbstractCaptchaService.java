package com.greg.golf.captcha;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.greg.golf.error.ReCaptchaInvalidException;

@Slf4j
public abstract class AbstractCaptchaService implements ICaptchaService{
	
	protected static final Pattern RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");    
    protected static final String RECAPTCHA_URL_TEMPLATE = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s";
    
    protected final HttpServletRequest request;
    protected final CaptchaSettings captchaSettings;
    protected final ReCaptchaAttemptService reCaptchaAttemptService;
    protected final RestOperations restTemplate;

    protected AbstractCaptchaService(HttpServletRequest request, CaptchaSettings captchaSettings,
    		ReCaptchaAttemptService reCaptchaAttemptService, RestOperations restTemplate) {
		super();
		this.request = request;
		this.captchaSettings = captchaSettings;
		this.reCaptchaAttemptService = reCaptchaAttemptService;
		this.restTemplate = restTemplate;
	}
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
       // Do any additional configuration here
       return builder.build();
    }
    
    @Override
    public String getReCaptchaSite() {
        return captchaSettings.getSite();
    }

    @Override
    public String getReCaptchaSecret() {
        return captchaSettings.getSecret();
    }
  

    protected void securityCheck(final String response) {
        log.debug("Attempting to validate response {}", response);

        if (reCaptchaAttemptService.isBlocked(getClientIP())) {
            throw new ReCaptchaInvalidException("Client exceeded maximum number of failed attempts");
        }

        if (!responseSanityCheck(response)) {
            throw new ReCaptchaInvalidException("Response contains invalid characters");
        }
    }

    protected boolean responseSanityCheck(final String response) {
        return StringUtils.hasLength(response) && RESPONSE_PATTERN.matcher(response).matches();
    }

    protected String getClientIP() {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
