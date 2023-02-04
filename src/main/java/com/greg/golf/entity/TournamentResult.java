package com.greg.golf.entity;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@Entity
@Slf4j
@Table(name = "tournament_result")
public class TournamentResult {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private Tournament tournament;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private Player player;

	@NotNull
	@Column(name = "strokes_brutto")
	private Integer strokesBrutto;

	@NotNull
	@Column(name = "strokes_netto")
	private Integer strokesNetto;

	@NotNull
	@Column(name = "played_rounds")
	private Integer playedRounds;

	@NotNull
	@Column(name = "stroke_rounds")
	private Integer strokeRounds;

	@NotNull
	@Column(name = "stb_net")
	private Integer stbNet;

	@NotNull
	@Column(name = "stb_gross")
	private Integer stbGross;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "tournamentResult")
	private List<TournamentRound> tournamentRound;

	public void increaseStrokeRounds() {
		strokeRounds++;
	}
}
