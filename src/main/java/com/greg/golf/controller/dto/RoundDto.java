package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.greg.golf.entity.helpers.Views;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoundDto {
	
	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Round identifier", example = "25", accessMode = READ_ONLY)
	private Long id;
	
	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Match play identifier", example = "true", accessMode = READ_WRITE,  allowableValues = { "true", "false" })
	private Boolean matchPlay;

	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Course for a round", accessMode = READ_WRITE)
	private CourseDto course;

	@Schema(description = "Set of players participated in the round", accessMode = READ_WRITE, minimum = "1", maximum = "4")
	private Set<PlayerDto> player;

	@JsonView(Views.RoundWithoutPlayer.class)
	@Schema(description = "Date for a round", accessMode = READ_WRITE, pattern="yyyy/MM/dd kk:mm")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd kk:mm")
	private Date roundDate;

	@Schema(description = "Round score card list (one per hole)", accessMode = READ_WRITE)
	private List<ScoreCardDto> scoreCard;
	
	@Schema(description = "Round tournament", accessMode = READ_WRITE)
	private TournamentDto tournament;
}
