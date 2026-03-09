package com.greg.golf.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.greg.golf.controller.dto.OnlineRoundDto;
import com.greg.golf.controller.dto.OnlineScoreCardDto;
import com.greg.golf.controller.dto.PlayerDto;
import com.greg.golf.entity.OnlineRound;
import com.greg.golf.entity.OnlineScoreCard;
import com.greg.golf.service.OnlineRoundService;
import com.greg.golf.service.PlayerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Slf4j
public class OnlineScoreCardController extends BaseController {

	private final OnlineRoundService onlineRoundService;
	private final PlayerService playerService;
	private final SimpMessagingTemplate template;


	public OnlineScoreCardController(ModelMapper modelMapper, OnlineRoundService onlineRoundService,
			PlayerService playerService, SimpMessagingTemplate template) {
		super(modelMapper);
		this.onlineRoundService = onlineRoundService;
		this.playerService = playerService;
		this.template = template;
	}

	@MessageMapping("/hole")
	@SendTo("/topic")
	public OnlineScoreCardDto send(OnlineScoreCardDto onlineScoreCardDto) {

        log.info("Received s -  {}", onlineScoreCardDto);

		var onlineScoreCard = modelMapper.map(onlineScoreCardDto, OnlineScoreCard.class);

		onlineRoundService.saveOnlineScoreCard(modelMapper.map(onlineScoreCardDto, OnlineScoreCard.class));

		return modelMapper.map(onlineScoreCard, OnlineScoreCardDto.class);
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Save hole results for online round")
	@PostMapping(value = "/rest/OnlineScoreCard")
	public HttpStatus syncOnlineScoreCards(
			@Parameter(description = "List of ScoreCard objects", required = true) @RequestBody List<OnlineScoreCardDto> onlineScoreCards) {

		log.info("Attempt to save hole result for online round");

		List<OnlineScoreCard> oScoreCardLst = mapList(onlineScoreCards, OnlineScoreCard.class);

		oScoreCardLst = onlineRoundService.syncOnlineScoreCards(oScoreCardLst);

		oScoreCardLst.stream().filter(OnlineScoreCard::isSyncRequired).forEach(onlineScoreCard ->
				template.convertAndSend("/topic" ,modelMapper.map(onlineScoreCard, OnlineScoreCardDto.class)));

		return HttpStatus.OK;
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Adds online rounds")
	@PostMapping(value = "/rest/OnlineRounds")
	public List<OnlineRoundDto> addOnlineRounds(
			@Parameter(description = "List of OnlineRound objects", required = true) @RequestBody @Valid List<OnlineRoundDto> onlineRounds) {

		log.info("trying to add onlineRounds");
		List<OnlineRound> orLst = mapList(onlineRounds, OnlineRound.class);

		return mapList(onlineRoundService.save(orLst), OnlineRoundDto.class);

	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return all online rounds")
	@GetMapping(value = "/rest/OnlineRound/all")
	public List<OnlineRoundDto> getOnlineRounds() {
		log.info("Requested online rounds");
		return mapList(onlineRoundService.getOnlineRounds(), OnlineRoundDto.class);
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return all score cards already saved for on-line round")
	@GetMapping(value = "/rest/OnlineScoreCard/{onlineRoundId}")
	public List<OnlineScoreCardDto> getOnlineScoreCards(
			@Parameter(description = "Online round id", example = "1", required = true) @PathVariable("onlineRoundId") Long onlineRoundId) {
		log.info("Requested online round score cards");
		return mapList(onlineRoundService.getOnlineScoreCards(onlineRoundId), OnlineScoreCardDto.class);
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return online rounds for course")
	@GetMapping(value = "/rest/OnlineRoundCourse/{courseId}")
	public List<OnlineRoundDto> getOnlineRoundsCourse(
			@Parameter(description = "Course id", example = "1", required = true) @PathVariable("courseId") Long courseId) {
		log.info("Requested online rounds for course");
		return mapList(onlineRoundService.getOnlineRoundsForCourse(courseId), OnlineRoundDto.class);
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return player for nick")
	@GetMapping(value = "/rest/Player/{nick}")
	public PlayerDto getPlayer(
			@Parameter(description = "nick", example = "player", required = true) @PathVariable("nick") String nick) {

        log.info("Requested player for nick: {}", nick);
		var player = playerService.getPlayerForNick(nick);
		if (player == null) {
			return null;
		}
		
		return modelMapper.map(playerService.getPlayerForNick(nick), PlayerDto.class);
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Online scorecard API")
	@Operation(summary = "Delete online round with given owner id.")
	@DeleteMapping("/rest/OnlineRound/{identifier}")
	public HttpStatus deleteOnlineRoundForIdentifier(
			@Parameter(description = "Online round identifier", example = "1", required = true) @PathVariable Integer identifier) {

        log.info("trying to delete online round with identifier: {}", identifier);
		onlineRoundService.deleteForIdentifier(identifier);
		return HttpStatus.OK;

	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Online scorecard API")
	@Operation(summary = "Finalize online rounds for identifier")
	@PostMapping(value = "/rest/OnlineRound")
	public HttpStatus finalizeOnlineRounds(
			@Parameter(description = "Identifier object", required = true) @RequestBody Integer identifier) {

        log.info("trying to finalize online round for identifier: {}", identifier);

		onlineRoundService.finalize(identifier);

		return HttpStatus.OK;
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return online rounds for identifier")
	@GetMapping(value = "/rest/OnlineRound/Identifier/{identifier}")
	public List<OnlineRoundDto> getOnlineRoundsForIdentifier(
			@Parameter(description = "Online round identifier", example = "1", required = true) @PathVariable("identifier") Integer identifier) {
		log.info("Requested online rounds for identifier: {}", identifier);
		return mapList(onlineRoundService.getOnlineRoundsForIdentifier(identifier), OnlineRoundDto.class);
	}
}
