package com.greg.golf.controller;

import com.greg.golf.controller.dto.LeagueDto;
import com.greg.golf.controller.dto.LeagueMatchDto;
import com.greg.golf.controller.dto.LeaguePlayerDto;
import com.greg.golf.controller.dto.LeagueResultDto;
import com.greg.golf.entity.League;
import com.greg.golf.entity.LeagueMatch;
import com.greg.golf.entity.LeaguePlayer;
import com.greg.golf.service.LeagueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class LeagueController extends BaseController {

    private final LeagueService leagueService;

    public LeagueController(ModelMapper modelMapper, LeagueService leagueService) {
        super(modelMapper);
        this.leagueService = leagueService;
    }

    @Tag(name = "League API")
    @Operation(summary = "Adds league")
    @PostMapping(value = "/rest/League")
    public HttpStatus addLeague(
            @Parameter(description = "League object", required = true) @RequestBody @Valid LeagueDto leagueDto) {

        log.info("trying to add league: {}", leagueDto.getName());

        leagueService.addLeague(modelMapper.map(leagueDto, League.class));

        return HttpStatus.OK;
    }

    @Tag(name = "League API")
    @Operation(summary = "Return all leagues")
    @GetMapping(value = "/rest/League/{pageId}")
    public List<LeagueDto> getLeagues(
            @Parameter(description = "Page id", example = "0", required = true) @PathVariable("pageId") @NotNull @PositiveOrZero Integer pageId) {

        log.info("Requested list of leagues");
        return mapList(leagueService.findAllLeaguesPageable(pageId), LeagueDto.class);
    }

    @Tag(name = "League API")
    @Operation(summary = "Add player participant to tournament")
    @PostMapping(value = "/rest/LeaguePlayer")
    public HttpStatus addPlayer(
            @Parameter(description = "LeaguePlayer object", required = true) @RequestBody @Valid LeaguePlayerDto leaguePlayerDto) {

        log.info("trying to add league player: {}", leaguePlayerDto.getNick());

        leagueService.addPlayer(modelMapper.map(leaguePlayerDto, LeaguePlayer.class));

        return HttpStatus.OK;
    }

    @Tag(name = "League API")
    @Operation(summary = "Delete player participant from league")
    @DeleteMapping(value = "/rest/LeaguePlayer/{leagueId}/{playerId}")
    public HttpStatus deletePlayer(
            @Parameter(description = "League id", example = "1", required = true) @PathVariable("leagueId") Long leagueId,
            @Parameter(description = "Player id", example = "1", required = true) @PathVariable(name = "playerId", required = false) Long playerId) {

        log.info("Delete league player: {} for league {}", playerId, leagueId);

        leagueService.deletePlayer(leagueId, playerId);

        return HttpStatus.OK;
    }

    @Tag(name = "League API")
    @Operation(summary = "Return all players belonging to the league")
    @GetMapping(value = "/rest/LeaguePlayer/{leagueId}")
    public List<LeaguePlayerDto> getLeaguePlayers(
            @Parameter(description = "League id", example = "1", required = true) @PathVariable("leagueId") Long leagueId) {
        log.info("Requested player participating in league {}", leagueId);
        return mapList(leagueService.getLeaguePlayers(leagueId), LeaguePlayerDto.class);
    }

    @Tag(name = "League API")
    @Operation(summary = "Close league. Further updates will not be possible.")
    @PatchMapping(value = "/rest/LeagueClose/{leagueId}")
    public HttpStatus closeLeague(
            @Parameter(description = "League id to be closed", required = true) @PathVariable("leagueId") Long leagueId) {

        log.info("trying to close league: {}", leagueId);

        leagueService.closeLeague(leagueId);

        return HttpStatus.OK;
    }

    @Tag(name = "League API")
    @Operation(summary = "Return all matches belonging to the league")
    @GetMapping(value = "/rest/LeagueMatch/{leagueId}")
    public List<LeagueMatchDto> getMatches(
            @Parameter(description = "League id", example = "1", required = true) @PathVariable("leagueId") Long leagueId) {
        log.info("Requested matches for league {}", leagueId);

        return mapList(leagueService.getMatches(leagueId), LeagueMatchDto.class);

    }

    @Tag(name = "League API")
    @Operation(summary = "Add match to league")
    @PostMapping(value = "/rest/LeagueMatch")
    public HttpStatus addMatch(
            @Parameter(description = "LeagueMatch object", required = true) @RequestBody @Valid LeagueMatchDto leagueMatchDto) {

        log.info("trying to add match to league: {}", leagueMatchDto);

        leagueService.addMatch(modelMapper.map(leagueMatchDto, LeagueMatch.class));

        return HttpStatus.OK;
    }

    @Tag(name = "League API")
    @Operation(summary = "Delete match for given winner and looser")
    @DeleteMapping(value = "/rest/LeagueMatch/{leagueId}/{winnerId}/{looserId}")
    public HttpStatus deleteMatch(
            @Parameter(description = "league id", example = "1", required = true) @PathVariable("leagueId") Long leagueId,
            @Parameter(description = "winner id", example = "1", required = true) @PathVariable("winnerId") Long winnerId,
            @Parameter(description = "looser id", example = "1", required = true) @PathVariable("looserId") Long looserId) {

        log.info("Delete match for winner: {} and looser: {} in league: {}", winnerId, looserId, leagueId);

        leagueService.deleteMatch(leagueId, winnerId, looserId);

        return HttpStatus.OK;
    }

    @Tag(name = "League API")
    @Operation(summary = "Delete league for given id")
    @DeleteMapping(value = "/rest/League/{leagueId}")
    public HttpStatus deleteLeague(
            @Parameter(description = "league id", example = "1", required = true) @PathVariable("leagueId") Long leagueId) {

        log.info("Delete league: {}", leagueId);

        leagueService.deleteLeague(leagueId);

        return HttpStatus.OK;
    }

    @Tag(name = "League API")
    @Operation(summary = "Send email notification to subscribers")
    @PostMapping(value = "/rest/League/Notification/{leagueId}")
    public HttpStatus notifySubscribers(
            @Parameter(description = "League id", example = "1", required = true)
            @NotNull
            @Positive
            @Valid
            @PathVariable("leagueId") Long leagueId,
            @Parameter(description = "LeagueResults list", required = true) @RequestBody @Valid LeagueResultDto[] leagueResultsDto) {

        log.info("trying to send notifications for league: {}", leagueId);
        leagueService.processNotifications(leagueId, leagueResultsDto);
        return HttpStatus.OK;
    }

    @Tag(name = "League API")
    @Operation(summary = "Add notification to league")
    @PostMapping(value = "/rest/League/AddNotification/{leagueId}")
    public HttpStatus addNotification(
            @Parameter(description = "League id", example = "1", required = true)
            @NotNull
            @Positive
            @Valid
            @PathVariable("leagueId") Long leagueId) {

        leagueService.addNotification(leagueId);
        return HttpStatus.OK;
    }

    @Tag(name = "League API")
    @Operation(summary = "Removes notification from league")
    @PostMapping(value = "/rest/League/RemoveNotification/{leagueId}")
    public HttpStatus removeNotification(
            @Parameter(description = "League id", example = "1", required = true)
            @NotNull
            @Valid
            @Positive
            @PathVariable("leagueId") Long leagueId) {

        leagueService.removeNotification(leagueId);
        return HttpStatus.OK;
    }
}
