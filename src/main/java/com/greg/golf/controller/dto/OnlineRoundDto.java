package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import com.greg.golf.controller.validators.TeamNotNull;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import lombok.Setter;

@TeamNotNull
@Getter
@Setter
public class OnlineRoundDto {

	@Schema(description = "Course identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Course for a round", accessMode = READ_WRITE)
	private CourseDto course;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "kk:mm")
	@Schema(description = "Round tee time", accessMode = READ_WRITE, pattern = "kk:mm")
	private String teeTime;

	@NotNull
	@Schema(description = "Player", accessMode = READ_WRITE)
	private PlayerDto player;

	@NotNull
	@Schema(description = "Owner (player) of the round", accessMode = READ_WRITE)
	private Long owner;

	@NotNull
	@Schema(description = "Round completed flag", example = "true", accessMode = READ_WRITE, allowableValues = { "true",
			"false" })
	private Boolean finalized;

	@Schema(description = "Number of putts", example = "7", accessMode = READ_WRITE, minimum = "1", maximum = "10")
	private Boolean putts;

	@Schema(description = "Number of penalties", example = "3", accessMode = READ_WRITE, minimum = "1", maximum = "15")
	private Boolean penalties;

	@NotNull
	@Schema(description = "Supported formats: " +
			"0 = stroke play, " +
			"1 = match play, " +
			"2 = four ball stroke play, " +
			"3 = four ball stroke play",
			example = "0", accessMode = READ_WRITE, allowableValues = {"0", "1", "2", "3"})
	private Integer format;

	@Schema(description = "Match play hcp multiplier", example = "0.5", accessMode = READ_WRITE, allowableValues = {
			"0.5", "0.75", "0.9", "1"})
	private Float mpFormat;

	@NotNull
	@Schema(description = "Course tee", accessMode = READ_WRITE)
	@JsonProperty(value = "tee")
	private CourseTeeDto courseTee;

	@JsonProperty(value = "onlineScoreCard", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "List of scorecard objects", accessMode = WRITE_ONLY)
	private List<OnlineScoreCardDto> scoreCard;

	@Schema(description = "List of scorecard AOI objects", accessMode = READ_WRITE)
	private List<OnlineScoreCardDto> scoreCardAPI;
	
	@Schema(description = "Second player nick for match play", accessMode = READ_WRITE)
	private String nick2;

	@Schema(description = "Supported values: " +
			"1 = team 1, " +
			"2 = team 2, ",
			example = "1", accessMode = READ_WRITE, allowableValues = {"1", "2"})
	private Integer team;

	@NotNull
	@Schema(description = "Round identifier", example = "1", accessMode = READ_WRITE)
	private Integer identifier;
}
