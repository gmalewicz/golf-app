package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TournamentDto {

	@Schema(description = "Tournament identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@Schema(description = "Tournament name", example = "Test tournament", accessMode = READ_WRITE, maxLength = 255)
	private String name;

	@Schema(description = "Tournamnet start date", accessMode = READ_WRITE, pattern = "yyyy/MM/dd")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
	private Date startDate;

	@Schema(description = "Tournamnet end date", accessMode = READ_WRITE, pattern = "yyyy/MM/dd")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
	private Date endDate;

	@Schema(description = "Tournamnet owner", accessMode = READ_WRITE)
	private PlayerDto player;
}
