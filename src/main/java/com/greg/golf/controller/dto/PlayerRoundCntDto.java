package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

@Getter
@Setter
public class PlayerRoundCntDto {

    @NotNull
    @Schema(description = "Player identifier", example = "25", accessMode = READ_WRITE)
    private Long id;

    @NotNull
    @Schema(description = "Player nick name", example = "golfer", accessMode = READ_ONLY, maxLength=20)
    private String nick;

    @NotNull
    @Schema(description = "Player sex: false - male, true - female", example = "false", accessMode = READ_ONLY)
    private Boolean sex;

    @NotNull
    @Schema(description = "Player handicap", example = "38.5", accessMode = READ_ONLY, minimum="-5", maximum="54")
    private Float whs;

    @NotNull
    @Schema(description = "Player role", example = "1", accessMode = READ_ONLY, minimum="0", maximum="1")
    private Integer role;

    @NotNull
    @Schema(description = "Player role", example = "1", accessMode = READ_ONLY, minimum="0", maximum="1")
    private Long roundCnt;

    @Schema(description = "Polish Golf Association member identifier, null when not set", example = "12345", accessMode = READ_ONLY, nullable = true)
    private Long pzgId;

    @NotNull
    @Schema(description = "Player type", example = "0 - Local, 1 - Facebook", accessMode = READ_ONLY, minimum="0", maximum="1")
    private Integer type;
}
