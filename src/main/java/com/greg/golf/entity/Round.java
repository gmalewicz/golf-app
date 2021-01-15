package com.greg.golf.entity;

import java.util.Date;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;



import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "round")
public class Round {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	//@JsonProperty(value = "matchPlay")
	@Column(name = "match_play")
	private Boolean matchPlay;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToOne
	//@JsonProperty(value = "course")
	private Course course;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	//@JsonProperty(value = "player", access = JsonProperty.Access.WRITE_ONLY)
	//@JsonProperty(value = "player")
	//@JsonView(Views.RoundWithPlayer.class)
	@ManyToMany(cascade = { CascadeType.DETACH }, fetch = FetchType.LAZY)
	@JoinTable(name = "player_round", joinColumns = { @JoinColumn(name = "round_id") }, inverseJoinColumns = {
			@JoinColumn(name = "player_id") })
	private Set<Player> player;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Column(name = "round_date")
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd kk:mm")
	private Date roundDate;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	//@SortNatural
	//@OrderBy
	//@JsonProperty(value = "scoreCard", access = JsonProperty.Access.WRITE_ONLY)
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "round", orphanRemoval = true)
	// @JoinColumn(name = "round_id")
	private List<ScoreCard> scoreCard = new ArrayList<>();
	
	@EqualsAndHashCode.Exclude
	//@JsonProperty(value = "tournament", access = JsonProperty.Access.WRITE_ONLY)
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	private Tournament tournament;
	
	public void addScoreCard(ScoreCard scoreCardItem) {
		this.scoreCard.add(scoreCardItem);
		scoreCardItem.setRound(this);
	}

	public void removeScoreCard(ScoreCard scoreCardItem) {
		this.scoreCard.remove(scoreCardItem);
		scoreCardItem.setRound(null);
	}

}
