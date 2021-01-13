package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDto {

	@Schema(description = "Player identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@Schema(description = "Tee name", example = "ladies red 1-18", accessMode = READ_WRITE, maxLength = 100)
	private String name;

	@Schema(description = "Course par", example = "72", accessMode = READ_WRITE, minimum = "30", maximum = "79")
	private Integer par;

	@Schema(description = "Number of holes", example = "9", accessMode = READ_WRITE, allowableValues = { "9", "18" })
	private Integer holeNbr;

	@Schema(description = "List of holes")
	private List<HoleDto> holes;

	@Schema(description = "List of tees")
	private List<CourseTeeDto> tees;
}
