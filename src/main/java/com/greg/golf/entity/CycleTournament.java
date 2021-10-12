package com.greg.golf.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Entity
@Table(name = "cycle_tournament")
public class CycleTournament {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	private Cycle cycle;

	@NotNull
	@Size(min = 3, max = 100, message = "Tournament name should be between 3 and 100 characters")
	@Column(name = "name")
	private String name;

	@NotNull
	@Min(value = 1, message = "Minimum number of rounds for tournament")
	@Max(value = 4, message = "Maximum number of rounds for tournament")
	@Column(name = "rounds")
	private Integer rounds;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Column(name = "best_off")
	private Boolean bestOf;
}
