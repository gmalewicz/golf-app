package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TournamentResultDto {

	@Schema(description = "Result identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Player for scorecard", accessMode = READ_ONLY)
	private PlayerDto player;

	@NotNull
	@Schema(description = "Tournament gross strokes", accessMode = READ_ONLY, minimum = "0")
	private Integer strokesBrutto;

	@NotNull
	@Schema(description = "Tournament net strokes", accessMode = READ_ONLY, minimum = "0")
	private Integer strokesNetto;

	@NotNull
	@Schema(description = "Number of played rounds", accessMode = READ_ONLY, minimum = "0")
	private Integer playedRounds;

	@NotNull
	@Schema(description = "Stableford net", accessMode = READ_ONLY, minimum = "0")
	private Integer stbNet;

	@NotNull
	@Schema(description = "Stableford gross", accessMode = READ_ONLY, minimum = "0")
	private Integer stbGross;

	@NotNull
	@Schema(description = "Number of rounds applicable for stroke categories", accessMode = READ_ONLY, minimum = "0")
	private Integer strokeRounds;
}
