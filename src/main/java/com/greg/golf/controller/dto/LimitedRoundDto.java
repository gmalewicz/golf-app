package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.util.Date;


import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.greg.golf.entity.helpers.Views;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LimitedRoundDto {
	
	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Round identifier", example = "25", accessMode = READ_ONLY)
	private Long id;
	
	@NotNull
	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Match play identifier", example = "true", accessMode = READ_WRITE,  allowableValues = { "true", "false" })
	private Boolean matchPlay;

	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Match play allowance format", example = "0.5", accessMode = READ_ONLY, allowableValues = {
			"0.5", "0.75", "0.9", "1"})
	private Float mpFormat;
		
	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Course for a round", accessMode = READ_WRITE)
	private LimitedCourseDto course;

	@NotNull
	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Date for a round", accessMode = READ_WRITE, pattern="yyyy/MM/dd kk:mm")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd kk:mm")
	private Date roundDate;
}
