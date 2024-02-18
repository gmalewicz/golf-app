package com.greg.golf.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@Entity
@Table(name = "teetime_parameters")
public class TeeTimeParameters {

	public static final boolean STATUS_NOT_PUBLISHED = false;
	public static final boolean STATUS_PUBLISHED = true;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "flight_size")
	private Integer flightSize;

	@NotNull
	@Column(name = "first_teetime")
	private String firstTeeTime;

	@NotNull
	@Column(name = "teetime_step")
	private Integer teeTimeStep;

	@NotNull
	@Column(name = "published")
	private Boolean published;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "teeTimeParameters")
	private List<TeeTime> teeTimes;


	@NotNull
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tournament_id")
	private Tournament tournament;
}
