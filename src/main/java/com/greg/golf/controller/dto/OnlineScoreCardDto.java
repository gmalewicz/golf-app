package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OnlineScoreCardDto {


	@ToString.Exclude
	@Schema(description = "OnlineSCoreCard identifier", example = "25", accessMode = READ_ONLY)
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

	@ToString.Exclude
	@Schema(description = "Time", example = "10:59", accessMode = READ_WRITE, minimum = "00:00", maximum = "23:59")
	@Pattern(regexp = "^(0\\d|1\\d|2[0-3]):[0-5]\\d$")
	private String time;
}
