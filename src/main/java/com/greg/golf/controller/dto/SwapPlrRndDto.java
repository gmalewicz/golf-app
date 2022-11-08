package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

@Getter
@Setter
public class SwapPlrRndDto {

	@NotNull
	@Schema(description = "New player identifier", example = "25", accessMode = WRITE_ONLY)
	private Long newPlrId;

	@NotNull
	@Schema(description = "Old player identifier", example = "25", accessMode = WRITE_ONLY)
	private Long oldPlrId;

	@NotNull
	@Schema(description = "Round identifier", example = "25", accessMode = WRITE_ONLY)
	private Long roundId;
}
