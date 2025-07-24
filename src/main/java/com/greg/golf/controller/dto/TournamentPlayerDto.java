package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import lombok.ToString;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.*;

@Getter
@Setter
public class TournamentPlayerDto {

	@Schema(description = "TournamentPlayer object identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Player identifier", example = "25", accessMode = READ_WRITE)
	private Long playerId;

	@NotNull
	@Schema(description = "Tournament identifier", example = "25", accessMode = READ_WRITE)
	private Long tournamentId;

	@Min(value = -5)
	@Max(value = 54)
	@Schema(description = "Player handicap", example = "38.5", accessMode = READ_ONLY, minimum = "-5", maximum = "54")
	private Float whs;

	@Size(min = 1, max = 20, message = "Nick must be between 1 and 20 characters")
	@Schema(description = "Player nick name", example = "golfer", accessMode = READ_ONLY, maxLength=20)
	private String nick;

	@ToString.Exclude
	@Schema(description = "Player sex: false - male, true - female", example = "false", accessMode = READ_ONLY)
	private Boolean sex;
}
