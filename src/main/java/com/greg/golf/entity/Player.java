package com.greg.golf.entity;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "player")
public class Player implements Comparable<Player> {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@EqualsAndHashCode.Exclude
	@Column(name = "nick")
	private String nick;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Column(name = "password")
	private String password;

	@EqualsAndHashCode.Exclude
	private Boolean sex;

	@EqualsAndHashCode.Exclude
	@Column(name = "whs")
	private Float whs;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Min(value = 0)
	@Max(value = 1)
	@Column(name = "type")
	private Integer type;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Min(value = 0)
	@Max(value = 1)
	@Column(name = "role")
	private Integer role;

	@EqualsAndHashCode.Exclude
	@NotNull
	private Boolean modified;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "player_round", joinColumns = { @JoinColumn(name = "player_id") }, inverseJoinColumns = {
			@JoinColumn(name = "round_id") })
	private List<Round> rounds;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player")
	private List<ScoreCard> scoreCard;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player")
	private List<Game> game;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player")
	private List<TournamentResult> tournamentResult;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player")
	private List<Tournament> tournament;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player")
	private List<OnlineRound> onlineRound;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player")
	private List<FavouriteCourse> favouriteCourse;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "player")
	private List<Cycle> cycle;

	@Transient
	private String captcha;

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
		
		if (id.equals(((Player) obj).id)) {
			retVal = true;
		}
	
		return retVal;
		
	}

	@Override
	public int hashCode() {

		return super.hashCode();
	}
}
