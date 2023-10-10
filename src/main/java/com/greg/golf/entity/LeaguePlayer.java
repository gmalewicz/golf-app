package com.greg.golf.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

	@EqualsAndHashCode.Exclude
	@Column(name = "nick")
	private String nick;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	@JoinColumn(name = "league_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private League league;
}
