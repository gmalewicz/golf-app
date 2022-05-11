package com.greg.golf.service.helpers;

import com.greg.golf.error.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
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

    public static void verifyPlayer(Long playerId, String errorTxt) throws UnauthorizedException {

        log.info(playerId.toString());
        log.info(SecurityContextHolder.getContext().getAuthentication().getName());
        log.info(Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName()).toString());


        if (!Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName()).equals(playerId)) {

            log.error(errorTxt);
            throw new UnauthorizedException();
        }
    }
}
