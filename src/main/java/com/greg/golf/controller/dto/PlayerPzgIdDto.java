package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

@Getter
@Setter
public class PlayerPzgIdDto {

	@NotNull
	@Schema(description = "Player identifier", example = "25", accessMode = READ_WRITE)
	private Long id;

	@Min(value = 1)
	@Schema(description = "Polish Golf Association member identifier, null clears it", example = "12345", accessMode = READ_WRITE, nullable = true)
	private Long pzgId;
}
