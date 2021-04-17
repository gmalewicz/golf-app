package com.greg.golf.controller;

import java.util.List;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
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
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.service.OnlineRoundService;
import com.greg.golf.service.PlayerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
public class OnlineScoreCardController extends BaseController {

	private final OnlineRoundService onlineRoundService;
	private final PlayerService playerService;

	public OnlineScoreCardController(ModelMapper modelMapper, OnlineRoundService onlineRoundService,
			PlayerService playerService) {
		super(modelMapper);
		this.onlineRoundService = onlineRoundService;
		this.playerService = playerService;
	}

	@MessageMapping("/hole")
	@SendTo("/topic")
	public OnlineScoreCardDto send(OnlineScoreCardDto onlineScoreCardDto) {

		log.debug("Received s -  " + onlineScoreCardDto);

		OnlineScoreCard onlineScoreCard = modelMapper.map(onlineScoreCardDto, OnlineScoreCard.class);

		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setId(onlineScoreCard.getOrId());
		onlineScoreCard.setOnlineRound(onlineRound);

		onlineRoundService.saveOnlineScoreCard(onlineScoreCard);

		return modelMapper.map(onlineScoreCard, OnlineScoreCardDto.class);
	}

	@Tag(name = "Online scoreard API")
	@Operation(summary = "Adds online rounds")
	@PostMapping(value = "/rest/OnlineRounds")
	public List<OnlineRoundDto> addOnlineRounds(
			@Parameter(description = "List of OnlineRound objects", required = true) @RequestBody List<OnlineRoundDto> onlineRounds) {

		log.info("trying to add onlineRounds");
		List<OnlineRound> orLst = mapList(onlineRounds, OnlineRound.class);

		return mapList(onlineRoundService.save(orLst), OnlineRoundDto.class);

	}

	@Tag(name = "Online scoreard API")
	@Operation(summary = "Adds online round")
	@PostMapping(value = "/rest/OnlineRound")
	public OnlineRoundDto addOnlineRound(
			@Parameter(description = "OnlineRound object", required = true) @RequestBody OnlineRoundDto onlineRoundDto) {

		log.info("trying to add onlineRound: " + onlineRoundDto);
		OnlineRound or = modelMapper.map(onlineRoundDto, OnlineRound.class);

		return modelMapper.map(onlineRoundService.save(or), OnlineRoundDto.class);

	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return all online rounds")
	@GetMapping(value = "/rest/OnlineRound")
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
	@Operation(summary = "Delete online round with given id.")
	@DeleteMapping("/rest/OnlineRound/{id}")
	public ResponseEntity<Long> deleteOnlineRound(
			@Parameter(description = "Online round id", example = "1", required = true) @PathVariable Long id) {

		log.info("trying to delete online round: " + id);

		try {
			onlineRoundService.delete(id);
		} catch (Exception e) {
			return new ResponseEntity<>(id, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(id, HttpStatus.OK);

	}

	@Tag(name = "Online scoreard API")
	@Operation(summary = "Finalize online round")
	@PostMapping(value = "/rest/FinalizeOnlineRound/{id}")
	public Round finalizeOnlineRound(
			@Parameter(description = "Online round id", example = "1", required = true) @PathVariable Long id) {

		log.info("trying to finzalie online round: " + id);

		return onlineRoundService.finalizeById(id);

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
		log.info("Requested player for nick");
		Optional<Player> player = playerService.getPlayer(nick);

		if (player.isEmpty()) {
			return null;
		}

		return modelMapper.map(player.get(), PlayerDto.class);
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Delete online round with given owner id.")
	@DeleteMapping("/rest/OnlineRoundForOwner/{ownerId}")
	public ResponseEntity<Long> deleteOnlineRoundForOwner(
			@Parameter(description = "Online round owner id", example = "1", required = true) @PathVariable Long ownerId) {

		log.info("trying to delete online round for owner: " + ownerId);

		try {
			onlineRoundService.deleteForOwner(ownerId);
		} catch (Exception e) {
			return new ResponseEntity<>(ownerId, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(ownerId, HttpStatus.OK);

	}

	@Tag(name = "Online scoreard API")
	@Operation(summary = "Finalize online rounds for owner")
	@PostMapping(value = "/rest/FinalizeOnlineOwnerRounds/")
	public HttpStatus finalizeOwnerOnlineRounds(
			@Parameter(description = "Owner object", required = true) @RequestBody Long ownerId) {

		log.info("trying to finalize online round for owner: " + ownerId);

		onlineRoundService.finalizeForOwner(ownerId);

		return HttpStatus.OK;
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return online rounds for owner")
	@GetMapping(value = "/rest/OnlineRoundOwner/{ownerId}")
	public List<OnlineRoundDto> getOnlineRoundsOwner(
			@Parameter(description = "Player (owner) id", example = "1", required = true) @PathVariable("ownerId") Long ownerId) {
		log.info("Requested online rounds for owner");
		return mapList(onlineRoundService.getOnlineRoundsForOwner(ownerId), OnlineRoundDto.class);
	}
}
