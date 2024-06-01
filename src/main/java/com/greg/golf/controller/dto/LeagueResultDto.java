package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Getter
@Setter
public class LeagueResultDto {

	@Schema(description = "Player nick", example = "Player", accessMode = READ_ONLY, maxLength=20)
	@Size(max = 20, message = "Nick must exists and cannot be longer than 20 characters")
	@NotEmpty
	private String nick;

	@PositiveOrZero
	@Schema(description = "Big points", accessMode = READ_ONLY, minimum = "0")
	private Integer big;

	@PositiveOrZero
	@Schema(description = "Small points", accessMode = READ_ONLY, minimum = "0")
	private Integer small;

	@PositiveOrZero
	@Schema(description = "Matches played", accessMode = READ_ONLY, minimum = "0")
	private Integer matchesPlayed;
}
