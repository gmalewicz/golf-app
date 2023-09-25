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
public class LeagueMatchDto {

	@Schema(description = "LeagueMatch object identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "League identifier", example = "25", accessMode = READ_WRITE)
	private Long leagueId;

	@NotNull
	@Schema(description = "Winner identifier", example = "25", accessMode = READ_WRITE)
	private Long winnerId;

	@NotNull
	@Schema(description = "Looser identifier", example = "25", accessMode = READ_WRITE)
	private Long looserId;

	@Size(min = 3, max = 4, message = "Result must be between 3 and 4 characters")
	@Schema(description = "Match result", example = "A/S", accessMode = READ_WRITE, maxLength=4)
	private String result;
}
