package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

@Getter
@Setter
public class HoleDto {

	@Schema(description = "Hole par", example = "4", accessMode = READ_WRITE, minimum = "3", maximum = "6")
	private Integer par;

	@Schema(description = "Hole number", example = "9", accessMode = READ_WRITE, minimum = "1", maximum = "18")
	private Integer number;

	@Schema(description = "Hole handicap (si)", example = "1", accessMode = READ_WRITE, minimum = "1", maximum = "18")
	private Integer si;
}
