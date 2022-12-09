package com.greg.golf.entity;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Data
@Entity
@Table(name = "online_round")
public class OnlineRound {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	private Course course;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Column(name = "time_time")
	private String teeTime;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Column(name = "date")
	private Date date;

	@NotNull
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@NotNull
	private Long owner;

	@NotNull
	private Boolean finalized;

	private Boolean putts;

	private Boolean penalties;

	@Column(name = "match_play")
	private Boolean matchPlay;
	
	@Column(name = "mp_format")
	private Float mpFormat;

	@EqualsAndHashCode.Exclude
	@Column(name = "nick2")
	private String nick2;
		
	@NotNull
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_tee_id", nullable = false)
	private CourseTee courseTee;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "onlineRound", orphanRemoval = true)
	private List<OnlineScoreCard> scoreCard = new ArrayList<>();

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Transient
	private List<OnlineScoreCard> scoreCardAPI = new ArrayList<>();
}
