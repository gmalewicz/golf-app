package com.greg.golf.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.List;
import java.util.ArrayList;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import javax.persistence.CascadeType;

@Data
@Entity
@Table(name = "online_round")
public class OnlineRound {

	@Id
	// @JsonIgnore
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
	// @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "kk:mm")
	private String teeTime;

	@EqualsAndHashCode.Exclude
	@NotNull
	// @JsonIgnore
	@Column(name = "date")
	private Date date;

	@NotNull
	@ToString.Exclude
	// @JsonProperty(value = "player")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@NotNull
	// @JsonProperty(value = "owner")
	private Long owner;

	@NotNull
	// @JsonProperty(value = "finalized")
	private Boolean finalized;

	// @JsonProperty(value = "putts")
	private Boolean putts;

	// @JsonProperty(value = "penalties")
	private Boolean penalties;

	// @JsonProperty(value = "matchPlay")
	@Column(name = "match_play")
	private Boolean matchPlay;

	@EqualsAndHashCode.Exclude
	// @Schema(description = "Player nick name", example = "golfer")
	@Column(name = "nick2")
	private String nick2;

	@NotNull
	@ToString.Exclude
	// @JsonProperty(value = "tee")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_tee_id", nullable = false)
	private CourseTee courseTee;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	// @JsonProperty(value = "onlineScoreCard", access =
	// JsonProperty.Access.WRITE_ONLY)
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "onlineRound", orphanRemoval = true)
	private List<OnlineScoreCard> scoreCard = new ArrayList<>();

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@Transient
	private List<OnlineScoreCard> scoreCardAPI = new ArrayList<>();
}
