package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

@Getter
@Setter
public class LeaguePlayerDto {

	@Schema(description = "LeaguePlayer object identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Player identifier", example = "25", accessMode = READ_WRITE)
	private Long playerId;

	@NotNull
	@Schema(description = "League object", accessMode = READ_WRITE)
	private LeagueDto league;

	@Size(min = 1, max = 20, message = "Nick must be between 1 and 20 characters")
	@Schema(description = "Player nick name", example = "golfer", accessMode = READ_ONLY, maxLength=20)
	private String nick;
}
