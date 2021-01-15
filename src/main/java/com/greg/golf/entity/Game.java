package com.greg.golf.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;
import lombok.ToString;
import javax.validation.constraints.NotNull;


@Data
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
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
	//@JsonProperty( value = "player", access = JsonProperty.Access.WRITE_ONLY)
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
	@Type(type = "jsonb")
    @Column(name = "game_data", columnDefinition = "jsonb")
	private GameData gameData;
}
