package com.greg.golf.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "score_card")
public class ScoreCard {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	//@JsonProperty(value = "round_id", access = JsonProperty.Access.WRITE_ONLY)
	//@ManyToOne(fetch = FetchType.LAZY)
	@ManyToOne
	private Round round;

	@NotNull
	@Min(value = 1, message = "Hole number cannot be lower than 1")
    @Max(value = 18, message = "Hole number cannot be higher than 18")
	@Column(name = "hole")
	private Integer hole;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Min(value = 0, message = "Stroke number cannot be lower than 0")
    @Max(value = 16, message = "Stroke number cannot be higher than 16")
	@Column(name = "stroke")
	private Integer stroke;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Min(value = 0, message = "Putts number cannot be lower than 0")
    @Max(value = 10, message = "Putts number cannot be higher than 10")
	@Column(name = "pats")
	private Integer pats;
	
	@EqualsAndHashCode.Exclude
	@Min(value = 0, message = "Penalty number cannot be lower than 0")
    @Max(value = 5, message = "Penalty number cannot be higher than 5")
	@Column(name = "penalty")
	private Integer penalty;
	
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	//@JsonIgnore
	@Transient
	private int stbNet;
	
	//@JsonIgnore
	@Transient
	private int stbGross;
	
	//@JsonIgnore
	@Transient
	private int hcp;
	
	//@JsonIgnore
	@Transient
	private int corStroke;
}
