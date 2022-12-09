package com.greg.golf.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.List;

@Data
@Entity
@Table(name = "cycle")
public class Cycle {

	public static final boolean STATUS_OPEN = false;
	public static final boolean STATUS_CLOSE = true;

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
	@Column(name = "best_rounds")
	private Integer bestRounds;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Column(name = "max_whs")
	private Float maxWhs;

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
