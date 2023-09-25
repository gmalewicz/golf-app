package com.greg.golf.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "league_match")
public class LeagueMatch {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@Column(name = "league_id")
	private Long leagueId;

	@ToString.Exclude
	@Column(name = "winner_id")
	private Long winnerId;
	@ToString.Exclude
	@Column(name = "looser_id")
	private Long looserId;
	@EqualsAndHashCode.Exclude
	@Column(name = "result")
	private String result;
}
