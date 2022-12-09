package com.greg.golf.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "player_round")
public class PlayerRound {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@Column(name = "player_id")
	private Long playerId;

	@ToString.Exclude
	@Column(name = "round_id")
	private Long roundId;

	@Column(name = "whs")
	private Float whs;

	@Column(name = "tee_id")
	private Long teeId;

	@Column(name = "tournament_id")
	private Long tournamentId;

	private Float cr;

	private Integer sr;

	@Column(name = "tee_type")
	private Integer teeType;
}
