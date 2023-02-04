package com.greg.golf.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "round")
public class Round {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	@Column(name = "match_play")
	private Boolean matchPlay;
	
	@Column(name = "mp_format")
	private Float mpFormat;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	private Course course;

	@OrderBy("id ASC")
	//@SortNatural
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToMany(cascade = { CascadeType.DETACH }, fetch = FetchType.LAZY)
	@JoinTable(name = "player_round", joinColumns = { @JoinColumn(name = "round_id") }, inverseJoinColumns = {
			@JoinColumn(name = "player_id") })
	private Set<Player> player;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Column(name = "round_date")
	private Date roundDate;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "round", orphanRemoval = true)
	private List<ScoreCard> scoreCard = new ArrayList<>();

}
