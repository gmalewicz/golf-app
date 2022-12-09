package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;


@Getter
@Setter
public class GameDto {

	@Schema(description = "Player identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@JsonProperty( value = "player", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "Owner (player) of the game", accessMode = READ_WRITE)
	private PlayerDto player;

	@NotNull
	@Schema(description = "Game identifier", example = "1", accessMode = READ_WRITE, allowableValues = { "1", "2" })
	private Long gameId;

	@NotNull
	@Schema(description = "Game stake", example = "0.5", accessMode = READ_WRITE)
	private Float stake;

	@NotNull
	@Schema(description = "Date in ISO format", accessMode = READ_WRITE)
	private Date gameDate;

	@NotNull
	@Schema(description = "Game data", accessMode = READ_WRITE)
	private GameDataDto gameData;

}
