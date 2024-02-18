package com.greg.golf.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "teetime")
public class TeeTime {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "flight")
	private Integer flight;

	@NotNull
	@Column(name = "time")
	private String time;

	@NotNull
	@Column(name = "nick")
	private String nick;

	@EqualsAndHashCode.Exclude
	@Column(name = "hcp")
	private Float hcp;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "teetime_parameters_id", nullable = false)
	private TeeTimeParameters teeTimeParameters;
}
