package com.greg.golf.controller.validators;

import com.greg.golf.controller.dto.OnlineRoundDto;
import com.greg.golf.entity.helpers.Common;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TeamNotNullValidator implements ConstraintValidator<TeamNotNull, OnlineRoundDto> {

    @Override
    public boolean isValid(OnlineRoundDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        if (requiresTeam(dto.getFormat()) && dto.getTeam() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Team must not be null when format is FourBall MP, FourBall SP or Texas Scramble")
                    .addPropertyNode("team")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private static boolean requiresTeam(Integer format) {
        return format != null && (format == Common.FOUR_BALL_MATCH_PLAY_FORMAT
                || format == Common.FOUR_BALL_STROKE_PLAY_FORMAT
                || format == Common.TEXAS_SCRAMBLE_FORMAT);
    }
}
