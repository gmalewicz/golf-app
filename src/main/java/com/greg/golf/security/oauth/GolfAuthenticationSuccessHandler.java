package com.greg.golf.security.oauth;

import com.greg.golf.configurationproperties.Oauth2Config;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.service.PlayerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public class GolfAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final PlayerService playerService;

    private static final String FACEBOOK = "facebook";
    private static final String GOOGLE = "google";


    private final Oauth2Config oauth2Config;

    @Autowired
    public GolfAuthenticationSuccessHandler(@Lazy PlayerService playerService, Oauth2Config oauth2Config) {

        this.playerService = playerService;
        this.oauth2Config = oauth2Config;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        int playerType = Common.TYPE_PLAYER_LOCAL;

        if (((OAuth2AuthenticationToken)authentication).getAuthorizedClientRegistrationId().equals(FACEBOOK)) {
            playerType = Common.TYPE_PLAYER_FACEBOOK;
        } else if (((OAuth2AuthenticationToken)authentication).getAuthorizedClientRegistrationId().equals(GOOGLE)) {
            playerType = Common.TYPE_PLAYER_GOOGLE;
        }

        GolfOAuth2User oauthUser = (GolfOAuth2User) authentication.getPrincipal();

        log.debug("Player name: " + oauthUser.getName());
        log.debug("Player first name: " + oauthUser.getFirstName());
        log.debug("Player last name: " + oauthUser.getLastName());
        log.debug("Social type: " + playerType);

        String queryParams = playerService.processOAuthPostLogin(oauthUser.getFirstName(), oauthUser.getLastName(), playerType);

        response.sendRedirect(oauth2Config.getRedirect() + Objects.requireNonNullElse(queryParams, "?error=playerType"));
    }
}
