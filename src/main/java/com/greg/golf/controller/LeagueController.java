package com.greg.golf.controller;

import com.greg.golf.controller.dto.LeagueDto;
import com.greg.golf.entity.League;
import com.greg.golf.service.LeagueService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@OpenAPIDefinition(tags = { @Tag(name = "League API") })
public class LeagueController extends BaseController {

    private final LeagueService leagueService;

    public LeagueController(ModelMapper modelMapper, LeagueService leagueService) {
        super(modelMapper);
        this.leagueService = leagueService;
    }

    @Tag(name = "League API")
    @Operation(summary = "Adds league")
    @PostMapping(value = "/rest/League")
    public HttpStatus addCycle(
            @Parameter(description = "League object", required = true) @RequestBody LeagueDto leagueDto) {

        log.info("trying to add league: " + leagueDto.getName());

        leagueService.addLeague(modelMapper.map(leagueDto, League.class));

        return HttpStatus.OK;
    }

    @Tag(name = "League API")
    @Operation(summary = "Return all leagues")
    @GetMapping(value = "/rest/League")
    public List<LeagueDto> getLeagues() {
        log.info("Requested list of leagues");

        return mapList(leagueService.findAllLeagues(), LeagueDto.class);
    }
}
