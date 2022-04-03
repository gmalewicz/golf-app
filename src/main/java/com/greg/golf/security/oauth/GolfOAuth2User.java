package com.greg.golf.security.oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class GolfOAuth2User implements OAuth2User {

    private OAuth2User oauth2User;

    public GolfOAuth2User(OAuth2User oauth2User) {
        this.oauth2User = oauth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oauth2User.getAttribute("name");
    }

    public String getFirstName() {

        // Facebook
        if (oauth2User.getAttribute("first_name") != null) {

            return oauth2User.getAttribute("first_name");
        }

        // Google
        if (oauth2User.getAttribute("given_name") != null) {

            return oauth2User.getAttribute("given_name");
        }

        return null;
    }

    public String getLastName() {

        // Facebook
        if (oauth2User.getAttribute("last_name") != null) {

            return oauth2User.getAttribute("last_name");
        }

        // Google
        if (oauth2User.getAttribute("family_name") != null) {

            return oauth2User.getAttribute("family_name");
        }

        return null;

    }
}
