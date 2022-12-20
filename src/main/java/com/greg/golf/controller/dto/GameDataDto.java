package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameDataDto {

	@NotNull
	@Schema(description = "Player's nicks", example = "Golfer", accessMode = READ_WRITE)
	private String[] playerNicks;
	
	@NotNull
	@Schema(description = "Player scores", accessMode = READ_WRITE)
	private Integer[] score;
	
	@NotNull
	@Schema(description = "Game result", accessMode = READ_WRITE)
	private Short[][] gameResult;
}
