package com.greg.golf.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Entity
@Table(name = "cycle")
public class Cycle {

	public static final boolean STATUS_OPEN = false;
	public static final boolean STATUS_CLOSE = true;

	public static final Integer RULE_VOLVO_2021 = 1;
	// based on STB net without any modifications
	public static final Integer RULE_STANDARD = 0;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	@Column(name = "name")
	private String name;

	@EqualsAndHashCode.Exclude
	@NotNull
	private Boolean status;

	@EqualsAndHashCode.Exclude
	@NotNull
	private Integer rule;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "cycle", orphanRemoval = true)
	private List<CycleTournament> cycleTournaments;
}
