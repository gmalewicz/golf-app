package com.greg.golf.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;


import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.greg.golf.controller.dto.GameSendData;
import com.greg.golf.entity.Game;
import com.greg.golf.entity.Player;
import com.greg.golf.repository.GameRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Slf4j
@Service("gameService")
public class GameService {
	
	private static final Map<Long, String> gameIdNameMap = Map.of(1L, "Hole Stake Game", 2L, "Bingo, Bango, Bongo Game");
			
	private final GameRepository gameRepository;
	private final EmailServiceImpl emailServiceImpl;
	private final TemplateEngine templateEngine;
	
	@Transactional
	public void save(Game game) {

		gameRepository.save(game);
	}

	@Transactional(readOnly = true)
	public List<Game> listByPlayer(Player player) {

		return gameRepository.findByPlayer(player);
	}
	
	@Transactional(readOnly = true)
	public void sendGameDetail(GameSendData gameSendData) throws MessagingException, NoSuchElementException {
		
		Optional<Game> game = gameRepository.findById(gameSendData.getGameId());
		log.debug("Game data retrieved");
		
		var context = new Context(); 
		context.setVariable("gameName", gameIdNameMap.get(game.orElseThrow().getGameId()));
		context.setVariable("game", game.orElseThrow());
		String body = templateEngine.process("GameDetailsMailTemplate.html", context);
		
		
		emailServiceImpl.sendEmail(gameSendData.getEmail(), "Game results", body);
	}

}
