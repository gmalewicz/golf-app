package com.greg.golf.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "tournament_player")
public class TournamentPlayer {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@Column(name = "player_id")
	private Long playerId;

	@ToString.Exclude
	@Column(name = "tournament_id")
	private Long tournamentId;

	@Column(name = "whs")
	private Float whs;

	@EqualsAndHashCode.Exclude
	@Column(name = "nick")
	private String nick;

	@EqualsAndHashCode.Exclude
	private Boolean sex;
}
