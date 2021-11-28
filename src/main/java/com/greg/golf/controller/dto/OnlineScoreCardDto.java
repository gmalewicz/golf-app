package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnlineScoreCardDto {

	@Schema(description = "Course identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Hole number", example = "14", accessMode = READ_WRITE, minimum = "1", maximum = "18")
	private Integer hole;

	@NotNull
	@Schema(description = "Number of strokes (including putts)", example = "6", accessMode = READ_WRITE, minimum = "1", maximum = "16")
	private Integer stroke;

	@Schema(description = "Number of putts", example = "7", accessMode = READ_WRITE, minimum = "1", maximum = "10")
	private Integer putt;

	@Schema(description = "Number of penalties", example = "3", accessMode = READ_WRITE, minimum = "1", maximum = "15")
	private Integer penalty;

	@Schema(description = "Player for scorecard", accessMode = READ_WRITE)
	private PlayerDto player;

	@Schema(description = "Online round id", example = "25", accessMode = READ_ONLY)
	private long orId;

	@Schema(description = "Update flag", example = "true", accessMode = READ_WRITE, allowableValues = { "true",
			"false" })
	private boolean update;

	@Schema(description = "Time", example = "10:59", accessMode = READ_WRITE, minimum = "00:00", maximum = "23:59")
	@Pattern(regexp = "/^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$/")
	private String time;

}
