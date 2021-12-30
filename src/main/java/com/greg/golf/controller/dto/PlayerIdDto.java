package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Getter
@Setter
public class PlayerIdDto {

	@NotNull
	@Schema(description = "Player identifier", example = "25", accessMode = READ_ONLY)
	private Long id;
}
