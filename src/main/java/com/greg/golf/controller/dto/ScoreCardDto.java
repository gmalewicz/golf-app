package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

import javax.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreCardDto {
	
	@Schema(description = "Scorecard identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Hole number", example = "14", accessMode = READ_WRITE, minimum = "1", maximum = "18")
	private Integer hole;

	@NotNull
	@Schema(description = "Number of strokes (including putts)", example = "6", accessMode = READ_WRITE, minimum = "1", maximum = "16")
	private Integer stroke;

	@NotNull
	@Schema(description = "Number of putts", example = "7", accessMode = READ_WRITE, minimum = "1", maximum = "10")
	private Integer pats;
	
	@Schema(description = "Number of penalties", example = "3", accessMode = READ_WRITE, minimum = "1", maximum = "15")
	private Integer penalty;
	
	@Schema(description = "Player for scorecard", accessMode = READ_WRITE)
	private PlayerDto player;

}
