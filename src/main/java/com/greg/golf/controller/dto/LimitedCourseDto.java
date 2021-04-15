package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonView;
import com.greg.golf.entity.helpers.Views;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class LimitedCourseDto {

	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Course identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Course name", example = "Lisia Polana", accessMode = READ_WRITE, maxLength = 100)
	private String name;

	@NotNull
	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Course par", example = "72", accessMode = READ_WRITE, minimum = "30", maximum = "79")
	private Integer par;

	@NotNull
	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Number of holes", example = "9", accessMode = READ_WRITE, allowableValues = { "9", "18" })
	private Integer holeNbr;
}
