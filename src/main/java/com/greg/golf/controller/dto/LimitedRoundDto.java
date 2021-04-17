package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

import com.fasterxml.jackson.annotation.JsonView;
import com.greg.golf.entity.helpers.Views;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LimitedRoundDto extends BaseRoundDto {
	
	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Course for a round", accessMode = READ_WRITE)
	private LimitedCourseDto course;
}
