package com.greg.golf.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.greg.golf.controller.dto.OnlineScoreCardDto;
import com.greg.golf.entity.OnlineRound;
import com.greg.golf.entity.OnlineScoreCard;
import com.greg.golf.service.OnlineRoundService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class OnlineScoreCardController {
	
	@Autowired
	private OnlineRoundService onlineRoundService;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@MessageMapping("/hole")
	@SendTo("/topic")
	public OnlineScoreCardDto send(OnlineScoreCardDto onlineScoreCardDto) {
	    
		log.debug("Received s -  " + onlineScoreCardDto);
		
		OnlineScoreCard onlineScoreCard =  modelMapper.map(onlineScoreCardDto, OnlineScoreCard.class);
		
		
		OnlineRound onlineRound = new OnlineRound();
		onlineRound.setId(onlineScoreCard.getOrId());
		onlineScoreCard.setOnlineRound(onlineRound);
		
		onlineRoundService.saveOnlineScoreCard(onlineScoreCard);
		
	    return modelMapper.map(onlineScoreCard, OnlineScoreCardDto.class);
	}
}
