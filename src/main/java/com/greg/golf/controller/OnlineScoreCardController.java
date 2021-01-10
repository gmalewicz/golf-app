package com.greg.golf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.greg.golf.entity.OnlineRound;
import com.greg.golf.entity.OnlineScoreCard;
import com.greg.golf.service.OnlineRoundService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class OnlineScoreCardController {
	
	@Autowired
	private OnlineRoundService onlineRoundService;
	
	@MessageMapping("/hole")
	@SendTo("/topic")
	public OnlineScoreCard send(OnlineScoreCard onlineScoreCard) throws Exception {
	    
		log.debug("Received s -  " + onlineScoreCard);
		
		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setId(onlineScoreCard.getOnlineRoundId());
		onlineScoreCard.setOnlineRound(onlineRound);
		
		onlineRoundService.saveOnlineScoreCard(onlineScoreCard);
		
		
		
		
	    return onlineScoreCard;
	}
}
