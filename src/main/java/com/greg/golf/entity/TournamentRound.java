package com.greg.golf.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "tournament_round")
public class TournamentRound {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	@JoinColumn(name = "tournament_result_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private TournamentResult tournamentResult;

	@NotNull
	@Column(name = "strokes_brutto")
	private Integer strokesBrutto;

	@NotNull
	@Column(name = "strokes_netto")
	private Integer strokesNetto;
	
	@NotNull
	@Column(name = "stb_net")
	private Integer stbNet;

	@NotNull
	@Column(name = "stb_gross")
	private Integer stbGross;
	
	@NotNull
	@Column(name = "scr_diff")
	private Float scrDiff;

	@EqualsAndHashCode.Exclude
	@NotNull
	private Boolean strokes;
	
	@NotNull
	@Size(min = 3, max = 100, message = "Course name should be between 3 and 100 characters")
	@Column(name = "course_name")
	private String courseName;

	@Column(name = "round_id")
	private Integer roundId;

	@NotNull
	@Column(name = "playing_hcp")
	private Integer playingHcp;

	@NotNull
	@Column(name = "hcp")
	private Float hcp;

	@NotNull
	@Column(name = "course_hcp")
	private Integer courseHcp;
}
