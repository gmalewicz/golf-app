package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.*;

@Getter
@Setter
public class CycleTournamentDto {

	@Schema(description = "CycleTournament identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Cycle tournament name", example = "Tournament 1", accessMode = READ_WRITE, maxLength = 255)
	private String name;

	@NotNull
	@Schema(description = "Number of rounds: 0 - based on STB net, 1 - Volvo 2021", example = "1", accessMode = READ_WRITE, minimum = "1", maximum = "4")
	private Integer rounds;

	@NotNull
	@Schema(description = "Indicates if only best tournament round should be taken into account", example = "false", accessMode = READ_WRITE)
	private Boolean bestOf;

	@Schema(description = "Cycle object for that tournament", accessMode = WRITE_ONLY)
	private CycleDto cycleDto;
}
