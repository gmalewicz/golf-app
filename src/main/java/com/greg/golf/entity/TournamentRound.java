package com.greg.golf.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
	//@JsonProperty( value = "courseName", access = JsonProperty.Access.READ_ONLY)
	@Size(min = 3, max = 100, message = "Course name should be between 3 and 100 characters")
	@Column(name = "course_name")
	private String courseName;
}
