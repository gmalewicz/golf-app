package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

@Getter
@Setter
public class CycleDto {

	@Schema(description = "Cycle identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Cycle name", example = "2018 Golf Cycle", accessMode = READ_WRITE, maxLength = 255)
	private String name;

	@NotNull
	@Schema(description = "Cycle status: false - open, true - close", example = "false", accessMode = READ_WRITE)
	private Boolean status;

	@NotNull
	@Schema(description = "Cycle rule: 0 - based on STB net, 1 - Volvo 2021", example = "0", accessMode = READ_WRITE)
	private Integer rule;

	@Schema(description = "Cycle owner", accessMode = READ_WRITE)
	private PlayerDto player;
}
