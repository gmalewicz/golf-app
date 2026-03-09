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

        if ((dto.getFormat() == Common.FOUR_BALL_MATCH_PLAY_FORMAT || dto.getFormat() == Common.FOUR_BALL_STROKE_PLAY_FORMAT) && dto.getTeam() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Team must not be null when format is FourBall MP or FourBall SP")
                    .addPropertyNode("team")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
