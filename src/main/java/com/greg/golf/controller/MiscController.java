package com.greg.golf.controller;

import com.greg.golf.controller.dto.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@OpenAPIDefinition(tags = @Tag(name = "Miscellaneous API"))
public class MiscController extends BaseController {

    @Value("${app.version:unknown}")
    String version;

    public MiscController(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Tag(name = "Miscellaneous API")
    @Operation(summary = "Get backend version")
    @GetMapping(value = "/rest/Version")
    public ResponseEntity<VersionDto> getVersion(HttpServletRequest request) {

        log.info("Getting backend version: " + version);

        var versionDto = new VersionDto();
        versionDto.setVersion(version);

        return new ResponseEntity<>(versionDto, HttpStatus.OK);
    }
}
