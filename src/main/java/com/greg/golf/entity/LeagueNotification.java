package com.greg.golf.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "league_notification")
public class LeagueNotification {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@NotNull
	@Column(name = "player_id")
	private Long playerId;

	@ToString.Exclude
	@NotNull
	@Column(name = "league_id")
	private Long leagueId;
}
