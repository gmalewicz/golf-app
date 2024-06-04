package com.greg.golf.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "online_score_card")
public class OnlineScoreCard {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@JoinColumn(name = "online_round_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private OnlineRound onlineRound;

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
	@Min(value = 0, message = "Putts number cannot be lower than 0")
	@Max(value = 5, message = "Putts number cannot be higher than 5")
	@Column(name = "putt")
	private Integer putt;

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

	@EqualsAndHashCode.Exclude
	@Size(min = 5, max = 5, message=  "Hour must be exact 5 characters long. Seconds not included")
	@Column(name = "time")
	private String time;

	@Transient
	private long orId;

	@Transient
	private boolean update;

	@Transient
	private boolean syncRequired;

	@EqualsAndHashCode.Exclude
	@Column(name = "lat")
	private Double lat;

	@EqualsAndHashCode.Exclude
	@Column(name = "lng")
	private Double lng;
}
