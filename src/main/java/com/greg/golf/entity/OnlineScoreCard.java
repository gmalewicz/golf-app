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
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "online_score_card")
public class OnlineScoreCard {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	//@JsonProperty(value = "onlineRound_id", access = JsonProperty.Access.WRITE_ONLY)
	@JsonIgnore
	@JoinColumn(name = "online_round_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private OnlineRound onlineRound;

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
	@Min(value = 0, message = "Putts number cannot be lower than 0")
    @Max(value = 5, message = "Putts number cannot be higher than 5")
	@Column(name = "putt")
	private Integer putt;
		
	@EqualsAndHashCode.Exclude
	@Min(value = 0, message = "Penalty number cannot be lower than 0")
    @Max(value = 5, message = "Penalty number cannot be higher than 5")
	@Column(name = "penalty")
	private Integer penalty;
	
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;
	
	@Transient
	private long orId;
	
	@Transient
	private boolean update;

}
