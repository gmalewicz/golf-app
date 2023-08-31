package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

@Getter
@Setter
public class LeagueDto {

	@Schema(description = "League identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "League name", example = "2018 Golf League", accessMode = READ_WRITE, maxLength = 255)
	private String name;

	@NotNull
	@Schema(description = "League status: false - open, true - close", example = "false", accessMode = READ_WRITE)
	private Boolean status;

	@Schema(description = "League owner", accessMode = READ_WRITE)
	private PlayerDto player;
}
