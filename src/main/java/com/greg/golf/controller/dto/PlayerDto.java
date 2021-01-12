package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

@Getter
@Setter
public class PlayerDto {
		
	@Schema(description = "Player identifier", example = "25", accessMode = READ_WRITE)
	private Long id;
	
	@Schema(description = "Player nick name", example = "golfer", accessMode = READ_WRITE, maxLength=20)
	private String nick;

	@Schema(description = "Player password", example = "welcome", accessMode = READ_WRITE, maxLength=60)
	private String password;

	@Schema(description = "Player handicap", example = "38.5", accessMode = READ_WRITE, minimum="-5", maximum="54")
	private Float whs;

	@Schema(description = "Player role", example = "0", accessMode = READ_ONLY, minimum ="0", maximum = "1")
	private Integer role;
	
	@Schema(description = "Google captcha", example = "03AG...", accessMode = WRITE_ONLY)
	private String captcha;
	
}
