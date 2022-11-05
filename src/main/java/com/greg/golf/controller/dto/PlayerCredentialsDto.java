package com.greg.golf.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;


@Getter
@Setter
public class PlayerCredentialsDto {

	@NotNull
	@Size(min = 1, max = 20, message = "Nick must be between 1 and 20 characters")
	@Schema(description = "Player nick name", example = "golfer", accessMode = WRITE_ONLY, maxLength=20)
	protected String nick;
	
	@NotNull
	@Size(min = 1, max = 60, message = "Password must be between 1 and 60 characters")
	@Schema(description = "Player password", example = "welcome", accessMode = WRITE_ONLY, maxLength=60)
	protected String password;
}
