package com.greg.golf.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.entity.Round;
import com.greg.golf.entity.ScoreCard;

import com.greg.golf.repository.ScoreCardRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service("scoreCardService")
public class ScoreCardService {

	private final ScoreCardRepository scoreCardRepository;

	@Transactional(readOnly = true)
	public List<ScoreCard> listByRound(Round round) {

		return scoreCardRepository.findByRound(round);
	}
}
