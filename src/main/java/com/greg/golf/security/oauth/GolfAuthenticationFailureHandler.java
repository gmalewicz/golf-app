package com.greg.golf.security.oauth;

import com.greg.golf.configurationproperties.Oauth2Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class GolfAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    private Oauth2Config oauth2Config;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.error("logged error is " + exception.getMessage());
        log.error("logged error is " + exception.getCause());

        response.sendRedirect(oauth2Config.getRedirect() + "?error=authFailed");
    }
}
