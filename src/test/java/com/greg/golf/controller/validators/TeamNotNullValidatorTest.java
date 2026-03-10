package com.greg.golf.controller.validators;

import com.greg.golf.controller.dto.OnlineRoundDto;
import com.greg.golf.entity.helpers.Common;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TeamNotNullValidatorTest {

    private TeamNotNullValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new TeamNotNullValidator();
        context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(builder);
        when(builder.addPropertyNode(anyString()))
                .thenReturn(
                        mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
                );
    }

    @Test
    void shouldReturnTrueWhenDtoIsNull() {
        boolean result = validator.isValid(null, context);

        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void shouldReturnTrueWhenFormatIsNotFourBallAndTeamIsNull() {
        OnlineRoundDto dto = mock(OnlineRoundDto.class);
        when(dto.getFormat()).thenReturn(999); // non-FourBall format
        when(dto.getTeam()).thenReturn(null);

        boolean result = validator.isValid(dto, context);

        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void shouldReturnFalseWhenFormatIsFourBallMatchPlayAndTeamIsNull() {
        OnlineRoundDto dto = mock(OnlineRoundDto.class);
        when(dto.getFormat()).thenReturn(Common.FOUR_BALL_MATCH_PLAY_FORMAT);
        when(dto.getTeam()).thenReturn(null);

        boolean result = validator.isValid(dto, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(
                "Team must not be null when format is FourBall MP or FourBall SP"
        );
    }

    @Test
    void shouldReturnFalseWhenFormatIsFourBallStrokePlayAndTeamIsNull() {
        OnlineRoundDto dto = mock(OnlineRoundDto.class);
        when(dto.getFormat()).thenReturn(Common.FOUR_BALL_STROKE_PLAY_FORMAT);
        when(dto.getTeam()).thenReturn(null);

        boolean result = validator.isValid(dto, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(
                "Team must not be null when format is FourBall MP or FourBall SP"
        );
    }

    @Test
    void shouldReturnTrueWhenFormatIsFourBallAndTeamIsNotNull() {
        OnlineRoundDto dto = mock(OnlineRoundDto.class);
        when(dto.getFormat()).thenReturn(Common.FOUR_BALL_MATCH_PLAY_FORMAT);
        when(dto.getTeam()).thenReturn(1);

        boolean result = validator.isValid(dto, context);

        assertTrue(result);
        verifyNoInteractions(context);
    }
}
