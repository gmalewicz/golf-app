package com.greg.golf.entity;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "tournament")
public class Tournament {

	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	@Column(name = "name")
	private String name;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	//@JsonIgnore
	@OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "tournament")
	private List<Round> round = new ArrayList<>();

	@NotNull
	@EqualsAndHashCode.Exclude
	@Column(name = "start_date")
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
	private Date startDate;
	
	@NotNull
	@EqualsAndHashCode.Exclude
	@Column(name = "end_date")
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
	private Date endDate;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Column(name = "best_rounds")
	private Integer bestRounds;
	
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	//@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "tournament", orphanRemoval = true)
	private List<TournamentResult> tournamentResult;
	
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;
	
	public void addRound(Round roundItem) {
		this.round.add(roundItem);
		roundItem.setTournament(this);
	}

	public void removeRound(Round roundItem) {
		this.round.remove(roundItem);
		roundItem.setTournament(null);
	}

}
