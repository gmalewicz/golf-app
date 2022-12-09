package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

@Getter
@Setter
public class RoundWhsDto {

	@NotNull
	@Schema(description = "Player identifier", example = "25", accessMode = WRITE_ONLY)
	private Long playerId;

	@NotNull
	@Schema(description = "Round identifier", example = "25", accessMode = WRITE_ONLY)
	private Long roundId;

	@NotNull
	@Schema(description = "New handicap", example = "38.5", accessMode = WRITE_ONLY, minimum="-5", maximum="54")
	private Float whs;
}
