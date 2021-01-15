package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

import java.util.Date;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Getter
@Setter
public class GameDto {

	@Schema(description = "Player identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@Schema(description = "Owner (player) of the game", accessMode = READ_WRITE)
	private PlayerDto player;

	@Schema(description = "Game identifier", example = "1", accessMode = READ_WRITE, allowableValues = { "1", "2" })
	private Long gameId;

	@Schema(description = "Game stake", example = "0.5", accessMode = READ_WRITE)
	private Float stake;

	@Schema(description = "Date in ISO format", accessMode = READ_WRITE)
	private Date gameDate;

	@Schema(description = "Game data", accessMode = READ_WRITE)
	private GameDataDto gameData;

}
