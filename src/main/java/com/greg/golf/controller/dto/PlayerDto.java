package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class PlayerDto implements Comparable<PlayerDto> {
		
	@Schema(description = "Player identifier", example = "25", accessMode = READ_WRITE)
	private Long id;
	
	@NotNull
	@Schema(description = "Player nick name", example = "golfer", accessMode = READ_WRITE, maxLength=20)
	private String nick;

	@NotNull
	@Schema(description = "Player sex: false - male, true - female", example = "false", accessMode = READ_WRITE)
	private Boolean sex;
	
	@NotNull
	@JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "Player password", example = "welcome", accessMode = READ_WRITE, maxLength=60)
	private String password;

	@NotNull
	@Schema(description = "Player handicap", example = "38.5", accessMode = READ_WRITE, minimum="-5", maximum="54")
	private Float whs;
	
	@JsonProperty(value = "captcha", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "Google captcha", example = "03AG...", accessMode = WRITE_ONLY)
	private String captcha;
	
	@Override
	public int compareTo(PlayerDto o) {
				
		return (int)(id - o.id);
	}
	
	@Override
	public boolean equals(Object obj) {
		
		var retVal = false;
		
		if (!(obj instanceof PlayerDto)) {
			return false;
		}
		
		if (id == ((PlayerDto) obj).id) {
			retVal = true;
		}
	
		return retVal;
		
	}
	
	@Override
	public int hashCode() {

		return super.hashCode();
	}
	
}
