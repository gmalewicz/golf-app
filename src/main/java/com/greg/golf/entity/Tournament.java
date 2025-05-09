package com.greg.golf.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "tournament")
public class Tournament {

	public static final boolean STATUS_OPEN = false;
	public static final boolean STATUS_CLOSE = true;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	@Column(name = "name")
	private String name;

	@NotNull
	@EqualsAndHashCode.Exclude
	@Column(name = "start_date")
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
	private Date startDate;
	
	@NotNull
	@EqualsAndHashCode.Exclude
	@Column(name = "end_date")
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
	private Date endDate;

	@EqualsAndHashCode.Exclude
	@NotNull
	@Column(name = "best_rounds")
	private Integer bestRounds;

	@EqualsAndHashCode.Exclude
	@NotNull
	private Boolean status;

	@Column(name = "play_hcp_multiplayer")
	@NotNull
	private Float playHcpMultiplayer;

	@EqualsAndHashCode.Exclude
	@Column(name = "max_play_hcp")
	private Integer maxPlayHcp;

	@EqualsAndHashCode.Exclude
	@Column(name = "can_update_hcp")
	@NotNull
	private Boolean canUpdateHcp;
	
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	//@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "tournament", orphanRemoval = true)
	private List<TournamentResult> tournamentResult;
	
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "tournament", orphanRemoval = true)
	private TeeTimeParameters teeTimeParameters;

}
