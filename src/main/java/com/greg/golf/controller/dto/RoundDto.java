package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoundDto extends BaseRoundDto {
	
	@Schema(description = "Course for a round", accessMode = READ_WRITE)
	private CourseDto course;

	@Schema(description = "Set of players participated in the round", accessMode = READ_WRITE, minimum = "1", maximum = "4")
	private List<PlayerDto> player;

	@JsonProperty(value = "scoreCard", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "Round score card list (one per hole)", accessMode = READ_WRITE)
	private List<ScoreCardDto> scoreCard;
	
	@JsonProperty(value = "tournament", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "Round tournament", accessMode = READ_WRITE)
	private TournamentDto tournament;
}
