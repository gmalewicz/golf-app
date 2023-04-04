package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlayerDto implements Comparable<PlayerDto> {

	@Schema(description = "Player identifier", example = "25", accessMode = READ_WRITE)
	private Long id;

	@Schema(description = "Player nick name", example = "golfer", accessMode = READ_WRITE, maxLength=20)
	private String nick;

	@ToString.Exclude
	@Schema(description = "Player sex: false - male, true - female", example = "false", accessMode = READ_WRITE)
	private Boolean sex;

	@ToString.Exclude
	@JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "Player password", example = "welcome", accessMode = READ_WRITE, maxLength=60)
	private String password;

	@ToString.Exclude
	@Schema(description = "Player handicap", example = "38.5", accessMode = READ_WRITE, minimum="-5", maximum="54")
	private Float whs;

	@ToString.Exclude
	@JsonProperty(value = "captcha", access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "Google captcha", example = "03AG...", accessMode = WRITE_ONLY)
	private String captcha;

	@ToString.Exclude
	@Schema(description = "Update in case of social player", example = "false", accessMode = WRITE_ONLY)
	private Boolean updateSocial;
	
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
		
		if (id.equals(((PlayerDto) obj).id)) {
			retVal = true;
		}
	
		return retVal;
	}

	@Override
	public int hashCode() {

		return super.hashCode();
	}
}
