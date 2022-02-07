package com.greg.golf.security.oauth;

import com.greg.golf.configurationproperties.CourseServiceConfig;
import com.greg.golf.configurationproperties.Oauth2Config;
import com.greg.golf.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class GolfAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private PlayerService playerService;

    @Autowired
    private Oauth2Config oauth2Config;

    @Autowired
    public GolfAuthenticationSuccessHandler(@Lazy PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        GolfOAuth2User oauthUser = (GolfOAuth2User) authentication.getPrincipal();

        log.debug("Player name: " + oauthUser.getName());
        log.debug("Player first name: " + oauthUser.getFirstName());
        log.debug("Player last name: " + oauthUser.getLastName());

        String queryParams = playerService.processOAuthPostLogin(oauthUser.getFirstName(), oauthUser.getLastName());

        response.sendRedirect(oauth2Config.getRedirect() + queryParams);
    }
}