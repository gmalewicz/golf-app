package com.greg.golf.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	@Column(name = "winner_id")
	private Long winnerId;

	@ToString.Exclude
	@Column(name = "looser_id")
	private Long looserId;

	@EqualsAndHashCode.Exclude
	@Column(name = "result")
	private String result;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	@JoinColumn(name = "league_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private League league;
}
