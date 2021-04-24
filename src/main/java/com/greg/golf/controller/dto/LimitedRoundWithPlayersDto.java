package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LimitedRoundWithPlayersDto extends LimitedRoundDto {

	@Schema(description = "Set of players participated in the round", accessMode = READ_WRITE, minimum = "1", maximum = "4")
	private List<PlayerDto> player;
}
