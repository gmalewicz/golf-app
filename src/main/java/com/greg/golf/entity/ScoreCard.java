package com.greg.golf.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import javax.persistence.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "score_card")
public class ScoreCard {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	//@JsonProperty(value = "round_id", access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	private Round round;

	@NotNull
	@Min(value = 1, message = "Hole number cannot be lower than 1")
    @Max(value = 18, message = "Hole number cannot be higher than 18")
	@Column(name = "hole")
	private Integer hole;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Min(value = 0, message = "Stroke number cannot be lower than 0")
    @Max(value = 15, message = "Stroke number cannot be higher than 15")
	@Column(name = "stroke")
	private Integer stroke;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Min(value = 0, message = "Putts number cannot be lower than 0")
    @Max(value = 10, message = "Putts number cannot be higher than 10")
	@Column(name = "pats")
	private Integer pats;
	
	@EqualsAndHashCode.Exclude
	@Min(value = 0, message = "Penalty number cannot be lower than 0")
    @Max(value = 5, message = "Penalty number cannot be higher than 15")
	@Column(name = "penalty")
	private Integer penalty;
	
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	//@JsonIgnore
	@Transient
	private int stbNet;
	
	//@JsonIgnore
	@Transient
	private int stbGross;
	
	//@JsonIgnore
	@Transient
	private int hcp;
	
	//@JsonIgnore
	@Transient
	private int corStroke;
}
