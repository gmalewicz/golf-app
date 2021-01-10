package com.greg.golf.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;
import com.greg.golf.repository.ScoreCardRepository;


@Service("scoreCardService")
public class ScoreCardService {
	

	@Autowired
	private ScoreCardRepository scoreCardRepository;

	@Transactional(readOnly = true)
	public List<ScoreCard> listByRound(Round round) {


		return scoreCardRepository.findByRound(round);
	}
	
	@Transactional(readOnly = true)
	public List<ScoreCard> findByRoundAndPlayer(Round round, Player player) {
		return scoreCardRepository.findByRoundAndPlayer(round, player);
	}
}
