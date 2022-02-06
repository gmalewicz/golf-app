package com.greg.golf.security.oauth;

import com.greg.golf.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class GolfAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    private PlayerService playerService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.debug("logged error is " + exception.getMessage());
        log.debug("logged error is " + exception.getCause());

        response.sendRedirect("http://localhost:4200/login");
    }
}
