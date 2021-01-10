package com.greg.golf.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.greg.golf.entity.Game;
import com.greg.golf.entity.GameSendData;
import com.greg.golf.entity.Player;
import com.greg.golf.repository.GameRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("gameService")
public class GameService {
	
	private static Map<Long, String> gameIdNameMap = null;

	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private EmailServiceImpl emailServiceImpl;
	
	@Autowired
	TemplateEngine templateEngine;
	
	GameService() {
		
		if (gameIdNameMap == null) {
			gameIdNameMap = new HashMap<Long, String>();
			gameIdNameMap.put(1l, "Hole Stake Game");
			gameIdNameMap.put(2l, "Bingo, Bango, Bongo Game");
		}
		
	}

	@Transactional
	public Game save(Game game) {

		return gameRepository.save(game);
	}

	@Transactional(readOnly = true)
	public List<Game> listByPlayer(Player player) {

		return gameRepository.findByPlayer(player);
	}
	
	@Transactional(readOnly = true)
	public void sendGameDetail(GameSendData gameSendData) throws MessagingException {
		
		Optional<Game> game = gameRepository.findById(gameSendData.getGameId());
		log.debug("Game data retrived");
		Context context = new Context(); 
		context.setVariable("gameName", gameIdNameMap.get(game.get().getGameId()));
		context.setVariable("game", game.get());
		String body = templateEngine.process("GameDetailsMailTemplate.html", context);
		
		
		emailServiceImpl.sendEmail(gameSendData.getEmail(), "Game results", body);
		//emailServiceImpl.sendSimpleMessage("grzegorz.malewicz@gmail.com", "test", "body");
	}

}
