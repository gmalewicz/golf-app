package com.greg.golf.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "tournament_result")
public class TournamentResult {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	// @JsonIgnore
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
	// @JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "tournamentResult")
	private List<TournamentRound> tournamentRound;

	public void increaseStrokeRounds() {
		strokeRounds++;
	}
}
