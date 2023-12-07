package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Getter
@Setter
public class VersionDto {

    @Schema(description = "Version identifier", example = "3.5.6", accessMode = READ_ONLY)
    private String version;

}
