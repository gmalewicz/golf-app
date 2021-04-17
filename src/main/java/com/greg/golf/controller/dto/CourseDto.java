package com.greg.golf.controller.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CourseDto extends LimitedCourseDto {

	@JsonProperty(value = "holes", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "List of holes")
	private List<HoleDto> holes;

	@JsonProperty(value = "tees", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "List of tees")
	private List<CourseTeeDto> tees;
}
