package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Getter
@Setter
public class IdDto {

	@NotNull
	@Schema(description = "Identifier", example = "25", accessMode = READ_ONLY)
	private Long id;
}
