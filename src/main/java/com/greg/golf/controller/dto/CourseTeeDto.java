package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Getter
@Setter
public class CourseTeeDto {

	@Schema(description = "Player identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Tee name", example = "ladies red 1-18", accessMode = READ_WRITE, maxLength = 100)
	private String tee;

	@NotNull
	@Schema(description = "Course rate", example = "71", accessMode = READ_WRITE, minimum = "25", maximum = "89")
	private Float cr;

	@NotNull
	@Schema(description = "Slope rate", example = "78", accessMode = READ_WRITE, minimum = "55", maximum = "155")
	private Integer sr;

	@NotNull
	@Schema(description = "Tee type: 0 - 18 holes, 1 - first 9, 2 - second 9", example = "0", accessMode = READ_WRITE, minimum = "1", maximum = "2")
	private Integer teeType;
	
	@NotNull
	@Schema(description = "Player sex: false - male, true - female", example = "false", accessMode = READ_WRITE)
	private Boolean sex;

}
