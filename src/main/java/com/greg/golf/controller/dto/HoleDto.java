package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

@Getter
@Setter
public class HoleDto {

	@NotNull
	@Schema(description = "Hole par", example = "4", accessMode = READ_WRITE, minimum = "3", maximum = "6")
	private Integer par;

	@NotNull
	@Schema(description = "Hole number", example = "9", accessMode = READ_WRITE, minimum = "1", maximum = "18")
	private Integer number;

	@NotNull
	@Schema(description = "Hole handicap (si)", example = "1", accessMode = READ_WRITE, minimum = "1", maximum = "18")
	private Integer si;
}
