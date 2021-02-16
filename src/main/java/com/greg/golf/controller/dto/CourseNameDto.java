package com.greg.golf.controller.dto;


import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class CourseNameDto {

	
	@Schema(description = "Course name", example = "Lisia Polana", accessMode = READ_ONLY, maxLength = 100)
	@NotNull
	private String name;
	
}
