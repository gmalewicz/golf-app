package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class PlayerDto {
		
	@Schema(description = "Player identifier", example = "25", accessMode = READ_WRITE)
	private Long id;
	
	@NotNull
	@Schema(description = "Player nick name", example = "golfer", accessMode = READ_WRITE, maxLength=20)
	private String nick;

	@NotNull
	@JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "Player password", example = "welcome", accessMode = READ_WRITE, maxLength=60)
	private String password;

	@NotNull
	@Schema(description = "Player handicap", example = "38.5", accessMode = READ_WRITE, minimum="-5", maximum="54")
	private Float whs;

	@NotNull
	@Schema(description = "Player role", example = "0", accessMode = READ_ONLY, minimum ="0", maximum = "1")
	private Integer role;
	
	@JsonProperty(value = "captcha", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "Google captcha", example = "03AG...", accessMode = WRITE_ONLY)
	private String captcha;
	
}
