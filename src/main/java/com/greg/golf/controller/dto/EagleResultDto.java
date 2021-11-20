package com.greg.golf.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;


import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

@Getter
@Setter
public class EagleResultDto {

    @NotNull
    @JsonProperty(value = "firstName", access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "Player first name", accessMode = WRITE_ONLY)
    private String firstName;

    @NotNull
    @JsonProperty(value = "lastName", access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "Player last name", accessMode = WRITE_ONLY)
    private String lastName;

    @NotNull
    @Schema(description = "Player handicap", example = "38.5", accessMode = WRITE_ONLY, minimum = "-5", maximum = "54")
    @JsonProperty(value = "whs", access = JsonProperty.Access.WRITE_ONLY)
    private Float whs;

    @NotNull
    @Schema(description = "Round results stableford net", accessMode = WRITE_ONLY)
    @JsonProperty(value = "r", access = JsonProperty.Access.WRITE_ONLY)
    private int[] r;
}
