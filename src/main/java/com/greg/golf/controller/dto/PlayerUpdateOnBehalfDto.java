package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.*;

@Getter
@Setter
public class PlayerUpdateOnBehalfDto {

	@Schema(description = "player identifier", example = "1", accessMode = READ_WRITE)
	private Long id;

	@NotNull
	@Size(min = 1, max = 20, message = "Nick must be between 1 and 20 characters")
	@Schema(description = "Player nick name", example = "golfer", accessMode = READ_WRITE, maxLength=20)
	protected String nick;

	@NotNull
	@Min(value = -5)
	@Max(value = 54)
	@Schema(description = "Player handicap", example = "38.5", accessMode = READ_WRITE, minimum="-5", maximum="54")
	private Float whs;

	@NotNull
	@Schema(description = "Player sex: false - male, true - female", example = "false", accessMode = READ_WRITE)
	private Boolean sex;

	@Schema(description = "Update in case of social player", example = "false", accessMode = WRITE_ONLY)
	private Boolean updateSocial;
}
