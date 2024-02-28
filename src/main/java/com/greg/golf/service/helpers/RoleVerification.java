package com.greg.golf.service.helpers;

import com.greg.golf.error.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class RoleVerification {

    private RoleVerification() {
        throw new IllegalStateException("Utility class");
    }

    public static void verifyPlayer(Long playerId, String errorTxt) throws UnauthorizedException {

        log.debug(playerId.toString());
        log.debug(SecurityContextHolder.getContext().getAuthentication().getName());
        log.debug(Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName()).toString());

        if (!Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName()).equals(playerId)) {

            log.error(errorTxt);
            throw new UnauthorizedException();
        }
    }
}
