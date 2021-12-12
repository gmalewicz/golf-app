package com.greg.golf.service.helpers;

import com.greg.golf.error.UnauthorizedException;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;

@Log4j2
public class RoleVerification {

    private RoleVerification() {
        throw new IllegalStateException("Utility class");
    }

    public static void verifyRole(String role, String errorTxt) throws UnauthorizedException {

        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority()
                .equals(role)) {

            log.error(errorTxt);
            throw new UnauthorizedException();
        }
    }
}
