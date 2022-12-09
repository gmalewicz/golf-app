package com.greg.golf.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GameData {

	@NotNull
	private String[] playerNicks;
	@NotNull
	private Integer [] score;
	@NotNull
	private Short[][] gameResult;

}
