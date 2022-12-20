package com.greg.golf.entity;

import java.util.Date;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "game")
public class Game {
 
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@JsonIgnore
	private Long id;

	@NotNull
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;
	
	@NotNull
	@Column(name = "game_id")
	private Long gameId;

	@NotNull
	@Column(name = "stake")
	private Float stake;
	
	@NotNull(message ="Data in ISO format") 
	@Column(name = "game_date")
	private Date gameDate;
	
	@NotNull
	@Type(JsonBinaryType.class)
    @Column(name = "game_data", columnDefinition = "jsonb")
	private GameData gameData;
}
