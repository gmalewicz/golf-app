package com.greg.golf.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Getter
@Setter
public class CycleResultDto {

	@NotNull
	@JsonProperty( value = "name", access = JsonProperty.Access.READ_ONLY)
	@Schema(description = "Player name", accessMode = READ_ONLY)
	private String playerName;

	@NotNull
	@Schema(description = "Total score", example = "1", accessMode = READ_ONLY)
	private Integer total;

	@NotNull
	@JsonProperty( value = "cycleResult", access = JsonProperty.Access.READ_ONLY)
	@Schema(description = "Player cycle result", example = "1", accessMode = READ_ONLY)
	private Integer cycleScore;

	@NotNull
	@Schema(description = "Round results", accessMode = READ_ONLY)
	@JsonProperty( value = "r", access = JsonProperty.Access.READ_ONLY)
	private int[] results;
}
