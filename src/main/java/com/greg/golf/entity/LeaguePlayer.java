package com.greg.golf.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "league_player")
public class LeaguePlayer {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@Column(name = "player_id")
	private Long playerId;

	@ToString.Exclude
	@Column(name = "league_id")
	private Long leagueId;

	@EqualsAndHashCode.Exclude
	@Column(name = "nick")
	private String nick;
}
