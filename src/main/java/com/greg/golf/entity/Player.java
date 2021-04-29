package com.greg.golf.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.JoinColumn;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "player")
public class Player implements Comparable<Player> {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Column(name = "nick")
	private String nick;

	@NotNull
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Column(name = "password")
	private String password;

	@EqualsAndHashCode.Exclude
	@NotNull
	private Boolean sex;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Min(value = -5)
	@Max(value = 54)
	@Column(name = "whs")
	private Float whs;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Min(value = 0)
	@Max(value = 1)
	@Column(name = "role")
	private Integer role;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "player_round", joinColumns = { @JoinColumn(name = "player_id") }, inverseJoinColumns = {
			@JoinColumn(name = "round_id") })
	private List<Round> rounds;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player", orphanRemoval = true)
	private List<ScoreCard> scoreCard;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player", orphanRemoval = true)
	private List<Game> game;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player", orphanRemoval = true)
	private List<TournamentResult> tournamentResult;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player", orphanRemoval = true)
	private List<Tournament> tournament;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player", orphanRemoval = true)
	private List<OnlineRound> onlineRound;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player", orphanRemoval = true)
	private List<FavouriteCourse> favouriteCourse;

	@Transient
	private String captcha;

	public void addScoreCard(ScoreCard scoreCardItem) {
		this.scoreCard.add(scoreCardItem);
		scoreCardItem.setPlayer(this);
	}

	public void removeCoreCard(ScoreCard scoreCardItem) {
		this.scoreCard.remove(scoreCardItem);
		scoreCardItem.setPlayer(null);
	}

	@Override
	public int compareTo(Player o) {

		return (int) (id - o.id);
	}

	@Override
	public boolean equals(Object obj) {
		
		var retVal = false;
		
		if (!(obj instanceof Player)) {
			return false;
		}
		
		if (id == ((Player) obj).id) {
			retVal = true;
		}
	
		return retVal;
		
	}

	@Override
	public int hashCode() {

		return super.hashCode();
	}
}
