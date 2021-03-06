package com.greg.golf.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.mail.MessagingException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.greg.golf.controller.dto.GameSendData;
import com.greg.golf.entity.Game;
import com.greg.golf.entity.Player;
import com.greg.golf.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
@Service("gameService")
public class GameService {
	
	private static final Map<Long, String> gameIdNameMap = Map.of(1l, "Hole Stake Game", 2l, "Bingo, Bango, Bongo Game");
			
	private final GameRepository gameRepository;
	private final EmailServiceImpl emailServiceImpl;
	private final TemplateEngine templateEngine;
	
	@Transactional
	public Game save(Game game) {

		return gameRepository.save(game);
	}

	@Transactional(readOnly = true)
	public List<Game> listByPlayer(Player player) {

		return gameRepository.findByPlayer(player);
	}
	
	@Transactional(readOnly = true)
	public void sendGameDetail(GameSendData gameSendData) throws MessagingException, NoSuchElementException {
		
		Optional<Game> game = gameRepository.findById(gameSendData.getGameId());
		log.debug("Game data retrived");
		
		var context = new Context(); 
		context.setVariable("gameName", gameIdNameMap.get(game.orElseThrow().getGameId()));
		context.setVariable("game", game.orElseThrow());
		String body = templateEngine.process("GameDetailsMailTemplate.html", context);
		
		
		emailServiceImpl.sendEmail(gameSendData.getEmail(), "Game results", body);
	}

}
