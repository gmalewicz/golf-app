package com.greg.golf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.greg.golf.entity.OnlineRound;
import com.greg.golf.entity.OnlineScoreCard;

@Repository
public interface OnlineScoreCardRepository extends JpaRepository<OnlineScoreCard, Long> {

	List<OnlineScoreCard> getByOnlineRound(OnlineRound onlineRound);

	Optional<OnlineScoreCard> findByOnlineRoundAndHole(OnlineRound onlineRound, Integer hole);
}
