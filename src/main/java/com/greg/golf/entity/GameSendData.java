package com.greg.golf.entity;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GameSendData {
	
	@NotNull
	@Schema(description = "Game identifier for which details shall be sent", example = "25")
	private Long gameId;
	
	@NotNull
	@Schema(description = "Email address where data shall be send", example = "example@gmail.com")
	private String email;

}
