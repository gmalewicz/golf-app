package com.greg.golf.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class GameData implements Serializable {

	@NotNull
	private String[] playerNicks;
	@NotNull
	private Integer [] score;
	@NotNull
	private Short[][] gameResult;

}
