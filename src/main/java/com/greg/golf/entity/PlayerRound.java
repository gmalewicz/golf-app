package com.greg.golf.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
// import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "player_round")
public class PlayerRound {

	@Id
	@JsonIgnore
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//@JsonIgnore
	@ToString.Exclude
	@Column(name = "player_id")
	private Long playerId;

	@JsonIgnore
	@ToString.Exclude
	@Column(name = "round_id")
	private Long roundId;

	@Column(name = "whs")
	private Float whs;
	
	//@JsonIgnore
	@Column(name = "tee_id")
	private Long teeId;
	
	@JsonIgnore
	@Column(name = "tournament_id")
	private Long tournamentId;
	
	private Float cr;
	
	private Integer sr;
	
	@Column(name = "tee_type")
	private Integer teeType;
}
