package com.greg.golf.controller.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;


@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class PlayerNickDto {

	
	@Schema(description = "Player nick", example = "Gre", accessMode = READ_ONLY, maxLength = 20)
	@NotNull
	private String nick;

	@Schema(description = "Page number", example = "1", accessMode = READ_ONLY)
	@NotNull
	private Integer page;
}
