package com.greg.golf.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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
    @Schema(description = "Round results Stableford net", accessMode = WRITE_ONLY)
    @JsonProperty(value = "r", access = JsonProperty.Access.WRITE_ONLY)
    private int[] r;

    @NotNull
    @Schema(description = "Series number. Two series are supported",
            example = "1", minimum = "1", maximum = "2", accessMode = WRITE_ONLY)
    private Integer series;
}
